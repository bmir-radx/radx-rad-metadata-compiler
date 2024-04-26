package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;
import org.metadatacenter.artifacts.model.renderer.JsonSchemaArtifactRenderer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

public class Compiler {
  private static final String SPREADSHEET_FILE_PATH = "s";
  private static final String OUTPUT_DIRECTORY_PATH = "o";
  private static final String TEMPLATE_PATH = "t";
  private static final String MAPPING_SPREADSHEET_PATH = "m";
  private static final String HELP = "h";
  private static final String DEFAULT_MAPPING_SPREADSHEET_PATH = "src/main/resources/spreadsheet2templatePath2.0.xlsx";
  private static final SpreadsheetReader spreadsheetReader = new SpreadsheetReader();
  private static final JsonSchemaArtifactRenderer jsonSchemaArtifactRenderer = new JsonSchemaArtifactRenderer();
  private static final TemplateInstanceArtifactGenerator templateArtifactInstanceGenerator = new TemplateInstanceArtifactGenerator();
  private static final ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args) throws Exception {
    CommandLineParser commandLineParser = new DefaultParser();
    Options options = buildCommamdLineOptions();

    try{
      CommandLine command = commandLineParser.parse(options, args);
      Path spreadSheetFile = Path.of(command.getOptionValue(SPREADSHEET_FILE_PATH));
      Path outputDirectory = Path.of(command.getOptionValue(OUTPUT_DIRECTORY_PATH));
      Path template = Path.of(command.getOptionValue(TEMPLATE_PATH));
      Path mappingSpreadsheet;
      if(command.hasOption(MAPPING_SPREADSHEET_PATH)){
        mappingSpreadsheet = Path.of(command.getOptionValue(MAPPING_SPREADSHEET_PATH));
      } else{
        mappingSpreadsheet = Path.of(DEFAULT_MAPPING_SPREADSHEET_PATH);
      }

      if(command.hasOption(HELP)){
        Usage(options, "");
        return;
      }

      if (!Files.exists(outputDirectory)) {
        Files.createDirectories(outputDirectory);
      }

      if (Files.isDirectory(spreadSheetFile)) {
        try (Stream<Path> paths = Files.walk(spreadSheetFile)) {
          paths.filter(Files::isRegularFile)
              .filter(path -> path.toString().endsWith(".xlsx"))
              .forEach(file -> {
                // For each file, generate a report in the specified output directory
                try {
                  String outputFileName = getOutputFileName(file);
                  transform(file, outputDirectory.resolve(outputFileName), template, mappingSpreadsheet);
                } catch (Exception e) {
                  System.err.println("Error processing file " + file + ": " + e.getMessage());
                }
              });
        }
      } else if (Files.exists(spreadSheetFile)) {
        String outputFileName = getOutputFileName(spreadSheetFile);
        transform(spreadSheetFile, outputDirectory.resolve(outputFileName), template, mappingSpreadsheet);
      } else {
        throw new FileNotFoundException("Spreadsheet path not found: " + spreadSheetFile);
      }
    } catch (ParseException e){
      Usage(options, e.getMessage());
    }
  }

  private static void transform(Path spreadsheetFile, Path outputFile, Path template, Path mappingSpreadsheet) throws IOException, URISyntaxException {
//    var templateNode = mapper.readTree(Compiler.class.getClassLoader().getResource("RADxMetadataSpecification.json"));
    var templateNode = mapper.readTree(template.toFile());
    var spreadsheetData = spreadsheetReader.readRadxRadSpreadsheet(spreadsheetFile.toString());
//    var spreadSheet2templatePath = spreadsheetReader.readSpreadsheet2templatePath(
//        Objects.requireNonNull(Compiler.class.getClassLoader().getResource("spreadsheet2templatePath.xlsx")).getPath()
//    );
    var spreadSheet2templatePath = spreadsheetReader.readSpreadsheet2templatePath(mappingSpreadsheet.toString());
    var templateInstanceArtifact = templateArtifactInstanceGenerator.generateTemplateArtifactInstance(
        spreadsheetData,spreadSheet2templatePath, templateNode
    );
    ObjectNode templateInstanceRendering = jsonSchemaArtifactRenderer.renderTemplateInstanceArtifact(templateInstanceArtifact);
    mapper.writeValue(outputFile.toFile(), templateInstanceRendering);
  }

  private static Options buildCommamdLineOptions(){
    Options options = new Options();

    Option spreadsheetFilePathOption = Option.builder(SPREADSHEET_FILE_PATH)
        .argName("spreadsheet-file-path")
        .desc("Spreadsheet File Path")
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
        .argName("spreadsheet-to-template-mapping-path")
        .desc("Spreadsheet2templatePath mapping spreadsheet path")
        .required(false)
        .hasArg()
        .build();

    Option helpOption = Option.builder(HELP)
            .desc("Show help")
            .build();

    options.addOption(spreadsheetFilePathOption);
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
    return spreadSheetFile.getFileName().toString().replaceAll("\\.xlsx$", ".json");
  }
}
