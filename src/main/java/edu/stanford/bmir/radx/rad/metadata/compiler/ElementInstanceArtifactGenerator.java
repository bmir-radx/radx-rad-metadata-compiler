package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.*;

import java.net.URISyntaxException;
import java.util.*;

public class ElementInstanceArtifactGenerator {
  private final FieldInstanceArtifactGenerator fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator();

  public List<ElementInstanceArtifact> generateElementInstanceWithValue(String currentElement,
                                                               Map<String, Map<Integer, Map<String, List<String>>>> groupedData,
                                                               ElementSchemaArtifact elementSchemaArtifact,
                                                               TemplateSchemaArtifact templateSchemaArtifact,
                                                               Map<String, String> spreadsheetData,
                                                                String path) throws URISyntaxException {

    var childFields = elementSchemaArtifact.getFieldNames();
    var instances = groupedData.get(currentElement);

    List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();

    for(Map.Entry<Integer, Map<String, List<String>>> instanceEntry : instances.entrySet()) {
      var fields = instanceEntry.getValue();
      var elementInstanceBuilder = ElementInstanceArtifact.builder();

      for (var expectedField : childFields) {
        var fieldSchemaArtifact = elementSchemaArtifact.getFieldSchemaArtifact(expectedField);
        var expectedFieldValueConstraint = fieldSchemaArtifact.valueConstraints();
        var expectedFieldType = FieldType.getFieldType(fieldSchemaArtifact.fieldUi().inputType());
        if (expectedFieldValueConstraint.isPresent() && expectedFieldValueConstraint.get().isControlledTermValueConstraint()) {
          expectedFieldType = FieldType.CONTROLLED_TERM;
        }

        var isMultipleField = false;
        if (expectedFieldValueConstraint.isPresent()) {
          isMultipleField = expectedFieldValueConstraint.get().multipleChoice();
        }

        //check if field is an attribute value type
        //if yes, build attribute value fields, otherwise, build regular fields
        var specificationPath = path + "/" + currentElement + "/" + expectedField;
        if (AttributeValueFieldUtil.isAttributeValue(templateSchemaArtifact, specificationPath)) {
          var values = fields.get(expectedField);
          Map<String, FieldInstanceArtifact> attributeValueFieldInstances = new HashMap<>();
          if (values != null) {
            for (var value : values) {
              var spreadsheetValue = spreadsheetData.get(value);
              if (spreadsheetValue != null && !spreadsheetValue.equals("")) {
                attributeValueFieldInstances.put(value,
                    FieldInstanceArtifact.textFieldInstanceBuilder().withValue(spreadsheetValue).build());
              }
            }
          }
          elementInstanceBuilder.withAttributeValueFieldGroup(expectedField, attributeValueFieldInstances);

        } else {
          // if the expectedField in the template has the mapping field in the spreadsheet, then need to retrieve data from spreadsheet
          // otherwise, build an empty fieldArtifactInstance
          FieldInstanceArtifact fieldInstanceArtifact;
          if (fields.containsKey(expectedField)) {
            var value = fields.get(expectedField).get(0);
            fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(expectedFieldType, value, expectedFieldValueConstraint);
            if(isMultipleField){
              elementInstanceBuilder.withMultiInstanceFieldInstances(expectedField, List.of(fieldInstanceArtifact));
            } else{
              elementInstanceBuilder.withSingleInstanceFieldInstance(expectedField, fieldInstanceArtifact);
            }
          } else {
            if (isMultipleField) {
              fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildEmptyFieldInstance(expectedFieldType);
              elementInstanceBuilder.withMultiInstanceFieldInstances(expectedField, List.of(fieldInstanceArtifact));
            } else {
              //Add values to RADx-rad specific controlled terms fields or add an empty field entry
              RadxRadPrecisionFieldHandler.addSpecificControlledTerms(elementInstanceBuilder, currentElement, expectedField, fields, elementSchemaArtifact);
            }
          }
        }
      }

      //Build nested child element
      buildWithElementInstances(groupedData, spreadsheetData, templateSchemaArtifact, elementSchemaArtifact, elementInstanceBuilder, path + "/" + currentElement);

      //Add JsonLdContext for each elementInstance
      ContextGenerator.generateElementInstanceContext(
          elementSchemaArtifact,
          elementInstanceBuilder);

      //Add @id
      IdGenerator.generateElementId(elementInstanceBuilder);

      elementInstanceArtifacts.add(elementInstanceBuilder.build());
    }

    return elementInstanceArtifacts;
  }

  private void buildWithElementInstances(Map<String, Map<Integer, Map<String, List<String>>>> groupedData,
                                         Map<String, String> spreadsheetData,
                                         TemplateSchemaArtifact templateSchemaArtifact,
                                         ElementSchemaArtifact currentElementSchemaArtifact,
                                         ElementInstanceArtifact.Builder elementInstanceBuilder,
                                         String path) throws URISyntaxException {
    var childElements = currentElementSchemaArtifact.getElementNames();
    var mappedElements = groupedData.keySet();
    for (var childElement : childElements) {
      var childElementSchemaArtifact = currentElementSchemaArtifact.getElementSchemaArtifact(childElement);
      var isChildElementMultiple = childElementSchemaArtifact.isMultiple();
      //build element that has mapping in radx rad spreadsheet
      if (mappedElements.contains(childElement)){
        var childElementInstanceArtifacts = generateElementInstanceWithValue(childElement, groupedData, childElementSchemaArtifact, templateSchemaArtifact, spreadsheetData, path);
        if(isChildElementMultiple){
          elementInstanceBuilder.withMultiInstanceElementInstances(childElement, childElementInstanceArtifacts);
        } else{
          elementInstanceBuilder.withSingleInstanceElementInstance(childElement, childElementInstanceArtifacts.get(0));
        }
      } else{ // build empty element
        if(isChildElementMultiple){
          elementInstanceBuilder.withEmptyMultiInstanceElementInstances(childElement);
        } else{
          buildSingleEmptyElementInstance(childElement, templateSchemaArtifact, path + "/" + childElement);
        }
      }
    }
  }

  public ElementInstanceArtifact buildSingleEmptyElementInstance(String elementName,
                                               TemplateSchemaArtifact templateSchemaArtifact,
                                               String path) throws URISyntaxException {
    var elementInstanceBuilder = ElementInstanceArtifact.builder();
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
        var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildEmptyFieldInstance(inputType);
        if(isMultiple){
          elementInstanceBuilder.withEmptyMultiInstanceFieldInstances(expectedField);
        } else{
          elementInstanceBuilder.withSingleInstanceFieldInstance(expectedField, fieldInstanceArtifact);
        }
      }
    }

    //Add child element instances
    var childElements = elementSchemaArtifact.getElementNames();
    for (var childElement : childElements){
      if (elementSchemaArtifact.getElementSchemaArtifact(childElement).isMultiple()){
        elementInstanceBuilder.withEmptyMultiInstanceElementInstances(childElement);
      } else {
        var emptyElementInstanceArtifact = buildSingleEmptyElementInstance(childElement, templateSchemaArtifact, path + "/" + childElement);
        elementInstanceBuilder.withSingleInstanceElementInstance(childElement, emptyElementInstanceArtifact);
      }
    }
    return elementInstanceBuilder.build();
  }
}
