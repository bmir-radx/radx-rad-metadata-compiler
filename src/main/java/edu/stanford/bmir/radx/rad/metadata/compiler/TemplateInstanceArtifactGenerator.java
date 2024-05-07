package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators.TextFieldGenerator;
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
  public TemplateInstanceArtifact generateTemplateArtifactInstance(Map<String, String> spreadsheetData,
                                                                   Map<String, String> spreadsheet2templatePath,
                                                                   JsonNode templateNode,
                                                                   String schemaName) throws URISyntaxException {


    //read templateContent using cedar-artifact-library
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);

    var templateInstanceArtifactBuilder = TemplateInstanceArtifact.builder();

    var elements = templateSchemaArtifact.getElementNames();
    var spreadsheetDataManager = new SpreadsheetDataManager();
    spreadsheetDataManager.groupData(spreadsheetData, spreadsheet2templatePath, templateSchemaArtifact);
    var attributeValueMap = spreadsheetDataManager.attributeValueMap;
    var elementInstanceCounts = spreadsheetDataManager.elementInstanceCounts;
    var groupedData = spreadsheetDataManager.groupedData;
    var mappedElements = elementInstanceCounts.keySet();

    //Build child element instances artifacts
    for(var childElement : elements){
      var childElementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact(childElement);
      var isChildElementMultiple = childElementSchemaArtifact.isMultiple();
      if (mappedElements.contains(childElement)){
        var childElementInstanceArtifacts = elementInstanceArtifactGenerator.generateElementInstanceWithValue(childElement, "", childElementSchemaArtifact, templateSchemaArtifact, spreadsheetData, spreadsheetDataManager);
        if(isChildElementMultiple){
          templateInstanceArtifactBuilder.withMultiInstanceElementInstances(childElement, childElementInstanceArtifacts);
        } else{
          templateInstanceArtifactBuilder.withSingleInstanceElementInstance(childElement, childElementInstanceArtifacts.get(0));
        }
      } else{ // build empty element
        if(isChildElementMultiple){
          templateInstanceArtifactBuilder.withMultiInstanceElementInstances(childElement, Collections.emptyList());
        } else{
          var elementInstanceArtifact = elementInstanceArtifactGenerator.buildSingleEmptyElementInstance(childElementSchemaArtifact, templateSchemaArtifact, "/" + childElement);
          templateInstanceArtifactBuilder.withSingleInstanceElementInstance(childElement, elementInstanceArtifact);
        }
      }
    }

    //Build child field instances artifacts
    var childFields = templateSchemaArtifact.getFieldNames();
    var buildKeywords = false;
    for(var childField : childFields) {
      var childFieldSchemaArtifact = templateSchemaArtifact.getFieldSchemaArtifact(childField);
      var childFieldType = FieldType.getFieldType(childFieldSchemaArtifact);
      var childValueConstraints = childFieldSchemaArtifact.valueConstraints();
      var isChildFieldMultiple = childFieldSchemaArtifact.isMultiple();
      var currentPath = "/" + childField;
      FieldInstanceArtifact fieldInstanceArtifact;

      if(childField.equals(TITLE.getValue())){ // Build Title field with language tag
        var textFieldInstanceGenerator = new TextFieldGenerator();
        var value = groupedData.get(currentPath).get(1).get(0);
        fieldInstanceArtifact = textFieldInstanceGenerator.buildWithLanguage(value, "en");
        templateInstanceArtifactBuilder.withSingleInstanceFieldInstance(childField, fieldInstanceArtifact);
      } else if (groupedData.containsKey(currentPath)) { // Build field instance with value
        if(isChildFieldMultiple){
          var valueSet = groupedData.get(currentPath).get(1);
          var fieldInstanceArtifactList = fieldInstanceArtifactGenerator.buildMultiFieldInstances(childFieldType, valueSet, childValueConstraints);
          templateInstanceArtifactBuilder.withMultiInstanceFieldInstances(childField, fieldInstanceArtifactList);
        } else{
          var value = groupedData.get(currentPath).get(1).get(0);
          fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(childFieldType, value, childValueConstraints);
          templateInstanceArtifactBuilder.withSingleInstanceFieldInstance(childField, fieldInstanceArtifact);
        }
      } else {
        // Special handling for keywords
        if ((childField.equals(RadxSpecificationMetadataConstant.KEYWORDS.getValue()) || childField.equals(SUBJECTS.getValue()))) {
          if (!buildKeywords) {
            var input = spreadsheetData.get(KEYWORDS.getValue());
            RadxRadPrecisionFieldHandler.processKeywords(input, templateInstanceArtifactBuilder);
            buildKeywords = true;
          }
        } else{
          // build Empty field instance
          fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildEmptyFieldInstance(childFieldType, childValueConstraints);
          if (isChildFieldMultiple) {
            templateInstanceArtifactBuilder.withMultiInstanceFieldInstances(childField, List.of(fieldInstanceArtifact));
          } else {
            templateInstanceArtifactBuilder.withSingleInstanceFieldInstance(childField, fieldInstanceArtifact);
          }
        }
      }
    }

    //generate JsonLdContext
    ContextGenerator.generateTemplateInstanceContext(templateSchemaArtifact, templateInstanceArtifactBuilder);

    //generate JsonLdId
    IdGenerator.generateTemplateId(templateInstanceArtifactBuilder);

    //with IsBasedOn
    var id = templateSchemaArtifact.jsonLdId();
    if(id.isPresent()) {
      templateInstanceArtifactBuilder.withIsBasedOn(id.get());
    } else {
      templateInstanceArtifactBuilder.withIsBasedOn(new URI(IS_BASED_ON.getValue()));
    }

    //with schemaName
    if(schemaName != null){
      templateInstanceArtifactBuilder.withName(schemaName);
    } else{
      templateInstanceArtifactBuilder.withName(SCHEMA_NAME.getValue());
    }

    return templateInstanceArtifactBuilder
        .withDescription(SCHEMA_DESCRIPTION.getValue())
        .build();
  }
}
