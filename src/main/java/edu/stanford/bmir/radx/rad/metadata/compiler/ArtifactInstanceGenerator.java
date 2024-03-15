package edu.stanford.bmir.radx.rad.metadata.compiler;

import edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators.*;
import org.metadatacenter.artifacts.model.core.*;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.net.URISyntaxException;
import java.util.*;

public class ArtifactInstanceGenerator {
  private final Map<FieldType, FieldGenerator> fieldGenerators;

  public ArtifactInstanceGenerator() {
    fieldGenerators = Map.ofEntries(
        Map.entry(FieldType.LINK, new LinkFieldGenerator()),
        Map.entry(FieldType.TEXTFIELD, new TextFieldGenerator()),
        Map.entry(FieldType.TEXTAREA, new TextAreaFieldGenerator()),
        Map.entry(FieldType.NUMERIC, new NumericFieldGenerator()),
        Map.entry(FieldType.TEMPORAL, new TemporalFieldGenerator()),
        Map.entry(FieldType.RADIO, new RadioFieldGenerator()),
        Map.entry(FieldType.PHONE_NUMBER, new PhoneNumberFieldGenerator()),
        Map.entry(FieldType.EMAIL, new EmailGenerator()),
        Map.entry(FieldType.CHECKBOX, new CheckBoxFieldGenerator()),
        Map.entry(FieldType.LIST, new ListGenerator()),
        Map.entry(FieldType.CONTROLLED_TERM, new ControlledTermGenerator())
    );
  }

  public void buildFieldInstanceWithValues(FieldType fieldType,
                                           Map<String, List<String>> fields,
                                           String expectedField,
                                           boolean isMultipleField,
                                           ElementInstanceArtifact.Builder elementInstanceArtifactBuilder,
                                           Optional<ValueConstraints> valueConstraints) {
    var value = fields.get(expectedField).get(0);
    FieldGenerator generator = fieldGenerators.get(fieldType);
    var fieldInstanceArtifact = generator.buildWithValue(value, valueConstraints);
    updateElementInstanceWithFieldInstance(isMultipleField, elementInstanceArtifactBuilder, expectedField, fieldInstanceArtifact);
  }

  public void buildEmptyFieldInstance(FieldType fieldType,
                                      String expectedField,
                                      boolean isMultipleField,
                                      ElementInstanceArtifact.Builder elementInstanceArtifactBuilder){
    FieldGenerator generator = fieldGenerators.get(fieldType);
    FieldInstanceArtifact fieldInstanceArtifact = generator.buildEmptyFieldInstanceArtifact();
    updateElementInstanceWithFieldInstance(isMultipleField, elementInstanceArtifactBuilder, expectedField, fieldInstanceArtifact);
  }

