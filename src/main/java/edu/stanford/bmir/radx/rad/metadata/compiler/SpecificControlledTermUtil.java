package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxSpecificationMetadataConstant.*;

public class SpecificControlledTermUtil {
  private final static String person = "Person";
  private final static String orcid = "ORCiD";
  private final static String ror = "ROR";
  private final static String url = "URL";
  private final static String created = "Created";

  /***
   * This method aims to set RADx-rad specific controlled term fields
   * such as set Contributor Type to Person
   * It adds empty field entry if it is not a specific controlled term fields
   * @param elementInstanceArtifactBuilder
   * @param elementName
   * @param fields
   * @return
   */
  public static void addSpecificControlledTerms(
      ElementInstanceArtifact.Builder elementInstanceArtifactBuilder,
      String elementName,
      String expectedField,
      Map<String, List<String>> fields) throws URISyntaxException {

    var controlledTermMap = MapInitializer.createControlledTermsMap();
    String rorPrefix = controlledTermMap.get(ror);
    //If the element instance has value, then set specific controlled term fields
    if(isNonEmptyValue(fields)){
      if ((elementName.equals(DATA_FILE_CONTRIBUTORS.getField()) && expectedField.equals(CONTRIBUTOR_TYPE.getField())) ||
          (elementName.equals(DATA_FILE_CREATORS.getField()) && expectedField.equals(CREATOR_TYPE.getField()))){
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.builder()
                .withJsonLdId(new URI(controlledTermMap.get(person)))
                .withLabel(person)
                .build());
      } else if (elementName.equals(DATA_FILE_CONTRIBUTORS.getField())
          && expectedField.equals(CONTRIBUTOR_IDENTIFIER_SCHEME.getField())
          && fields.containsKey(CONTRIBUTOR_IDENTIFIER.getField())
          && fields.get(CONTRIBUTOR_IDENTIFIER.getField()).get(0)!= null){
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.builder()
                .withJsonLdId(new URI(controlledTermMap.get(orcid)))
                .withLabel(orcid)
                .build());
      } else if (elementName.equals(DATA_FILE_CREATORS.getField())
          && expectedField.equals(CREATOR_IDENTIFIER_SCHEME.getField())
          && fields.containsKey(CREATOR_IDENTIFIER.getField())
          && fields.get(CREATOR_IDENTIFIER.getField()).get(0)!= null){
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.builder()
                .withJsonLdId(new URI(controlledTermMap.get(orcid)))
                .withLabel(orcid)
                .build());
      } else if (elementName.equals(DATA_FILE_CONTRIBUTORS.getField())
          && expectedField.equals(CONTRIBUTOR_AFFILIATION_IDENTIFIER_SCHEME.getField())
          && fields.containsKey(CONTRIBUTOR_AFFILIATION_IDENTIFIER.getField())
          && fields.get(CONTRIBUTOR_AFFILIATION_IDENTIFIER.getField()).get(0) != null
          && fields.get(CONTRIBUTOR_AFFILIATION_IDENTIFIER.getField()).get(0).startsWith(rorPrefix)) {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.builder()
                .withJsonLdId(new URI(controlledTermMap.get(ror)))
                .withLabel(ror)
                .build());
      } else if (elementName.equals(DATA_FILE_CREATORS.getField())
          && expectedField.equals(CREATOR_AFFILIATION_IDENTIFIER_SCHEME.getField())
          && fields.containsKey(CREATOR_AFFILIATION_IDENTIFIER.getField())
          && fields.get(CREATOR_AFFILIATION_IDENTIFIER.getField()).get(0).startsWith(rorPrefix)) {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.builder()
                .withJsonLdId(new URI(controlledTermMap.get(ror)))
                .withLabel(ror)
                .build());
      } else if (elementName.equals(DATA_FILE_RELATED_RESOURCES.getField())
          && expectedField.equals(RELATED_RESOURCE_IDENTIFER_TYPE.getField())
          && fields.containsKey(RELATED_RESOURCE_IDENTIFER.getField())
          && fields.get(RELATED_RESOURCE_IDENTIFER.getField()).get(0) != null) {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.builder()
                .withJsonLdId(new URI(controlledTermMap.get(url)))
                .withLabel(url)
                .build());
      } else if (elementName.equals(DATA_FILE_DATES.getField())
          && expectedField.equals(EVENT_TYPE.getField())
          && fields.containsKey(DATE.getField())
          && fields.get(DATE.getField()).get(0) != null)  {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.builder()
                .withJsonLdId(new URI(controlledTermMap.get(created)))
                .withLabel(created)
                .build());
      } else{
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.builder().build());
      }
    } else{
      elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
          FieldInstanceArtifact.builder().build());
    }
  }

  private static boolean isNonEmptyValue(Map<String, List<String>> fields){
    for(Map.Entry<String, List<String>> entry : fields.entrySet()){
      for(var value : entry.getValue()){
        if (value != null){
          return true;
        }
      }
    }
    return false;
  }
}
