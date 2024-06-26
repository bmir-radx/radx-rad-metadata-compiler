package edu.stanford.bmir.radx.rad.metadata.compiler;

import java.util.HashMap;
import java.util.Map;

public class MapInitializer {
  private static final String personIri = "https://w3id.org/gdmt/Person";
  private static final String orcidIri = "https://orcid.org/";
  private static final String rorIri = "https://ror.org/";
  private static final String projectLeaderIri = "https://w3id.org/gdmt/ProjectLeader";
  private static final String createdIri = "https://w3id.org/gdmt/Created";
  private static final String urlIri = "https://w3id.org/gdmt/URL";
  private final static String meshIri =  "http://purl.bioontology.org/ontology/MESH";
  //TODO: need to update
  private final static String ueiIri = "https://uei.org/";
  private final static String piIri = "https://w3id.org/gdmt/PI";
  private final static String contactPiIri = "https://w3id.org/gdmt/ContactPI";
  private final static String dataPiIri = "https://w3id.org/gdmt/DataPI";


  public static Map<String, String> createControlledTermsMap(){
    Map<String, String> controlledTermsMap = new HashMap<>();
    controlledTermsMap.put("Person", personIri);
    controlledTermsMap.put("ORCiD", orcidIri);
    controlledTermsMap.put("ROR", rorIri);
    controlledTermsMap.put("UEI", ueiIri);
    controlledTermsMap.put("Project Leader", projectLeaderIri);
    controlledTermsMap.put("Created", createdIri);
    controlledTermsMap.put("URL", urlIri);
    controlledTermsMap.put("MESH", meshIri);
    controlledTermsMap.put("contact-PI", contactPiIri);
    controlledTermsMap.put("data-PI", dataPiIri);
    controlledTermsMap.put("PI", piIri);
    return controlledTermsMap;
  }
}
