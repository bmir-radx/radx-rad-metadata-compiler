package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxSpecificationMetadataConstant.*;

public class TemplateArtifactInstanceGenerator {
  private static final String XSD_IRI = "http://www.w3.org/2001/XMLSchema#";
  private static final String XSD_PREFIX = "xsd:";
  private static final Pattern FIELD_PATTERN = Pattern.compile("^(?!study_include_prospective_or_retrospective_human_samples)(.+?)_?(\\d*)$");
  private ContextGenerator contextGenerator = new ContextGenerator();
  private EmptyArtifactChecker emptyArtifactChecker = new EmptyArtifactChecker();

  public TemplateInstanceArtifact generateTemplateArtifactInstance(Map<String, String> spreadsheetData, Map<String, FieldArtifact> spreadsheet2template, JsonNode templateNode) throws URISyntaxException {
    //read templateContent using cedar-artifact-library
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    var expectedElements = templateSchemaArtifact.getElementNames();

    var groupedData = groupData(spreadsheetData, spreadsheet2template, templateSchemaArtifact);

    //generate elements that have values in the spreadsheet
    var templateInstanceArtifactBuilder = TemplateInstanceArtifact.builder();
    buildElementsWithValues(groupedData, templateSchemaArtifact, templateInstanceArtifactBuilder, spreadsheetData);

    //generate elements that does not contained in the spreadsheet
    var notPresentElements = getNotPresentElementsSet(groupedData, expectedElements);
    buildEmptyElementInstances(notPresentElements, templateSchemaArtifact, templateInstanceArtifactBuilder);

    return templateInstanceArtifactBuilder
        .withIsBasedOn(new URI(IS_BASED_ON.getField()))
        .withDescription(SCHEMA_DESCRIPTION.getField())
        .withName(SCHEMA_NAME.getField())
        .withJsonLdContext(contextGenerator.generateTemplateInstanceContext(templateSchemaArtifact))
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

        if(isAttributeValue(templateSchemaArtifact, specificationPath)){
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

  private FieldInstanceArtifact buildFieldInstance(Optional<ValueConstraints> valueConstraints, Map<String, List<String>> fields, String expectedField) throws URISyntaxException {
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

    return  fieldInstanceBuilder.build();
  }

  /***
   * Build elements that have values in the spreadsheet
   * @return
   */
  private void buildElementsWithValues(Map<String, Map<Integer, Map<String, List<String>>>> groupedData,
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
          if(isAttributeValue(templateSchemaArtifact, specificationPath)){
            var values = fields.get(expectedField);
            Map<String, FieldInstanceArtifact> attributeValueFieldInstances = new HashMap<>();
            if(values != null){
              for (var value: values){
                attributeValueFieldInstances.put(value,
                    FieldInstanceArtifact.builder().
                        withJsonLdValue(spreadsheetData.get(value)).
                        build());
              }
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
                //Add values to RADx-rad specific controlled terms fields or add an empty field entry
                SpecificControlledTermUtil.addSpecificControlledTerms(elementInstanceBuilder, elementName, expectedField, fields);
              }
            }
          }

        };

        //Build nested child element
        //Since radx-rad spreadsheet don't have fields that maps to nested element
        //directly build empty nested element
        for(var childElement : childElements){
          buildSingleEmptyElementInstance(childElement, templateSchemaArtifact, elementInstanceBuilder, "/" + elementName + "/" + childElement);
        }

        //Add context for each elementInstance
        elementInstanceBuilder.withJsonLdContext(
            contextGenerator.generateElementInstanceContext(
                templateSchemaArtifact.getElementSchemaArtifact(elementName)
            ));

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

  private Set<String> getNotPresentElementsSet(Map<String, Map<Integer, Map<String, List<String>>>> groupedData, List<String> expectedElements){
    var presentElements = groupedData.keySet();
    Set<String> difference = new HashSet<>(expectedElements);
    difference.removeAll(presentElements);
    return difference;
  }

  /***
   * Build Elements that does not have value in spreadsheet
   * @param notPresentElements
   * @param templateSchemaArtifact
   * @param templateInstanceBuilder
   * @return
   */
  private void buildEmptyElementInstances(Set<String> notPresentElements,
                                                                      TemplateSchemaArtifact templateSchemaArtifact,
                                                                      TemplateInstanceArtifact.Builder templateInstanceBuilder){
    for(var elementName : notPresentElements){
      var isMultipleElement = templateSchemaArtifact.getElementSchemaArtifact(elementName).isMultiple();
      var elementInstanceBuilder = ElementInstanceArtifact.builder();
      if(isMultipleElement){
        templateInstanceBuilder.withMultiInstanceElementInstances(elementName, List.of(elementInstanceBuilder.build()));
      } else{
          buildSingleEmptyElementInstance(elementName, templateSchemaArtifact, elementInstanceBuilder, "/" + elementName);
          templateInstanceBuilder.withElementInstance(elementName, elementInstanceBuilder.build());
      }
    }
  }

  private void buildSingleEmptyElementInstance(String elementName,
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
        buildSingleEmptyElementInstance(childElement, templateSchemaArtifact, elementInstanceBuilder, path + "/" + childElement);
      }
    }
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
