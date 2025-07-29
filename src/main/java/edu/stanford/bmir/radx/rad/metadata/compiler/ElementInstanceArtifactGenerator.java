package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.*;

import java.net.URISyntaxException;
import java.util.*;

public class ElementInstanceArtifactGenerator {
  private final boolean isFirstVersiontempalte;
  private final Map<String, String> csvData;
  private final Map<String, List<String>> attributeValueMap;
  private final Map<String, String> groupedData;
  private final Map<String, Integer> fieldsCounts;
  private final Map<String, Integer> elementCounts;
  private final TemplateSchemaArtifact templateSchemaArtifact;

  private final FieldInstanceArtifactGenerator fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator();

  public ElementInstanceArtifactGenerator(boolean isFirstVersiontempalte,
                                          Map<String, String> csvData,
                                          Map<String, List<String>> attributeValueMap,
                                          Map<String, String> groupedData,
                                          Map<String, Integer> fieldsCounts,
                                          Map<String, Integer> elementCounts,
                                          TemplateSchemaArtifact templateSchemaArtifact) {
    this.isFirstVersiontempalte = isFirstVersiontempalte;
    this.csvData = csvData;
    this.attributeValueMap = attributeValueMap;
    this.groupedData = groupedData;
    this.fieldsCounts = fieldsCounts;
    this.elementCounts = elementCounts;
    this.templateSchemaArtifact = templateSchemaArtifact;
  }

  public List<ElementInstanceArtifact> buildMultipleInstancesWithValues(ElementSchemaArtifact elementSchemaArtifact,
                                                                        String path) throws URISyntaxException {

    var currentElementInstanceNumber = elementCounts.get(path);
    var elementInstanceArtifacts = new ArrayList<ElementInstanceArtifact>();

    for(int i=0; i<currentElementInstanceNumber; i++){
      var elementInstanceArtifact = buildSingleInstanceWithValue(elementSchemaArtifact, path, i);
      elementInstanceArtifacts.add(elementInstanceArtifact);
    }
    return elementInstanceArtifacts;
  }

  public ElementInstanceArtifact buildSingleInstanceWithValue(ElementSchemaArtifact elementSchemaArtifact,
                                                              String path,
                                                              Integer elementIndex) throws URISyntaxException {
    var builder = ElementInstanceArtifact.builder();
    var currentPath = path + "[" + elementIndex + "]";

    //build field instances
    buildChildFieldInstances(currentPath, elementSchemaArtifact, builder);
    //build nested element instances
    buildChildElementInstances(currentPath, elementSchemaArtifact, builder);
    //Add JsonLdContext for each elementInstance
    ContextGenerator.generateElementInstanceContext(
        elementSchemaArtifact,
        builder);
    //Add @id
    IdGenerator.generateElementId(builder);
    return builder.build();
  }

  private void buildChildFieldInstances(String parentElementPath,
                                        ElementSchemaArtifact elementSchemaArtifact,
                                        ElementInstanceArtifact.Builder builder){
    var childFields = elementSchemaArtifact.getChildKeys();
    for(var childField: childFields){
      var currentPath = parentElementPath + "/" + childField;
      var normalizedPath = normalizePath(currentPath);
      var childFieldArtifactSchema = elementSchemaArtifact.getFieldSchemaArtifact(childField);
      var childFieldValueConstraint = childFieldArtifactSchema.valueConstraints();
      var childFieldType = FieldType.getFieldType(childFieldArtifactSchema);
      var isMultipleField = childFieldArtifactSchema.isMultiple();
      //Check if the child field is an attribute value type, which need to be build differently
      if(AttributeValueFieldUtil.isAttributeValue(templateSchemaArtifact, normalizedPath)){
        if(attributeValueMap.containsKey(normalizedPath)){
          var avInstances = buildAttributeValueInstances(normalizedPath);
          builder.withAttributeValueFieldGroup(childField, avInstances);
        }
        else{
          builder.withAttributeValueFieldGroup(childField, new LinkedHashMap<>());
        }
      }
      else{
        if(fieldsCounts.containsKey(currentPath)){
          if(isMultipleField){
            var childFieldInstanceNumber = fieldsCounts.get(currentPath);
            var values = getInstancesValues(currentPath, childFieldInstanceNumber);
            var fieldInstanceArtifacts = fieldInstanceArtifactGenerator.buildMultipleInstancesWithValues(childFieldType, values, childFieldValueConstraint);
            builder.withMultiInstanceFieldInstances(childField, fieldInstanceArtifacts);
          }
          else{
            var value = getSingleInstanceValue(currentPath);
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
            if(isFirstVersiontempalte){
              RadxRadPrecisionFieldHandler.firstVersionFieldPath(currentPath, elementCounts, groupedData, childFieldArtifactSchema, builder);
            }
            if(!builder.build().singleInstanceFieldInstances().containsKey(childField)){
              var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildSingleEmptyInstance(childFieldType, childFieldValueConstraint);
              builder.withSingleInstanceFieldInstance(childField, fieldInstanceArtifact);
            }
          }
        }
      }
    }
  }

