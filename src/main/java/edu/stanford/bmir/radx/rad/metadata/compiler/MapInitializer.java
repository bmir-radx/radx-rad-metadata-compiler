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
  public static Map<String, String> createControlledTermsMap(){
    Map<String, String> controlledTermsMap = new HashMap<>();
    controlledTermsMap.put("Person", personUri);
    controlledTermsMap.put("ORCiD", orcidUri);
    controlledTermsMap.put("ROR", rorUri);
    controlledTermsMap.put("Project Leader", projectLeaderUri);
    controlledTermsMap.put("Created", createdUri);
    controlledTermsMap.put("URL", urlUri);
    //TODO MESH
    controlledTermsMap.put("biosensor system, SARS-CoV-2, saliva, electrochemical sensing, diagnostic, vulnerable areas, low-cost", urlUri);
    return controlledTermsMap;
  }
}
