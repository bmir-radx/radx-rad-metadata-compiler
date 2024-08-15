package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Map;


public class CsvReaderTest {
  @Test
  public void testReadSpreadsheet() throws IOException {
    String pathToFile = getClass().getClassLoader().getResource("RADxRadMetadataExample.csv").getPath();

    CsvReader reader = new CsvReader();
    Map<String, String> fieldValues = reader.readCsvMetadata(pathToFile);

    assertEquals("Diana Carolina Vanegas-Gamboa", fieldValues.get("pi_fullname_1"));
    assertEquals("Development of an Automated Diagnostic Platform for SARS-CoV-2 Monitoring in Vulnerable Areas", fieldValues.get("project_title"));
    assertEquals("2007-04-05T12:30−02:00", fieldValues.get("data_file_creation_dateTime"));
  }
}