  private void updateElementInstanceWithFieldInstance(boolean isMultipleField, ElementInstanceArtifact.Builder elementInstanceArtifactBuilder, String expectedField, FieldInstanceArtifact fieldInstanceArtifact){
    if(isMultipleField){
      elementInstanceArtifactBuilder.withMultiInstanceFieldInstances(expectedField, List.of(fieldInstanceArtifact));
    } else{
      elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField, fieldInstanceArtifact);
    }
  }

  /***
   * Build elements that have values in the spreadsheet
   * groupedData format is:
   * {
   *    *   Element:{
   *    *     index{
   *    *       field1: [value],
   *    *       field2: [value],
   *    *       filed3(attribute-value): [spreadsheetField1, spreadsheetField2]
   *    *     }
   *    *   }
   *    * }
   */
  public void buildElementInstancesWithValues(Map<String, Map<Integer, Map<String, List<String>>>> groupedData,
                                              TemplateSchemaArtifact templateSchemaArtifact,
                                              TemplateInstanceArtifact.Builder templateInstanceArtifactBuilder,
                                              Map<String, String> spreadsheetData) throws URISyntaxException {

    for (Map.Entry<String, Map<Integer, Map<String, List<String>>>> entry : groupedData.entrySet()) {
      var elementName = entry.getKey();
      var instances = entry.getValue();
      var childFields = templateSchemaArtifact.getElementSchemaArtifact(elementName).getFieldNames();
      var childElements = templateSchemaArtifact.getElementSchemaArtifact(elementName).getElementNames();
      var isMultipleElement = templateSchemaArtifact.getElementSchemaArtifact(elementName).isMultiple();
      List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();

      for(Map.Entry<Integer, Map<String, List<String>>> instanceEntry : instances.entrySet()){
        var fields = instanceEntry.getValue();
        var elementInstanceBuilder = ElementInstanceArtifact.builder();

        for(var expectedField : childFields){
          var fieldInstanceArtifact = templateSchemaArtifact.getElementSchemaArtifact(elementName).getFieldSchemaArtifact(expectedField);
          var expectedFieldValueConstraint = fieldInstanceArtifact.valueConstraints();
          var expectedFieldType = FieldType.getFieldType(fieldInstanceArtifact.fieldUi().inputType());
          if(expectedFieldValueConstraint.isPresent() && expectedFieldValueConstraint.get().isControlledTermValueConstraint()){
            expectedFieldType = FieldType.CONTROLLED_TERM;
          }

          var isMultipleField = false;
          if(expectedFieldValueConstraint.isPresent()){
            isMultipleField = expectedFieldValueConstraint.get().multipleChoice();
          }

          //check if field is an attribute value type
          //if yes, build attribute value fields, otherwise, build regular fields
          var specificationPath = "/" + elementName + "/" + expectedField;
          if(AttributeValueFieldUtil.isAttributeValue(templateSchemaArtifact, specificationPath)){
            var values = fields.get(expectedField);
            Map<String, FieldInstanceArtifact> attributeValueFieldInstances = new HashMap<>();
            if(values != null){
              for (var value: values){
                var spreadsheetValue = spreadsheetData.get(value);
                if(spreadsheetValue != null && !spreadsheetValue.equals("")){
                  attributeValueFieldInstances.put(value,
                      FieldInstanceArtifact.textFieldInstanceBuilder().withValue(spreadsheetValue).build());
                }
              }
            }
            elementInstanceBuilder.withAttributeValueFieldGroup(expectedField, attributeValueFieldInstances);

          } else{
            // if the expectedField in the template has the mapping field in the spreadsheet, then need to retrieve data from spreadsheet
            // otherwise, build an empty fieldArtifactInstance
            if (fields.containsKey(expectedField)){
              buildFieldInstanceWithValues(expectedFieldType, fields, expectedField, isMultipleField, elementInstanceBuilder, expectedFieldValueConstraint);
            } else{
              if(isMultipleField){
                buildEmptyFieldInstance(expectedFieldType,expectedField, true, elementInstanceBuilder);
              } else{
                //Add values to RADx-rad specific controlled terms fields or add an empty field entry
                var elementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact(elementName);
                RadxRadPrecisionFieldHandler.addSpecificControlledTerms(elementInstanceBuilder, elementName, expectedField, fields, elementSchemaArtifact);
              }
            }
          }
        };

        //Build nested child element
        //Since RADx-rad spreadsheet don't have fields that maps to nested element
        //directly build empty nested element
        for(var childElement : childElements){
          buildSingleEmptyElementInstance(childElement, templateSchemaArtifact, elementInstanceBuilder, "/" + elementName + "/" + childElement);
        }

        //Add JsonLdContext for each elementInstance
        ContextGenerator.generateElementInstanceContext(
            templateSchemaArtifact.getElementSchemaArtifact(elementName),
            elementInstanceBuilder);

        //Add @id
        IdGenerator.generateElementId(elementInstanceBuilder);

        elementInstanceArtifacts.add(elementInstanceBuilder.build());
      };

      if(isMultipleElement){
        templateInstanceArtifactBuilder.withMultiInstanceElementInstances(elementName, elementInstanceArtifacts);
      } else{
        templateInstanceArtifactBuilder.withSingleInstanceElementInstance(elementName, elementInstanceArtifacts.get(0));
      }
    };
  }

  /***
   * Build Elements that does not have value in spreadsheet
   * @param notPresentElements
   * @param templateSchemaArtifact
   * @param templateInstanceBuilder
   * @return
   */
  public void buildEmptyElementInstances(Set<String> notPresentElements,
                                          TemplateSchemaArtifact templateSchemaArtifact,
                                          TemplateInstanceArtifact.Builder templateInstanceBuilder) throws URISyntaxException {
    for(var elementName : notPresentElements){
      var isMultipleElement = templateSchemaArtifact.getElementSchemaArtifact(elementName).isMultiple();
      var elementInstanceBuilder = ElementInstanceArtifact.builder();
      if(isMultipleElement){
        templateInstanceBuilder.withEmptyMultiInstanceElementInstances(elementName);
      } else{
        buildSingleEmptyElementInstance(elementName, templateSchemaArtifact, elementInstanceBuilder, "/" + elementName);
        templateInstanceBuilder.withSingleInstanceElementInstance(elementName, elementInstanceBuilder.build());
      }
    }
  }

  public void buildSingleEmptyElementInstance(String elementName,
                                               TemplateSchemaArtifact templateSchemaArtifact,
                                               ElementInstanceArtifact.Builder elementInstanceBuilder,
                                               String path) throws URISyntaxException {
    var elementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact(elementName);
    var childFields = elementSchemaArtifact.getFieldNames();

    //Add context
    ContextGenerator.generateElementInstanceContext(elementSchemaArtifact, elementInstanceBuilder);

    //Add @id
    IdGenerator.generateElementId(elementInstanceBuilder);

    //Add child field instances
    for(var expectedField : childFields){
      var specificationPath = path + "/" + expectedField;
      if(AttributeValueFieldUtil.isAttributeValue(templateSchemaArtifact, specificationPath)){
        elementInstanceBuilder.withAttributeValueFieldGroup(expectedField, Collections.emptyMap());
      } else{
        var fieldSchemaArtifact = elementSchemaArtifact.getFieldSchemaArtifact(expectedField);
        var inputType = FieldType.getFieldType(fieldSchemaArtifact.fieldUi().inputType());
        var isMultiple = fieldSchemaArtifact.isMultiple();
        buildEmptyFieldInstance(inputType, expectedField, isMultiple, elementInstanceBuilder);
      }
    }

    //Add child element instances
    var childElements = elementSchemaArtifact.getElementNames();
    for (var childElement : childElements){
      if (elementSchemaArtifact.getElementSchemaArtifact(childElement).isMultiple()){
        elementInstanceBuilder.withEmptyMultiInstanceElementInstances(childElement);
      } else {
        buildSingleEmptyElementInstance(childElement, templateSchemaArtifact, elementInstanceBuilder, path + "/" + childElement);
      }
    }
  }
}
