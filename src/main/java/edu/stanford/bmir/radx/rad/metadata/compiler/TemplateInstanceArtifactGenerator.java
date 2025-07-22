package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.*;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxSpecificationMetadataConstant.*;
import static edu.stanford.bmir.radx.rad.metadata.compiler.VersionSpecial.FIRST_VERSION_SPECIAL;

public class TemplateInstanceArtifactGenerator {
  private boolean isFirstVersionTemplate;
  private Map<String, List<String>> attributeValueMap = new HashMap<>();
  private Map<String, String> groupedData = new HashMap<>();
  private Map<String, Integer> fieldsCounts = new HashMap<>();
  private Map<String, Integer> elementCounts = new HashMap<>();

  public TemplateInstanceArtifact generateTemplateArtifactInstance(Map<String, String> csvData,
                                                                   Map<String, String> csv2templatePath,
                                                                   JsonNode templateNode,
                                                                   String schemaName) throws URISyntaxException {


    //read templateContent using cedar-artifact-library
    JsonArtifactReader jsonSchemaArtifactReader = new JsonArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    isFirstVersionTemplate = TemplateVersion.isFirstVersion(templateSchemaArtifact.jsonLdId());

    var templateInstanceArtifactBuilder = TemplateInstanceArtifact.builder();

    var csvDataManager = new CsvDataManager();
    csvDataManager.groupData(csvData, csv2templatePath, templateSchemaArtifact);
    attributeValueMap = csvDataManager.getAttributeValueMap();
    fieldsCounts = csvDataManager.getFieldsCounts();
    elementCounts = csvDataManager.getElementCounts();
    groupedData = csvDataManager.getGroupedData();

    var elementInstanceArtifactGenerator = new ElementInstanceArtifactGenerator(isFirstVersionTemplate, csvData, attributeValueMap, groupedData, fieldsCounts, elementCounts, templateSchemaArtifact);

    //Build child field instances artifacts
    buildChildFieldInstances("", csvData, templateSchemaArtifact, templateInstanceArtifactBuilder);

    //build child element instances artifacts
    buildChildElementInstance(csvData, templateSchemaArtifact, templateInstanceArtifactBuilder, elementInstanceArtifactGenerator);

    //generate JsonLdContext
    ContextGenerator.generateTemplateInstanceContext(templateSchemaArtifact, templateInstanceArtifactBuilder);

    //generate JsonLdId
    IdGenerator.generateTemplateId(templateInstanceArtifactBuilder);

    //with IsBasedOn
    var id = templateSchemaArtifact.jsonLdId();
    if (id.isPresent()) {
      templateInstanceArtifactBuilder.withIsBasedOn(id.get());
    } else {
      templateInstanceArtifactBuilder.withIsBasedOn(new URI(IS_BASED_ON.getValue()));
    }

    //with schemaName
    if (schemaName != null) {
      templateInstanceArtifactBuilder.withName(schemaName);
    } else {
      templateInstanceArtifactBuilder.withName(SCHEMA_NAME.getValue());
    }

    return templateInstanceArtifactBuilder
        .withDescription(SCHEMA_DESCRIPTION.getValue())
        .build();
  }

