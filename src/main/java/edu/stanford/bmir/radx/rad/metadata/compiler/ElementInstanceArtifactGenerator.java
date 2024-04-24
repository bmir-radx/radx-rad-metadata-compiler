package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.*;

import java.net.URISyntaxException;
import java.util.*;

public class ElementInstanceArtifactGenerator {
  private final FieldInstanceArtifactGenerator fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator();

  public List<ElementInstanceArtifact> generateElementInstanceWithValue(String currentElement, String path,
                                                               ElementSchemaArtifact elementSchemaArtifact,
                                                               TemplateSchemaArtifact templateSchemaArtifact,
                                                               Map<String, String> spreadsheetData) throws URISyntaxException {

    var attributeValueMap = SpreadsheetDataManager.attributeValueMap;
    var elementInstanceCounts = SpreadsheetDataManager.elementInstanceCounts;
    var groupedData = SpreadsheetDataManager.groupedData;
    var childFields = elementSchemaArtifact.getFieldNames();
    var instanceCount = elementInstanceCounts.get(currentElement);

    List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();

    for(int i = 1; i <= instanceCount; i++) {
      var elementInstanceBuilder = ElementInstanceArtifact.builder();

      for (var expectedField : childFields) {
        var currentPath = path + "/" + currentElement + "/" + expectedField;
        var fieldSchemaArtifact = elementSchemaArtifact.getFieldSchemaArtifact(expectedField);
        var expectedFieldValueConstraint = fieldSchemaArtifact.valueConstraints();
        var expectedFieldType = FieldType.getFieldType(fieldSchemaArtifact);
        var isMultipleField = fieldSchemaArtifact.isMultiple();

        //check if field is an attribute value type
        //if yes, build attribute value fields, otherwise, build regular fields
        if (attributeValueMap.containsKey(currentPath)) {
          var spreadsheetFields = attributeValueMap.get(currentPath);
          var attributeValueFieldInstances = fieldInstanceArtifactGenerator.buildAttributeValueField(spreadsheetData, spreadsheetFields);
          elementInstanceBuilder.withAttributeValueFieldGroup(expectedField, attributeValueFieldInstances);
        } else {
          // if the expectedField in the template has the mapping field in the spreadsheet, then need to retrieve data from spreadsheet
          // otherwise, build an empty fieldArtifactInstance
          FieldInstanceArtifact fieldInstanceArtifact;
          if (groupedData.containsKey(currentPath)) {
            if(isMultipleField){
              // build field instance one by one, explicitly for creator/contributor ids
              var valueSet = groupedData.get(currentPath).get(i);
              var fieldInstanceArtifactList = new ArrayList<FieldInstanceArtifact>();
              for(var j=0; j < valueSet.size(); j ++){
                var currentValue = valueSet.get(j);
                fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(expectedFieldType, currentValue, expectedFieldValueConstraint);
                fieldInstanceArtifactList.add(fieldInstanceArtifact);
              }
              elementInstanceBuilder.withMultiInstanceFieldInstances(expectedField, fieldInstanceArtifactList);

            } else{
              var value = groupedData.get(currentPath).get(i).get(0);
              fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(expectedFieldType, value, expectedFieldValueConstraint);
              elementInstanceBuilder.withSingleInstanceFieldInstance(expectedField, fieldInstanceArtifact);
            }
          } else {
            if (isMultipleField) {
              fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildEmptyFieldInstance(expectedFieldType);
              elementInstanceBuilder.withMultiInstanceFieldInstances(expectedField, List.of(fieldInstanceArtifact));
            } else {
              //Add values to RADx-rad specific controlled terms fields or add an empty field entry
              RadxRadPrecisionFieldHandler.addSpecificFields(elementInstanceBuilder, currentElement, expectedField, elementSchemaArtifact, groupedData, elementInstanceCounts, i);
            }
          }
        }
      }

      //Build nested child element
      buildWithElementInstances(spreadsheetData, elementSchemaArtifact, templateSchemaArtifact, elementInstanceBuilder, path + "/" + currentElement);

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

  private void buildWithElementInstances(Map<String, String> spreadsheetData,
                                         ElementSchemaArtifact currentElementSchemaArtifact,
                                         TemplateSchemaArtifact templateSchemaArtifact,
                                         ElementInstanceArtifact.Builder elementInstanceBuilder,
                                         String path) throws URISyntaxException {
    var childElements = currentElementSchemaArtifact.getElementNames();
    var elementInstanceCounts = SpreadsheetDataManager.elementInstanceCounts;
    var mappedElements = elementInstanceCounts.keySet();
    for (var childElement : childElements) {
      var childElementSchemaArtifact = currentElementSchemaArtifact.getElementSchemaArtifact(childElement);
      var isChildElementMultiple = childElementSchemaArtifact.isMultiple();
      //build element that has mapping in radx rad spreadsheet
      if (mappedElements.contains(childElement)){
        var childElementInstanceArtifacts = generateElementInstanceWithValue(childElement, path, childElementSchemaArtifact, templateSchemaArtifact, spreadsheetData);
        if(isChildElementMultiple){
          elementInstanceBuilder.withMultiInstanceElementInstances(childElement, childElementInstanceArtifacts);
        } else{
          elementInstanceBuilder.withSingleInstanceElementInstance(childElement, childElementInstanceArtifacts.get(0));
        }
      } else{ // build empty element
        if(isChildElementMultiple){
          elementInstanceBuilder.withMultiInstanceElementInstances(childElement, Collections.emptyList());
        } else{
          buildSingleEmptyElementInstance(childElementSchemaArtifact, templateSchemaArtifact, path + "/" + childElement);
        }
      }
    }
  }

  public ElementInstanceArtifact buildSingleEmptyElementInstance(ElementSchemaArtifact elementSchemaArtifact,
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
        var inputType = FieldType.getFieldType(fieldSchemaArtifact);
        var isMultiple = fieldSchemaArtifact.isMultiple();
        var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildEmptyFieldInstance(inputType);
        if(isMultiple){
          elementInstanceBuilder.withMultiInstanceFieldInstances(expectedField, Collections.emptyList());
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
        elementInstanceBuilder.withMultiInstanceElementInstances(childElement, Collections.emptyList());
      } else {
        var emptyElementInstanceArtifact = buildSingleEmptyElementInstance(childElementSchemaArtifact, templateSchemaArtifact, path + "/" + childElement);
        elementInstanceBuilder.withSingleInstanceElementInstance(childElement, emptyElementInstanceArtifact);
      }
    }
    return elementInstanceBuilder.build();
  }
}
