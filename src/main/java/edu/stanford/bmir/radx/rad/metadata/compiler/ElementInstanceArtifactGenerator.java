package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.*;

import java.net.URISyntaxException;
import java.util.*;

public class ElementInstanceArtifactGenerator {
  private final FieldInstanceArtifactGenerator fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator();

  public List<ElementInstanceArtifact> generateElementInstanceWithValue(String currentElement,
                                                               Map<String, List<String>> attributeValueMap,
                                                               Map<String, Map<Integer, String>> groupedData,
                                                               Map<String, Integer> elementInstanceCounts,
                                                               ElementSchemaArtifact elementSchemaArtifact,
                                                               TemplateSchemaArtifact templateSchemaArtifact,
                                                               Map<String, String> spreadsheetData,
                                                                String path) throws URISyntaxException {

    var childFields = elementSchemaArtifact.getFieldNames();
    var instanceCount = elementInstanceCounts.get(currentElement);

    List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();

    for(int i = 1; i <= instanceCount; i++) {
      var elementInstanceBuilder = ElementInstanceArtifact.builder();

      for (var expectedField : childFields) {
        var currentPath = path + "/" + currentElement + "/" + expectedField;
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
        if (attributeValueMap.containsKey(currentPath)) {
          var spreadsheetFields = attributeValueMap.get(currentPath);
          Map<String, FieldInstanceArtifact> attributeValueFieldInstances = new HashMap<>();
          if (spreadsheetFields != null) {
            for (var spreadsheetField : spreadsheetFields) {
              var spreadsheetValue = spreadsheetData.get(spreadsheetField);
              if (spreadsheetValue != null && !spreadsheetValue.equals("")) {
                attributeValueFieldInstances.put(spreadsheetField,
                    FieldInstanceArtifact.textFieldInstanceBuilder().withValue(spreadsheetValue).build());
              }
            }
          }
          elementInstanceBuilder.withAttributeValueFieldGroup(expectedField, attributeValueFieldInstances);

        } else {
          // if the expectedField in the template has the mapping field in the spreadsheet, then need to retrieve data from spreadsheet
          // otherwise, build an empty fieldArtifactInstance
          FieldInstanceArtifact fieldInstanceArtifact;
          if (groupedData.containsKey(currentPath)) {
            var value = groupedData.get(currentPath).get(i);
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
              RadxRadPrecisionFieldHandler.addSpecificControlledTerms(elementInstanceBuilder, currentElement, expectedField, elementSchemaArtifact, currentPath, groupedData, elementInstanceCounts, i);
            }
          }
        }
      }

      //Build nested child element
      buildWithElementInstances(attributeValueMap, groupedData, spreadsheetData, elementInstanceCounts,  elementSchemaArtifact, templateSchemaArtifact, elementInstanceBuilder, path + "/" + currentElement);

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

  private void buildWithElementInstances(Map<String, List<String>> attributeValueMap,
                                         Map<String, Map<Integer, String>> groupedData,
                                         Map<String, String> spreadsheetData,
                                         Map<String, Integer> elementInstanceCounts,
                                         ElementSchemaArtifact currentElementSchemaArtifact,
                                         TemplateSchemaArtifact templateSchemaArtifact,
                                         ElementInstanceArtifact.Builder elementInstanceBuilder,
                                         String path) throws URISyntaxException {
    var childElements = currentElementSchemaArtifact.getElementNames();
    var mappedElements = elementInstanceCounts.keySet();
    for (var childElement : childElements) {
      var childElementSchemaArtifact = currentElementSchemaArtifact.getElementSchemaArtifact(childElement);
      var isChildElementMultiple = childElementSchemaArtifact.isMultiple();
      //build element that has mapping in radx rad spreadsheet
      if (mappedElements.contains(childElement)){
        var childElementInstanceArtifacts = generateElementInstanceWithValue(childElement, attributeValueMap, groupedData, elementInstanceCounts, childElementSchemaArtifact, templateSchemaArtifact, spreadsheetData, path);
        if(isChildElementMultiple){
          elementInstanceBuilder.withMultiInstanceElementInstances(childElement, childElementInstanceArtifacts);
        } else{
          elementInstanceBuilder.withSingleInstanceElementInstance(childElement, childElementInstanceArtifacts.get(0));
        }
      } else{ // build empty element
        if(isChildElementMultiple){
          elementInstanceBuilder.withEmptyMultiInstanceElementInstances(childElement);
        } else{
          buildSingleEmptyElementInstance(childElement, childElementSchemaArtifact, templateSchemaArtifact, path + "/" + childElement);
        }
      }
    }
  }

  public ElementInstanceArtifact buildSingleEmptyElementInstance(String elementName,
                                               ElementSchemaArtifact elementSchemaArtifact,
                                               TemplateSchemaArtifact templateSchemaArtifact,
                                               String path) throws URISyntaxException {
    var elementInstanceBuilder = ElementInstanceArtifact.builder();
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
      var childElementSchemaArtifact = elementSchemaArtifact.getElementSchemaArtifact(childElement);
      if (childElementSchemaArtifact.isMultiple()){
        elementInstanceBuilder.withEmptyMultiInstanceElementInstances(childElement);
      } else {
        var emptyElementInstanceArtifact = buildSingleEmptyElementInstance(childElement, childElementSchemaArtifact, templateSchemaArtifact, path + "/" + childElement);
        elementInstanceBuilder.withSingleInstanceElementInstance(childElement, emptyElementInstanceArtifact);
      }
    }
    return elementInstanceBuilder.build();
  }
}