  private void buildChildFieldInstances(String parentPath,
                                        Map<String, String> csvData,
                                        TemplateSchemaArtifact templateSchemaArtifact,
                                        TemplateInstanceArtifact.Builder builder){
    var fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator();
    var childFields = templateSchemaArtifact.getChildKeys();

    for(var childField: childFields){
      var currentPath = "/" + childField;
      var normalizedPath = normalizePath(currentPath);
      var childFieldArtifactSchema = templateSchemaArtifact.getFieldSchemaArtifact(childField);
      var childFieldValueConstraint = childFieldArtifactSchema.valueConstraints();
      var childFieldType = FieldType.getFieldType(childFieldArtifactSchema);
      var isMultipleField = childFieldArtifactSchema.isMultiple();
      //Check if the child field is an attribute value type, which need to be build differently
      if(AttributeValueFieldUtil.isAttributeValue(templateSchemaArtifact, normalizedPath)){
        if(attributeValueMap.containsKey(normalizedPath)){
          var avInstances = fieldInstanceArtifactGenerator.buildAttributeValueField(csvData, attributeValueMap.get(normalizedPath));
          builder.withAttributeValueFieldGroup(childField, avInstances);
        }
        else{
          builder.withAttributeValueFieldGroup(childField, new LinkedHashMap<>());
        }
      }
      else{
        //todo special handle for template2.0: Title, keywords and Subjects.
        if(fieldsCounts.containsKey(currentPath)){
          if(isMultipleField){
            var childFieldInstanceNumber = fieldsCounts.get(currentPath);
            var values = getInstancesValues(currentPath, childFieldInstanceNumber);
            var fieldInstanceArtifacts = fieldInstanceArtifactGenerator.buildMultipleInstancesWithValues(childFieldType, values, childFieldValueConstraint);
            builder.withMultiInstanceFieldInstances(childField, fieldInstanceArtifacts);
          }
          else{
            var value = getSingleInstanceValue(parentPath);
            var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(childFieldType, value, childFieldValueConstraint);
            builder.withSingleInstanceFieldInstance(childField, fieldInstanceArtifact);
          }
        }
        else{
          if(isMultipleField){
            var fieldInstanceArtifacts = fieldInstanceArtifactGenerator.buildMultipleEmptyInstances(childFieldType, childFieldValueConstraint);
            builder.withMultiInstanceFieldInstances(childField, fieldInstanceArtifacts);
          }
          else{
            var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildSingleEmptyInstance(childFieldType, childFieldValueConstraint);
            builder.withSingleInstanceFieldInstance(childField, fieldInstanceArtifact);
          }
        }
      }
    }
  }

  private void buildChildElementInstance(Map<String,String> csvData,
                                         TemplateSchemaArtifact templateSchemaArtifact,
                                         TemplateInstanceArtifact.Builder builder,
                                         ElementInstanceArtifactGenerator elementInstanceArtifactGenerator) throws URISyntaxException {
    var elements = templateSchemaArtifact.getChildKeys();
    for (var childElement : elements) {
      var currentPath = "/" + childElement;
      if (FIRST_VERSION_SPECIAL.isFirstVersionSpecialty(childElement) && isFirstVersionTemplate) {
        //generate Data File Subjects and Data File Related Resources element instances
        RadxRadPrecisionFieldHandler.firstVersionElementPatch(childElement, fieldsCounts, groupedData, templateSchemaArtifact, builder);
      } else {
        var childElementArtifactSchema = templateSchemaArtifact.getElementSchemaArtifact(childElement);
        var isMultiple = childElementArtifactSchema.isMultiple();
        if (elementCounts.containsKey(currentPath)) {
          if(isMultiple){
            var childElementInstances = elementInstanceArtifactGenerator.buildMultipleInstancesWithValues(childElementArtifactSchema, currentPath);
            builder.withMultiInstanceElementInstances(childElement, childElementInstances);
          }
          else{
            var childElementInstance = elementInstanceArtifactGenerator.buildSingleInstanceWithValue(childElementArtifactSchema, currentPath, 0);
            builder.withSingleInstanceElementInstance(childElement, childElementInstance);
          }
        }
        else{
          if(isMultiple){
            var childElementInstances = elementInstanceArtifactGenerator.buildMultipleEmptyInstances();
            builder.withMultiInstanceElementInstances(childElement, childElementInstances);
          }
          else{
            var childElementInstance = elementInstanceArtifactGenerator. buildSingleEmptyInstance(childElementArtifactSchema, currentPath);
            builder.withSingleInstanceElementInstance(childElement, childElementInstance);
          }
        }
      }
    }
  }

  private String normalizePath(String path){
    return path.replace("\\[\\d+\\]", "");
  }

  private List<String> getInstancesValues(String path, Integer count){
    var values = new ArrayList<String>();
    for(int i=0; i<count; i++){
      var currentPath = path + "[" + i + "]";
      values.add(groupedData.get(currentPath));
    }
    return values;
  }

  private String getSingleInstanceValue(String path){
    var currentPath = path + "[0]";
    return groupedData.get(currentPath);
  }
}
