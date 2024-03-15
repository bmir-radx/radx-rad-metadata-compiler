package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;
import org.metadatacenter.artifacts.model.renderer.JsonSchemaArtifactRenderer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Compiler {
  private static final String SPREADSHEET_FILE_PATH = "s";
  private static final String OUTPUT_DIRECTORY_PATH = "o";
  private static final SpreadsheetReader spreadsheetReader = new SpreadsheetReader();
  private static final JsonSchemaArtifactRenderer jsonSchemaArtifactRenderer = new JsonSchemaArtifactRenderer();
  private static final TemplateArtifactInstanceGenerator templateArtifactInstanceGenerator = new TemplateArtifactInstanceGenerator();
  private static final ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args) throws Exception {
    CommandLineParser commandLineParser = new DefaultParser();
    Options options = buildCommamdLineOptions();

    try{
      CommandLine command = commandLineParser.parse(options, args);
      Path spreadSheetFile = Path.of(command.getOptionValue(SPREADSHEET_FILE_PATH));
      Path outputDirectory = Path.of(command.getOptionValue(OUTPUT_DIRECTORY_PATH));
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
                  transform(spreadSheetFile, outputDirectory.resolve(outputFileName));
                } catch (Exception e) {
                  System.err.println("Error processing file " + file + ": " + e.getMessage());
                }
              });
        }
      } else if (Files.exists(spreadSheetFile)) {
        String outputFileName = getOutputFileName(spreadSheetFile);
        transform(spreadSheetFile, outputDirectory.resolve(outputFileName));
      } else {
        throw new FileNotFoundException("Spreadsheet path not found: " + spreadSheetFile);
      }
    } catch (ParseException e){
      Usage(options, e.getMessage());
    }

  }

  private static void transform(Path spreadsheetFile, Path outputFile) throws IOException, URISyntaxException {
    var templateNode = mapper.readTree(Compiler.class.getClassLoader().getResource("RADxMetadataSpecification.json"));
    var spreadsheetData = spreadsheetReader.readRadxRadSpreadsheet(spreadsheetFile.toString());
    var spreadSheet2template = spreadsheetReader.readSpreadsheet2Template(
        Compiler.class.getClassLoader().getResource("spreadsheet2template.xlsx").getPath()
    );
    var templateInstanceArtifact = templateArtifactInstanceGenerator.generateTemplateArtifactInstance(
        spreadsheetData,spreadSheet2template, templateNode
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

    options.addOption(spreadsheetFilePathOption);
    options.addOption(outputDirectoryPathOption);
    return options;
  }

  private static void Usage(Options options, String errorMessage){
    String header = "RADx-Rad Metadata Compiler";
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp(Compiler.class.getName(), header, options, errorMessage, true);
  }

  private static String getOutputFileName(Path spreadSheetFile){
    return spreadSheetFile.getFileName().toString().replaceAll("\\.xlsx$", "_transform.json");
  }
}
