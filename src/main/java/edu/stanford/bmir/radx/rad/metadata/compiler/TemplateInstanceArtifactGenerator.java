package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.*;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxRadFieldsConstant.KEYWORDS;
import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxSpecificationMetadataConstant.*;

public class TemplateInstanceArtifactGenerator {

  private final ElementInstanceArtifactGenerator elementInstanceArtifactGenerator = new ElementInstanceArtifactGenerator();
  private final FieldInstanceArtifactGenerator fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator();

  public TemplateInstanceArtifact generateTemplateArtifactInstance(Map<String, String> spreadsheetData, Map<String, String> spreadsheet2templatePath, JsonNode templateNode) throws URISyntaxException {
    //read templateContent using cedar-artifact-library
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);

    var templateInstanceArtifactBuilder = TemplateInstanceArtifact.builder();

    var elements = templateSchemaArtifact.getElementNames();
    SpreadsheetDataManager.groupData(spreadsheetData, spreadsheet2templatePath, templateSchemaArtifact);
    var attributeValueMap = SpreadsheetDataManager.attributeValueMap;
    var elementInstanceCounts = SpreadsheetDataManager.elementInstanceCounts;
    var groupedData = SpreadsheetDataManager.groupedData;
    var mappedElements = elementInstanceCounts.keySet();

    //Build child element instances artifacts
    for(var childElement : elements){
      if(childElement.equals(DATA_FILE_SUBJECTS.getValue())){
        //generate Data File Subjects element
        RadxRadPrecisionFieldHandler.addDataFileSubjectsElement(spreadsheetData.get(KEYWORDS.getValue()), templateSchemaArtifact, templateInstanceArtifactBuilder);
      } else{
        var childElementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact(childElement);
        var isChildElementMultiple = childElementSchemaArtifact.isMultiple();
        if (mappedElements.contains(childElement)){
          var childElementInstanceArtifacts = elementInstanceArtifactGenerator.generateElementInstanceWithValue(childElement, attributeValueMap, groupedData, elementInstanceCounts, childElementSchemaArtifact, templateSchemaArtifact, spreadsheetData, "");
          if(isChildElementMultiple){
            templateInstanceArtifactBuilder.withMultiInstanceElementInstances(childElement, childElementInstanceArtifacts);
          } else{
            templateInstanceArtifactBuilder.withSingleInstanceElementInstance(childElement, childElementInstanceArtifacts.get(0));
          }
        } else{ // build empty element
          if(isChildElementMultiple){
            templateInstanceArtifactBuilder.withEmptyMultiInstanceElementInstances(childElement);
          } else{
            var elementInstanceArtifact = elementInstanceArtifactGenerator.buildSingleEmptyElementInstance(childElement, childElementSchemaArtifact, templateSchemaArtifact, "/" + childElement);
            templateInstanceArtifactBuilder.withSingleInstanceElementInstance(childElement, elementInstanceArtifact);
          }
        }
      }
    }

    //Build chile field instances artifacts
    var childFields = templateSchemaArtifact.getFieldNames();
    for(var childField : childFields) {
      var childFieldSchemaArtifact = templateSchemaArtifact.getFieldSchemaArtifact(childField);
      var childFieldType = FieldType.getFieldType(childFieldSchemaArtifact);
      var childValueConstraints = childFieldSchemaArtifact.valueConstraints();
      var isChildFieldMultiple = childFieldSchemaArtifact.isMultiple();
      var currentPath = "/" + childField;
      FieldInstanceArtifact fieldInstanceArtifact;
      if (groupedData.containsKey(currentPath)) { // Build field instance with value
        //TODO handling multiple instances if needed
        var value = groupedData.get(currentPath).get(1);
        fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(childFieldType, value, childValueConstraints);
      } else { // build Empty field instance
        fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildEmptyFieldInstance(childFieldType);
      }

      if (isChildFieldMultiple) {
        templateInstanceArtifactBuilder.withMultiInstanceFieldInstances(childField, List.of(fieldInstanceArtifact));
      } else {
        templateInstanceArtifactBuilder.withSingleInstanceFieldInstance(childField, fieldInstanceArtifact);
      }
    }

    //generate JsonLdContext
    ContextGenerator.generateTemplateInstanceContext(templateSchemaArtifact, templateInstanceArtifactBuilder);

    //generate JsonLdId
    IdGenerator.generateTemplateId(templateInstanceArtifactBuilder);

    return templateInstanceArtifactBuilder
        .withIsBasedOn(new URI(IS_BASED_ON.getValue()))
        .withDescription(SCHEMA_DESCRIPTION.getValue())
        .withName(SCHEMA_NAME.getValue())
        .build();
  }
}
