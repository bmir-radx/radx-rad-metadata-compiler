package edu.stanford.bmir.radx.rad.metadata.compiler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class RADxRadGeneratorTest {
  private Map<String, String> dataMap;
  private RADxRadGenerator raDxRadGenerator;

  @BeforeEach
    void setUp(){
      dataMap = new HashMap<>();
      raDxRadGenerator = new RADxRadGenerator();
      dataMap.put("pi_fullname_1", "Diana Carolina Vanegas-Gamboa");
      dataMap.put("pi_role_1", "Project Lead");
      dataMap.put("pi_fullname_2", "Robert Marie Johnson");
      dataMap.put("pi_role_2", "Project Lead");

      dataMap.put("creator_fullname_1", "Diana Carolina Vanegas-Gamboa");
      dataMap.put("creator_orcid_1", "0000-0001-9858-0960");
      dataMap.put("creator_fullname_2", "Diana Carolina Vanegas-Gamboa");
      dataMap.put("creator_affiliation_name_2", "Stanford University");

      dataMap.put("keywords", "biosensor system, SARS-CoV-2, saliva, electrochemical sensing, diagnostic, vulnerable areas, low-cost");
      dataMap.put("data_file_title", "A human readable title for the data file");
  }

  @Test
  void testGenerateRADxRadMetadata(){
    var metadata = raDxRadGenerator.generateRADxRadMetadata(dataMap);

    assertEquals(2, metadata.getContactPIs().size());
    assertEquals("Diana Carolina Vanegas-Gamboa", metadata.getContactPIs().get(0).getName().getFullName());
    assertEquals(2, metadata.getCreators().size());
    assertEquals("Stanford University", metadata.getCreators().get(1).getAffiliation().getAffiliationName());
    assertEquals("biosensor system, SARS-CoV-2, saliva, electrochemical sensing, diagnostic, vulnerable areas, low-cost", metadata.getStudy().getKeywords());
    assertEquals("A human readable title for the data file", metadata.getDataFile().getDataFileTitle());
  }

}