  private void buildChildElementInstances(String parentElementPath,
                                          ElementSchemaArtifact elementSchemaArtifact,
                                          ElementInstanceArtifact.Builder builder) throws URISyntaxException {
    var childElements = elementSchemaArtifact.getElementKeys();
    for(var childElement:childElements){
      var currentChildElementPath = parentElementPath + "/" + childElement;
      var childElementArtifactSchema = elementSchemaArtifact.getElementSchemaArtifact(childElement);
      var isMultiple = childElementArtifactSchema.isMultiple();
      if(elementCounts.containsKey(currentChildElementPath)){
        if(isMultiple){
          var childElementInstances = buildMultipleInstancesWithValues(childElementArtifactSchema, currentChildElementPath);
          builder.withMultiInstanceElementInstances(childElement, childElementInstances);
        }
        else{
          var childElementInstance = buildSingleInstanceWithValue(childElementArtifactSchema, currentChildElementPath, 0);
          builder.withSingleInstanceElementInstance(childElement, childElementInstance);
        }
      }
      else{
        if(isMultiple){
          var childElementInstances = buildMultipleEmptyInstances();
          builder.withMultiInstanceElementInstances(childElement, childElementInstances);
        }
        else{
          var childElementInstance = buildSingleEmptyInstance(childElementArtifactSchema, currentChildElementPath);
          builder.withSingleInstanceElementInstance(childElement, childElementInstance);
        }
      }
    }
  }

  public ElementInstanceArtifact buildSingleEmptyInstance(ElementSchemaArtifact elementSchemaArtifact,
                                               String path) throws URISyntaxException {
    var elementInstanceBuilder = ElementInstanceArtifact.builder();
    var childFields = elementSchemaArtifact.getChildKeys();

    //Add context
    ContextGenerator.generateElementInstanceContext(elementSchemaArtifact, elementInstanceBuilder);

    //Add @id
    IdGenerator.generateElementId(elementInstanceBuilder);

    //Add child field instances
    buildEmptyChildFieldInstances(path, elementSchemaArtifact, elementInstanceBuilder);

    //Add child element instances
    buildEmptyChildElementInstances(path, elementSchemaArtifact, elementInstanceBuilder);

    return elementInstanceBuilder.build();
  }

  private void buildEmptyChildFieldInstances(String path,
                                             ElementSchemaArtifact elementSchemaArtifact,
                                             ElementInstanceArtifact.Builder builder){
    var childFields = elementSchemaArtifact.getChildKeys();
    for(var childField : childFields){
      var specificationPath = path + "/" + childField;
      if(AttributeValueFieldUtil.isAttributeValue(templateSchemaArtifact, specificationPath)){
        builder.withAttributeValueFieldGroup(childField, new LinkedHashMap<>());
      } else{
        var fieldSchemaArtifact = elementSchemaArtifact.getFieldSchemaArtifact(childField);
        var inputType = FieldType.getFieldType(fieldSchemaArtifact);
        var isMultiple = fieldSchemaArtifact.isMultiple();

        if(isMultiple){
          var fieldInstanceArtifacts = fieldInstanceArtifactGenerator.buildMultipleEmptyInstances(inputType, fieldSchemaArtifact.valueConstraints());
          builder.withMultiInstanceFieldInstances(childField, fieldInstanceArtifacts);
        } else{
          if(isFirstVersiontempalte){
            RadxRadPrecisionFieldHandler.firstVersionFieldPath(specificationPath, elementCounts, groupedData, fieldSchemaArtifact, builder);
          }
          if(!builder.build().singleInstanceFieldInstances().containsKey(childField)){
              var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildSingleEmptyInstance(inputType, fieldSchemaArtifact.valueConstraints());
              builder.withSingleInstanceFieldInstance(childField, fieldInstanceArtifact);
          }
        }
      }
    }
  }

  private void buildEmptyChildElementInstances(String path,
                                               ElementSchemaArtifact elementSchemaArtifact,
                                               ElementInstanceArtifact.Builder builder) throws URISyntaxException {
    var childElements = elementSchemaArtifact.getChildKeys();
    for (var childElement : childElements){
      var childElementSchemaArtifact = elementSchemaArtifact.getElementSchemaArtifact(childElement);
      if (childElementSchemaArtifact.isMultiple()){
        builder.withMultiInstanceElementInstances(childElement, Collections.emptyList());
      } else {
        var emptyElementInstanceArtifact = buildSingleEmptyInstance(childElementSchemaArtifact, path + "/" + childElement);
        builder.withSingleInstanceElementInstance(childElement, emptyElementInstanceArtifact);
      }
    }
  }

  public List<ElementInstanceArtifact> buildMultipleEmptyInstances(){
    return Collections.emptyList();
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

  private LinkedHashMap<String, FieldInstanceArtifact> buildAttributeValueInstances(String path){
    var csvFields = attributeValueMap.get(path);
    return fieldInstanceArtifactGenerator.buildAttributeValueField(csvData, csvFields);
  }

  private String normalizePath(String path){
    return path.replaceAll("\\[\\d+\\]", "");
  }
}
