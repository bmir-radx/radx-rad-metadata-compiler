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

public class TemplateArtifactInstanceGenerator {

  private final ArtifactInstanceGenerator artifactInstanceGenerator = new ArtifactInstanceGenerator();

  public TemplateInstanceArtifact generateTemplateArtifactInstance(Map<String, String> spreadsheetData, Map<String, FieldArtifact> spreadsheet2template, JsonNode templateNode) throws URISyntaxException {
    //read templateContent using cedar-artifact-library
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    var expectedElements = templateSchemaArtifact.getElementNames();

    var groupedData = SpreadsheetDataManager.groupData(spreadsheetData, spreadsheet2template, templateSchemaArtifact);

    //generate elements that have mappings in the spreadsheet except Data File Subjects element
    var templateInstanceArtifactBuilder = TemplateInstanceArtifact.builder();
    artifactInstanceGenerator.buildElementInstancesWithValues(groupedData, templateSchemaArtifact, templateInstanceArtifactBuilder, spreadsheetData);

    //generate Data File Subjects element
    RadxRadSpecificFieldHandler.addDataFileSubjectsElement(spreadsheetData.get(KEYWORDS.getValue()), templateSchemaArtifact, templateInstanceArtifactBuilder);

    //generate elements that does not contained in the spreadsheet
    var notPresentElements = getNotPresentElementsSet(groupedData, expectedElements);
    artifactInstanceGenerator.buildEmptyElementInstances(notPresentElements, templateSchemaArtifact, templateInstanceArtifactBuilder);

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


  private Set<String> getNotPresentElementsSet(Map<String, Map<Integer, Map<String, List<String>>>> groupedData, List<String> expectedElements){
    var presentElements = groupedData.keySet();
    Set<String> difference = new HashSet<>(expectedElements);
    difference.removeAll(presentElements);
    difference.remove(DATA_FILE_SUBJECTS.getValue());
    return difference;
  }
}
