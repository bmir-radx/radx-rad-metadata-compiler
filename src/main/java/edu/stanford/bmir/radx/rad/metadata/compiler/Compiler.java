package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;
import org.metadatacenter.artifacts.model.renderer.JsonSchemaArtifactRenderer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;


public class Compiler {
  private static final String CSV_FILE_PATH = "c";
  private static final String OUTPUT_DIRECTORY_PATH = "o";
  private static final String TEMPLATE_PATH = "t";
  private static final String MAPPING_SPREADSHEET_PATH = "m";
  private static final String HELP = "h";
  private static final String DEFAULT_MAPPING_SPREADSHEET = "/csv2templatePath.xlsx";
  private static final CsvReader csvReader = new CsvReader();
  private static final PathMappingReader pathMappingReader = new PathMappingReader();
  private static final JsonSchemaArtifactRenderer jsonSchemaArtifactRenderer = new JsonSchemaArtifactRenderer();
  private static final TemplateInstanceArtifactGenerator2 templateArtifactInstanceGenerator = new TemplateInstanceArtifactGenerator2();
  private static final ObjectMapper mapper = new ObjectMapper();
  public static void main(String[] args) throws Exception {
    CommandLineParser commandLineParser = new DefaultParser();
    Options options = buildCommamdLineOptions();

    try{
      CommandLine command = commandLineParser.parse(options, args);
      Path csvFile = Path.of(command.getOptionValue(CSV_FILE_PATH));
      Path outputDirectory = Path.of(command.getOptionValue(OUTPUT_DIRECTORY_PATH));
      Path template = Path.of(command.getOptionValue(TEMPLATE_PATH));
      Path mappingSpreadsheet;
      if(command.hasOption(MAPPING_SPREADSHEET_PATH)){
        mappingSpreadsheet = Path.of(command.getOptionValue(MAPPING_SPREADSHEET_PATH));
      } else{
        mappingSpreadsheet = getDefaultMappingPath();
      }

      if(command.hasOption(HELP)){
        Usage(options, "");
        return;
      }

      if (!Files.exists(outputDirectory)) {
        Files.createDirectories(outputDirectory);
      }

      if (Files.isDirectory(csvFile)) {
        try (Stream<Path> paths = Files.walk(csvFile)) {
          paths.filter(Files::isRegularFile)
              .filter(path -> path.toString().endsWith(".csv"))
              .forEach(file -> {
                // For each file, generate a report in the specified output directory
                try {
                  String outputFileName = getOutputFileName(file);
                  transform(file, outputDirectory.resolve(outputFileName), template, mappingSpreadsheet);
                  System.out.println("File compiled successfully: " + file);
                } catch (Exception e) {
                  System.out.println("Error processing file  " + file + ": " + e.getMessage());
                }
              });
        }
      } else if (Files.exists(csvFile)) {
        String outputFileName = getOutputFileName(csvFile);
        transform(csvFile, outputDirectory.resolve(outputFileName), template, mappingSpreadsheet);
        System.out.println("File compiled successfully: " + csvFile);
      } else {
        throw new FileNotFoundException("CSV path not found: " + csvFile);
      }
    } catch (ParseException e){
      Usage(options, e.getMessage());
    }
  }

  private static void transform(Path spreadsheetFile, Path outputFile, Path template, Path mappingSpreadsheet) throws IOException, URISyntaxException {
    var templateNode = mapper.readTree(template.toFile());
    var csvData = csvReader.readCsvMetadata(spreadsheetFile.toString());
    var csv2templatePath = pathMappingReader.readCsv2templatePath(mappingSpreadsheet.toString());
    var templateInstanceArtifact = templateArtifactInstanceGenerator.generateTemplateArtifactInstance(
        csvData,csv2templatePath, templateNode, null);
    ObjectNode templateInstanceRendering = jsonSchemaArtifactRenderer.renderTemplateInstanceArtifact(templateInstanceArtifact);
    mapper.writeValue(outputFile.toFile(), templateInstanceRendering);
  }

  private static Options buildCommamdLineOptions(){
    Options options = new Options();

    Option csvMetadataFilePathOption = Option.builder(CSV_FILE_PATH)
        .argName("csv-file-path")
        .desc("CSV Metadata File Path")
        .required(true)
        .hasArg()
        .build();

    Option outputDirectoryPathOption = Option.builder(OUTPUT_DIRECTORY_PATH)
        .argName("output-directory-path")
        .desc("Output directory path")
        .required(true)
        .hasArg()
        .build();

    Option templatePathOption = Option.builder(TEMPLATE_PATH)
        .argName("RADx-Metadata-Specification-path")
        .desc("RADx Metadata Specification Path")
        .required(true)
        .hasArg()
        .build();

    Option mappingPathOption = Option.builder(MAPPING_SPREADSHEET_PATH)
        .argName("csv-to-template-mapping-path")
        .desc("csv2template mapping spreadsheet path")
        .required(false)
        .hasArg()
        .build();

    Option helpOption = Option.builder(HELP)
            .desc("Show help")
            .build();

    options.addOption(csvMetadataFilePathOption);
    options.addOption(outputDirectoryPathOption);
    options.addOption(templatePathOption);
    options.addOption(mappingPathOption);
    options.addOption(helpOption);
    return options;
  }

  private static void Usage(Options options, String errorMessage){
    String header = "RADx-rad Metadata Compiler";
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp(Compiler.class.getName(), header, options, errorMessage, true);
  }

  private static String getOutputFileName(Path spreadSheetFile){
    return spreadSheetFile.getFileName().toString().replaceAll("\\.csv$", ".json");
  }

  private static Path getDefaultMappingPath()throws URISyntaxException {
    URL resource = Compiler.class.getResource(DEFAULT_MAPPING_SPREADSHEET);
    if (resource == null) {
      throw new IllegalArgumentException("File not found!");
    }

    if (resource.getProtocol().equals("jar")) {
      try (InputStream inputStream = resource.openStream()) {
        Path tempFile = Files.createTempFile("csv2templatePath", ".xlsx");
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      return Paths.get(resource.toURI());
    }
  }
}
