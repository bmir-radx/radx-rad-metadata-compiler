package edu.stanford.bmir.radx.rad.metadata.compiler;

import java.util.HashMap;
import java.util.Map;

public class MapInitializer {
  private static final String personUri = "http://vocab.fairdatacollective.org/gdmt/Person";
  private static final String orcidUri = "https://orcid.org/";
  private static final String rorUri = "https://ror.org/";
  private static final String projectLeaderUri = "http://vocab.fairdatacollective.org/gdmt/ProjectLeader";
  private static final String createdUri = "http://vocab.fairdatacollective.org/gdmt/Created";
  private static final String urlUri = "http://vocab.fairdatacollective.org/gdmt/URL";
  private final static String meshUri =  "http://purl.bioontology.org/ontology/MESH";
  //TODO: need to update
  private final static String ueiUri = "https://ror.org/";
  private final static String contactPiUri = "http://vocab.fairdatacollective.org/gdmt/ProjectLeader";
  private final static String coPiUri = "http://vocab.fairdatacollective.org/gdmt/ProjectLeader";
  private final static String dataPiUri = "http://vocab.fairdatacollective.org/gdmt/ProjectLeader";


  public static Map<String, String> createControlledTermsMap(){
    Map<String, String> controlledTermsMap = new HashMap<>();
    controlledTermsMap.put("Person", personUri);
    controlledTermsMap.put("ORCiD", orcidUri);
    controlledTermsMap.put("ROR", rorUri);
    controlledTermsMap.put("UEI", ueiUri);
    controlledTermsMap.put("Project Leader", projectLeaderUri);
    controlledTermsMap.put("Created", createdUri);
    controlledTermsMap.put("URL", urlUri);
    controlledTermsMap.put("MESH", meshUri);
    controlledTermsMap.put("Contact PI", contactPiUri);
    controlledTermsMap.put("Co PI", coPiUri);
    controlledTermsMap.put("Data PI", dataPiUri);
    return controlledTermsMap;
  }
}
