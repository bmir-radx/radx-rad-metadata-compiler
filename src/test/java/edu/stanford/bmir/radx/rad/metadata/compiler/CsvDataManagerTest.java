package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class CsvDataManagerTest {
  private String firstVersionMappingFile = "csv2templatePath1.xlsx";
  private String secondVersionMappingFile = "csv2templatePath2.xlsx";
  private String csvFileName = "RADxRadMetadataExample.csv";
  private String firstVersionTemplateFile = "RADxTemplate1.json";
  private String secondVersionTemplateFile = "RADxTemplate2.json";
  private PathMappingReader pathMappingReader = new PathMappingReader();
  private CsvReader csvReader = new CsvReader();
  private CsvDataManager csvDataManager = new CsvDataManager();
  private JsonArtifactReader jsonSchemaArtifactReader = new JsonArtifactReader();
  private ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setup(){

  }

  @Test
  public void testFirstVersion() throws IOException {
    process(firstVersionTemplateFile, firstVersionMappingFile);
  }


  @Test
  public void testSecondVersion() throws IOException {
    process(secondVersionTemplateFile, secondVersionMappingFile);
  }

  private void process(String templateFileName, String mappingFileName) throws IOException {
    var templateNode = mapper.readTree(getClass().getClassLoader().getResource(templateFileName));
    var templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);

    String mappingFilePath = Objects.requireNonNull(getClass().getClassLoader().getResource(mappingFileName)).getPath();
    String csvFilePath = Objects.requireNonNull(getClass().getClassLoader().getResource(csvFileName)).getPath();
    Map<String, String> csvData = csvReader.readCsvMetadata(csvFilePath);
    Map<String, String> csv2templatePath = pathMappingReader.readCsv2templatePath(mappingFilePath);
    csvDataManager.groupData(csvData, csv2templatePath, templateSchemaArtifact);
    var groupedData = csvDataManager.getGroupedData();
    var fieldCounts = csvDataManager.getFieldsCounts();
    var elementCounts = csvDataManager.getElementCounts();
    var avFields = csvDataManager.getAttributeValueMap();
    assert(groupedData.size() != 0);
    assert(fieldCounts.size() != 0);
    assert(elementCounts.size() != 0);
    assert(avFields.size() != 0);
  }
}
