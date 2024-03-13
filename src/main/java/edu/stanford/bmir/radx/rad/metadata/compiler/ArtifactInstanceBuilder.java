package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.*;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class ArtifactInstanceBuilder {
  public void buildFieldInstanceWithValues(Optional<ValueConstraints> valueConstraints,
                                           Map<String, List<String>> fields,
                                           String expectedField,
                                           boolean isMultipleField,
                                           ElementInstanceArtifact.Builder elementInstanceArtifactBuilder) throws URISyntaxException {
    var controlledTermsMap = MapInitializer.createControlledTermsMap();
    var fieldInstanceBuilder = FieldInstanceArtifact.builder();
    var value = fields.get(expectedField).get(0);

    if(valueConstraints.isPresent()){

      if(valueConstraints.get().isLinkValueConstraint()){
        if(value != null){
          fieldInstanceBuilder.withJsonLdId(new URI(value));
        }
      } else if (valueConstraints.get().isControlledTermValueConstraint()) {
        if(value != null) {
          fieldInstanceBuilder.withLabel(value);
          fieldInstanceBuilder.withJsonLdId(new URI(controlledTermsMap.get(value)));
        }
      } else if (valueConstraints.get().isTemporalValueConstraint()) {
        if(value != null) {
          var type = valueConstraints.get().asTemporalValueConstraints().temporalType().toString();
          fieldInstanceBuilder.withJsonLdValue(value);
          fieldInstanceBuilder.withJsonLdType(new URI(type));
        }
      } else{
        if(value != null){
          fieldInstanceBuilder.withJsonLdValue(value);
        }
      }
      //TODO: add other field input field
    }

    if(isMultipleField){
      elementInstanceArtifactBuilder.withMultiInstanceFieldInstances(expectedField, List.of(fieldInstanceBuilder.build()));
    } else{
      elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField, fieldInstanceBuilder.build());
    }
  }

  /***
   * Build elements that have values in the spreadsheet
   * @return
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
          var expectedFieldValueConstraint = templateSchemaArtifact.getElementSchemaArtifact(elementName).getFieldSchemaArtifact(expectedField).valueConstraints();

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
                      FieldInstanceArtifact.builder().
                          withJsonLdValue(spreadsheetValue).
                          build());
                }
              }
            }
            elementInstanceBuilder.withAttributeValueFieldInstances(expectedField, attributeValueFieldInstances);

          } else{
            // if the expectedField in the template has the corresponding field in the spreadsheet, then need to retrieve data from spreadsheet
            // otherwise, build an empty fieldArtifactInstance
            if (fields.containsKey(expectedField)){
              buildFieldInstanceWithValues(expectedFieldValueConstraint, fields, expectedField, isMultipleField, elementInstanceBuilder);
            } else{
              if(isMultipleField){
                elementInstanceBuilder.withMultiInstanceFieldInstances(expectedField, List.of(FieldInstanceArtifact.builder().build()));
              } else{
                //Add values to RADx-rad specific controlled terms fields or add an empty field entry
                var elementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact(elementName);
                SpecificControlledTermUtil.addSpecificControlledTerms(elementInstanceBuilder, elementName, expectedField, fields, elementSchemaArtifact);
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

        //Before add to elementInstanceArtifact list, check if all fields are empty, if yes, return an empty element
//        elementInstanceArtifacts.add(
//            emptyArtifactChecker.getOrEmptyElementInstanceArtifact(elementInstanceBuilder.build())
//        );
        elementInstanceArtifacts.add(elementInstanceBuilder.build());
      };

      if(isMultipleElement){
        templateInstanceArtifactBuilder.withMultiInstanceElementInstances(elementName, elementInstanceArtifacts);
      } else{
        templateInstanceArtifactBuilder.withElementInstance(elementName, elementInstanceArtifacts.get(0));
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
        //TODO: empty multiple element, should be an empty list.
        templateInstanceBuilder.withMultiInstanceElementInstances(elementName, List.of(elementInstanceBuilder.build()));
      } else{
        buildSingleEmptyElementInstance(elementName, templateSchemaArtifact, elementInstanceBuilder, "/" + elementName);
        templateInstanceBuilder.withElementInstance(elementName, elementInstanceBuilder.build());
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
        elementInstanceBuilder.withAttributeValueFieldInstances(expectedField, Collections.emptyMap());
      } else{
        var isMultiple = elementSchemaArtifact.getFieldSchemaArtifact(expectedField).isMultiple();
        buildEmptyFieldInstance(elementSchemaArtifact, expectedField, isMultiple, elementInstanceBuilder);
      }
    }

    //Add child element instances
    var childElements = elementSchemaArtifact.getElementNames();
    for (var childElement : childElements){
      if (elementSchemaArtifact.getElementSchemaArtifact(childElement).isMultiple()){
        elementInstanceBuilder
            .withMultiInstanceElementInstances(childElement,
                List.of(ElementInstanceArtifact.builder().build()));
      } else {
        buildSingleEmptyElementInstance(childElement, templateSchemaArtifact, elementInstanceBuilder, path + "/" + childElement);
      }
    }
  }

  public void buildEmptyFieldInstance(ElementSchemaArtifact elementSchemaArtifact, String field, boolean isMultiple, ElementInstanceArtifact.Builder elementInstanceArtifactBuilder) throws URISyntaxException {
    var fieldInstanceArtifactBuilder = FieldInstanceArtifact.builder();
    var inputType = elementSchemaArtifact.getFieldSchemaArtifact(field).fieldUi().inputType();
    var valueConstraints = elementSchemaArtifact.getFieldSchemaArtifact(field).valueConstraints();
    //TODO update with various type

    if (inputType.isLink()){
//      return FieldInstanceArtifact.builder().withJsonLdId(null).build();
      fieldInstanceArtifactBuilder.withJsonLdId(new URI("http://empty.com")).build();
    } else if (inputType.isNumeric()) {
      fieldInstanceArtifactBuilder
          .withJsonLdType(new URI("xsd:decimal"))
          .withJsonLdValue("null")
          .build();
    } else if (inputType.isTemporal()) {
      valueConstraints.ifPresent(constraints -> {
        var temporalDataType = constraints.asTemporalValueConstraints().temporalType();
      });
      fieldInstanceArtifactBuilder
//          .withJsonLdType(temporalDataType.toURI())
          .withJsonLdType(new URI("xsd:dateTime"))
          .withJsonLdValue("null")
          .build();
    } else {
      fieldInstanceArtifactBuilder.withJsonLdValue("null").build();
    }

    if(isMultiple){
      elementInstanceArtifactBuilder.withMultiInstanceFieldInstances(field, List.of(fieldInstanceArtifactBuilder.build()));
    } else{
      elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(field, fieldInstanceArtifactBuilder.build());
    }
  }
}
