package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.bmir.radx.metadata.validator.lib.JsonLoader;
import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateArtifactInstanceGenerator {
  //TODO CONTEXT
  private static final Pattern FIELD_PATTERN = Pattern.compile("(.+?)_?(\\d*)$");
  public TemplateInstanceArtifact generateTemplateArtifactInstance(Map<String, String> spreadsheetData, Map<String, FieldArtifact> spreadsheet2template, String templateContent){
    //read templateContent using cedar-artifact-library
    var templateNode = JsonLoader.loadJson(templateContent, "Template");
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    var expectedElements = templateSchemaArtifact.getElementNames();

    var groupedData = groupData(spreadsheetData, spreadsheet2template);

    //generate elements that have values in the spreadsheet
    var templateInstanceBuilder = buildElementsWithValues(groupedData, templateSchemaArtifact,spreadsheetData);

    //generate elements that does not contained in the spreadsheet
    var notPresentElements = getNotPresentElementsSet(spreadsheetData, expectedElements);
    var templateInstanceBuilder2 = buildEmptyElementInstances(notPresentElements, templateSchemaArtifact, templateInstanceBuilder);

    return templateInstanceBuilder2.build();
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
  private Map<String, Map<Integer, Map<String, List<String>>>> groupData(Map<String, String> spreadsheetData, Map<String, FieldArtifact> spreadsheet2template){
    Map<String, Map<Integer, Map<String, List<String>>>> groupedData = new HashMap<>();
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
            .computeIfAbsent(field, k -> new ArrayList<>())
            .add(value);
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

  private FieldInstanceArtifact buildFieldInstance(Optional<ValueConstraints> valueConstraints, Map<String, List<String>> fields, String expectedField) throws URISyntaxException {
    var controlledTermsMap = MapInitializer.createControlledTermsMap();
    var fieldInstanceBuilder = FieldInstanceArtifact.builder();
    var value = fields.get(expectedField);
    if(valueConstraints.isPresent() && valueConstraints.get().isLinkValueConstraint()){
      fieldInstanceBuilder.withJsonLdId(new URI(value.get(0)));
    } else if (valueConstraints.isPresent() && valueConstraints.get().isControlledTermValueConstraint()) {
      fieldInstanceBuilder.withLabel(value.get(0));
      fieldInstanceBuilder.withJsonLdId(new URI(controlledTermsMap.get(value.get(0))));
    } else{
      fieldInstanceBuilder.withJsonLdValue(value.get(0));
    }
    return  fieldInstanceBuilder.build();
  }

  /***
   * Build elements that have values in the spreadsheet
   * @return
   */
  private TemplateInstanceArtifact.Builder buildElementsWithValues(Map<String, Map<Integer, Map<String, List<String>>>> groupedData,
                                                                   TemplateSchemaArtifact templateSchemaArtifact,
                                                                   Map<String, String> spreadsheetData){

    var templateInstanceBuilder = TemplateInstanceArtifact.builder();

    groupedData.forEach((elementName, instances) -> {
      var childFields = templateSchemaArtifact.getElementSchemaArtifact(elementName).getFieldNames();
      var childElements = templateSchemaArtifact.getElementSchemaArtifact(elementName).getElementNames();
      var isMultipleElement = templateSchemaArtifact.getElementSchemaArtifact(elementName).isMultiple();
      List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();

      instances.forEach((index, fields) ->{
        var elementInstanceBuilder = ElementInstanceArtifact.builder();

        childFields.forEach(expectedField ->{
          var expectedFieldValueConstraint = templateSchemaArtifact.getElementSchemaArtifact(elementName).getFieldSchemaArtifact(expectedField).valueConstraints();

          var isMultipleField = false;
          if(expectedFieldValueConstraint.isPresent()){
            isMultipleField = expectedFieldValueConstraint.get().multipleChoice();
          }

          //check if field is an attribute value type
          //if yes, build attribute value fields, otherwise, build regular fields
          var specificationPath = "/" + elementName + "/" + expectedField;
          if(isAttributeValue(templateSchemaArtifact, specificationPath)){
            var values = fields.get(expectedField);
            Map<String, FieldInstanceArtifact> attributeValueFieldInstances = new HashMap<>();
            for (var value: values){
              attributeValueFieldInstances.put(value,
                  FieldInstanceArtifact.builder().
                      withJsonLdValue(spreadsheetData.get(value)).
                      build());
            }
            elementInstanceBuilder.withAttributeValueFieldInstances(expectedField, attributeValueFieldInstances);

          } else{
            // if the field in the template has the corresponding field in the spreadsheet, then need to retrieve data from spreadsheet
            // otherwise, build an empty fieldArtifactInstance
            if (fields.containsKey(expectedField)){
              FieldInstanceArtifact fieldInstance;
              try {
                fieldInstance = buildFieldInstance(expectedFieldValueConstraint, fields, expectedField);
              } catch (URISyntaxException e) {
                throw new RuntimeException(e);
              }
              if(isMultipleField){
                elementInstanceBuilder.withMultiInstanceFieldInstances(expectedField, List.of(fieldInstance));
              } else{
                elementInstanceBuilder.withSingleInstanceFieldInstance(expectedField, fieldInstance);
              }
            } else{
              if(isMultipleField){
                elementInstanceBuilder.withMultiInstanceFieldInstances(expectedField, List.of(FieldInstanceArtifact.builder().build()));
              } else{
                elementInstanceBuilder.withSingleInstanceFieldInstance(expectedField, FieldInstanceArtifact.builder().build());
              }
            }
          }

        });

        //Build nested child element
        //Since radx-rad spreadsheet don't have fields that maps to nested element
        //directly build empty nested element
        for(var childElement : childElements){
          buildEmptyElementInstance(childElement, templateSchemaArtifact, elementInstanceBuilder, "/" + elementName + "/" + childElement);
        }

        elementInstanceArtifacts.add(elementInstanceBuilder.build());
      });

      if(isMultipleElement){
        templateInstanceBuilder.withMultiInstanceElementInstances(elementName, elementInstanceArtifacts);
      } else{
        templateInstanceBuilder.withElementInstance(elementName, elementInstanceArtifacts.get(0));
      }
    });

    return templateInstanceBuilder;
  }

  /***
   * Build Elements that does not have value in spreadsheet
   * @param notPresentElements
   * @param templateSchemaArtifact
   * @param templateInstanceBuilder
   * @return
   */
  private TemplateInstanceArtifact.Builder buildEmptyElementInstances(Set<String> notPresentElements,
                                                                      TemplateSchemaArtifact templateSchemaArtifact,
                                                                      TemplateInstanceArtifact.Builder templateInstanceBuilder){
    for(var elementName : notPresentElements){
      var isMultipleElement = templateSchemaArtifact.getElementSchemaArtifact(elementName).isMultiple();
      var elementInstanceBuilder = ElementInstanceArtifact.builder();
      if(isMultipleElement){
        templateInstanceBuilder.withMultiInstanceElementInstances(elementName, List.of(elementInstanceBuilder.build()));
      } else{
        buildEmptyElementInstance(elementName, templateSchemaArtifact, elementInstanceBuilder, "/" + elementName);
      }
    }
    return templateInstanceBuilder;
  }

  private ElementInstanceArtifact.Builder buildEmptyElementInstance(String elementName,
                                                                     TemplateSchemaArtifact templateSchemaArtifact,
                                                                     ElementInstanceArtifact.Builder elementInstanceBuilder,
                                                                    String path){
    var elementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact(elementName);
    var childFields = elementSchemaArtifact.getFieldNames();

    for(var expectedField : childFields){
      var specificationPath = "/" + expectedField;
      if(isAttributeValue(templateSchemaArtifact, specificationPath)){
        elementInstanceBuilder.withAttributeValueFieldInstances(expectedField, new HashMap<>());
      } else{
        if(templateSchemaArtifact.getElementSchemaArtifact(elementName).getFieldSchemaArtifact(expectedField).isMultiple()){
          elementInstanceBuilder.withMultiInstanceFieldInstances(expectedField, List.of(buildEmptyFieldInstance()));
        } else{
          elementInstanceBuilder.withSingleInstanceFieldInstance(expectedField, buildEmptyFieldInstance());
        }
      }
    }

    var childElements = elementSchemaArtifact.getElementNames();
    for (var childElement : childElements){
      if (elementSchemaArtifact.getElementSchemaArtifact(childElement).isMultiple()){
        elementInstanceBuilder
            .withMultiInstanceElementInstances(childElement,
                List.of(ElementInstanceArtifact.builder().build()));
      } else {
        buildEmptyElementInstance(childElement, templateSchemaArtifact, elementInstanceBuilder, path + "/" + childElement);
      }
    }

    return elementInstanceBuilder;
  }

  private FieldInstanceArtifact buildEmptyFieldInstance(){
    return FieldInstanceArtifact.builder().build();
  }

  private boolean isAttributeValue(TemplateSchemaArtifact templateSchemaArtifact, String specificationPath){
    var templateReporter = new TemplateReporter(templateSchemaArtifact);
    var attributeValueInstanceArtifact = templateReporter.getFieldSchema(specificationPath);
    return attributeValueInstanceArtifact.map(fieldSchemaArtifact -> fieldSchemaArtifact.fieldUi().isAttributeValue()).orElse(false);
  }
}
