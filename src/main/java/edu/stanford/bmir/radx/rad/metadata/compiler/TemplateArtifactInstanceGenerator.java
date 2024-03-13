package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.*;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxSpecificationMetadataConstant.*;

public class TemplateArtifactInstanceGenerator {
  private final Pattern FIELD_PATTERN = Pattern.compile("^(?!study_include_prospective_or_retrospective_human_samples)(.+?)_?(\\d*)$");
  private final ArtifactInstanceBuilder artifactInstanceBuilder = new ArtifactInstanceBuilder();
  private final EmptyArtifactChecker emptyArtifactChecker = new EmptyArtifactChecker();

  public TemplateInstanceArtifact generateTemplateArtifactInstance(Map<String, String> spreadsheetData, Map<String, FieldArtifact> spreadsheet2template, JsonNode templateNode) throws URISyntaxException {
    //read templateContent using cedar-artifact-library
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    var expectedElements = templateSchemaArtifact.getElementNames();

    var groupedData = groupData(spreadsheetData, spreadsheet2template, templateSchemaArtifact);

    //generate elements that have values in the spreadsheet
    var templateInstanceArtifactBuilder = TemplateInstanceArtifact.builder();
    artifactInstanceBuilder.buildElementInstancesWithValues(groupedData, templateSchemaArtifact, templateInstanceArtifactBuilder, spreadsheetData);

    //generate elements that does not contained in the spreadsheet
    var notPresentElements = getNotPresentElementsSet(groupedData, expectedElements);
    artifactInstanceBuilder.buildEmptyElementInstances(notPresentElements, templateSchemaArtifact, templateInstanceArtifactBuilder);

    //generate JsonLdContext
    ContextGenerator.generateTemplateInstanceContext(templateSchemaArtifact, templateInstanceArtifactBuilder);

    //generate JsonLdId
    IdGenerator.generateTemplateId(templateInstanceArtifactBuilder);

    return templateInstanceArtifactBuilder
        .withIsBasedOn(new URI(IS_BASED_ON.getField()))
        .withDescription(SCHEMA_DESCRIPTION.getField())
        .withName(SCHEMA_NAME.getField())
        .build();
  }

  /***
   * Group spreadsheet data in the following format:
   * {
   *   Element:{
   *     index{
   *       field1: [value],
   *       field2: [value],
   *       filed3(attribute-value): [spreadsheetField1, spreadsheetField2]
   *     }
   *   }
   * }
   * If field is attribute-value as field3, the list of values are keys of kay-value pairs.
   * @param spreadsheetData
   * @param spreadsheet2template
   * @return
   */
  private Map<String, Map<Integer, Map<String, List<String>>>> groupData(
    Map<String, String> spreadsheetData,
    Map<String, FieldArtifact> spreadsheet2template,
    TemplateSchemaArtifact templateSchemaArtifact){

    Map<String, Map<Integer, Map<String, List<String>>>> groupedData = new HashMap<>();

    for (Map.Entry<String, String> entry : spreadsheetData.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      Matcher matcher = FIELD_PATTERN.matcher(key);
      if (matcher.find()) {
        String spreadsheetField = matcher.group(1);
        String indexStr = matcher.group(2);
        Integer index = indexStr.isEmpty() ? 1 : Integer.parseInt(indexStr);

        var element = spreadsheet2template.get(spreadsheetField).element();
        var field = spreadsheet2template.get(spreadsheetField).field();
        var specificationPath = "/" + element + "/" + field;

        if(AttributeValueFieldUtil.isAttributeValue(templateSchemaArtifact, specificationPath)){
          groupedData.computeIfAbsent(element, k -> new HashMap<>())
              .computeIfAbsent(1, k -> new HashMap<>())
              .computeIfAbsent(field, k -> new ArrayList<>())
              .add(key);
        } else{
          groupedData.computeIfAbsent(element, k -> new HashMap<>())
              .computeIfAbsent(index, k -> new HashMap<>())
              .computeIfAbsent(field, k -> new ArrayList<>())
              .add(value);
        }
      }
    };
    return groupedData;
  }

  private Set<String> getNotPresentElementsSet(Map<String, Map<Integer, Map<String, List<String>>>> groupedData, List<String> expectedElements){
    var presentElements = groupedData.keySet();
    Set<String> difference = new HashSet<>(expectedElements);
    difference.removeAll(presentElements);
    return difference;
  }
}
