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

  public TemplateInstanceArtifact generateTemplateArtifactInstance(Map<String, String> spreadsheetData, Map<String, FieldPath> spreadsheet2template, JsonNode templateNode) throws URISyntaxException {
    //read templateContent using cedar-artifact-library
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);

    var templateInstanceArtifactBuilder = TemplateInstanceArtifact.builder();

    var elements = templateSchemaArtifact.getElementNames();
    var groupedData = SpreadsheetDataManager.groupData(spreadsheetData, spreadsheet2template, templateSchemaArtifact);
    var mappedElements = groupedData.keySet();

    for(var childElement : elements){
      if(childElement.equals(DATA_FILE_SUBJECTS.getValue())){
        //generate Data File Subjects element
        RadxRadPrecisionFieldHandler.addDataFileSubjectsElement(spreadsheetData.get(KEYWORDS.getValue()), templateSchemaArtifact, templateInstanceArtifactBuilder);
      } else{
        var childElementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact(childElement);
        var isChildElementMultiple = childElementSchemaArtifact.isMultiple();
        if (mappedElements.contains(childElement)){
          var childElementInstanceArtifacts = elementInstanceArtifactGenerator.generateElementInstanceWithValue(childElement, groupedData, childElementSchemaArtifact, templateSchemaArtifact, spreadsheetData, "");
          if(isChildElementMultiple){
            templateInstanceArtifactBuilder.withMultiInstanceElementInstances(childElement, childElementInstanceArtifacts);
          } else{
            templateInstanceArtifactBuilder.withSingleInstanceElementInstance(childElement, childElementInstanceArtifacts.get(0));
          }
        } else{ // build empty element
          if(isChildElementMultiple){
            templateInstanceArtifactBuilder.withEmptyMultiInstanceElementInstances(childElement);
          } else{
            var elementInstanceArtifact = elementInstanceArtifactGenerator.buildSingleEmptyElementInstance(childElement, templateSchemaArtifact, "/" + childElement);
            templateInstanceArtifactBuilder.withSingleInstanceElementInstance(childElement, elementInstanceArtifact);
          }
        }
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
