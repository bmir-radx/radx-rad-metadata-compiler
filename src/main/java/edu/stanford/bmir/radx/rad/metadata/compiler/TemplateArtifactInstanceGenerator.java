package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.bmir.radx.metadata.validator.lib.JsonLoader;
import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateArtifactInstanceGenerator {
  private static final Pattern FIELD_PATTERN = Pattern.compile("(.+?)_?(\\d*)$");
  public TemplateInstanceArtifact generateTemplateArtifactInstance(Map<String, String> spreadsheetData, Map<String, FieldArtifact> spreadsheet2template, String templateContent){
    //read templateContent using cedar-artifact-library
    var templateNode = JsonLoader.loadJson(templateContent, "Template");
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    var expectedElements = templateSchemaArtifact.getElementNames();

    //generate elements that have values in the spreadsheet
    var templateInstanceBuilder = TemplateInstanceArtifact.builder();
    var groupedData = groupData(spreadsheetData, spreadsheet2template);
    groupedData.forEach((elementName, instances) -> {
      var expectedFields = templateSchemaArtifact.getElementSchemaArtifact(elementName).getFieldNames();

      instances.forEach((index, fields) ->{
        var elementInstanceBuilder = ElementInstanceArtifact.builder();

        expectedFields.forEach(expectedField ->{
          // if the field in the template has the corresponding field in the spreadsheet
          if (fields.containsKey(expectedField)){
            var expectedFieldValueConstraint = templateSchemaArtifact.getElementSchemaArtifact(elementName).getFieldSchemaArtifact(expectedField).valueConstraints();
            FieldInstanceArtifact fieldInstance = null;
            try {
              fieldInstance = buildFieldInstance(expectedFieldValueConstraint, fields, expectedField);
            } catch (URISyntaxException e) {
              throw new RuntimeException(e);
            }
            elementInstanceBuilder.withFieldInstance(expectedField, fieldInstance);
          } else{
            elementInstanceBuilder.withFieldInstance(expectedField, FieldInstanceArtifact.builder().build());
          }
        });
      });
    });

    //generate elements that does not contained in the spreadsheet
    var notPresentElements = getNotPresentElementsSet(spreadsheetData, expectedElements);
    notPresentElements.forEach(elementName ->{
      var elementInstance = ElementInstanceArtifact.builder().build();
      templateInstanceBuilder.withElementInstance(elementName, elementInstance);
    });

    return templateInstanceBuilder.build();
  }

  /***
   * Group spreadsheet data in the following format:
   * {
   *   Element:{
   *     index{
   *       field1: value,
   *       field2: value
   *     }
   *   }
   * }
   * @param spreadsheetData
   * @param spreadsheet2template
   * @return
   */
  private Map<String, Map<Integer, Map<String, String>>> groupData(Map<String, String> spreadsheetData, Map<String, FieldArtifact> spreadsheet2template){
    Map<String, Map<Integer, Map<String, String>>> groupedData = new HashMap<>();
    spreadsheetData.forEach((key, value) -> {
      Matcher matcher = FIELD_PATTERN.matcher(key);
      if (matcher.find()) {
        String spreadsheetField = matcher.group(1);
        String indexStr = matcher.group(2);
        Integer index = indexStr.isEmpty() ? 0 : Integer.parseInt(indexStr);

        var element = spreadsheet2template.get(spreadsheetField).element();
        var field = spreadsheet2template.get(spreadsheetField).field();

        groupedData.computeIfAbsent(element, k -> new HashMap<>())
            .computeIfAbsent(index, k -> new HashMap<>())
            .put(field, value);
      }
    });
    return groupedData;
  }

  private Set<String> getNotPresentElementsSet(Map<String, String> spreadsheetData, List<String> expectedElements){
    var presentElements = spreadsheetData.keySet();
    Set<String> difference = new HashSet<>(expectedElements);
    difference.removeAll(presentElements);
    return difference;
  }

  private FieldInstanceArtifact buildFieldInstance(Optional<ValueConstraints> valueConstraints, Map<String, String> fields, String expectedField) throws URISyntaxException {
    var controlledTermsMap = MapInitializer.createControlledTermsMap();
    var fieldInstanceBuilder = FieldInstanceArtifact.builder();
    var value = fields.get(expectedField);
    if(valueConstraints.isPresent() && valueConstraints.get().isLinkValueConstraint()){
      fieldInstanceBuilder.withJsonLdId(new URI(value));
    } else if (valueConstraints.isPresent() && valueConstraints.get().isControlledTermValueConstraint()) {
      fieldInstanceBuilder.withLabel(value);
      fieldInstanceBuilder.withJsonLdId(new URI(controlledTermsMap.get(value)));

    } else{
      fieldInstanceBuilder.withJsonLdValue(value);
    }
    return  fieldInstanceBuilder.build();
  }
}
