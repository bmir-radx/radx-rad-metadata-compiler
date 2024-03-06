package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Map;


public class SpreadsheetReaderTest {
  @Test
  public void testReadSpreadsheet() throws IOException {
    String pathToFile = getClass().getClassLoader().getResource("RADxRadMetadataTemplateSheet.xlsx").getPath();

    SpreadsheetReader reader = new SpreadsheetReader();
    Map<String, String> fieldValues = reader.readSpreadsheet(pathToFile);

    assertEquals("Diana Carolina Vanegas-Gamboa", fieldValues.get("pi_fullname_1"));
    assertEquals("Development of an Automated Diagnostic Platform for SARS-CoV-2 Monitoring in Vulnerable Areas", fieldValues.get("project_title"));
  }
}
