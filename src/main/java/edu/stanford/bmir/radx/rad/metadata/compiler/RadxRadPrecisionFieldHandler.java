package edu.stanford.bmir.radx.rad.metadata.compiler;

import edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators.TextFieldGenerator;
import org.metadatacenter.artifacts.model.core.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxSpecificationMetadataConstant.*;

public class RadxRadPrecisionFieldHandler {
  private final static String person = "Person";
  private final static String orcid = "ORCiD";
  private final static String ror = "ROR";
  private final static String uei = "UEI";
  private final static String url = "URL";
  private final static String created = "Created";
  private final static String meshUri =  "http://purl.bioontology.org/ontology/MESH";
  private final static String contributorIdentifierPath = "/Data File Contributors/Contributor Identifier";
  private final static String creatorIdentifierPath = "/Data File Creators/Creator Identifier";
  private final static String creatorAffiliationIdPath = "/Data File Creators/Creator Affiliation Identifier";
  private final static String creatorNamePath = "/Data File Creators/Creator Name";
  private final static String relatedResourceIdentifierPath = "/Data File Related Resources/Related Resource Identifier";
  private final static String datePath = "/Data File Dates/Date";
  private final static String studyIdentifierPath = "/Data File Parent Studies/Study Identifier";
  private final static String primaryLangPath = "/Primary Language";
  private static final FieldInstanceArtifactGenerator fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator();

  /***
   * This method aims to set RADx-rad specific controlled term fields
   * such as set Contributor Type to Person
   * It adds empty field entry if it is not a specific controlled term fields
   */
  public static void addSpecificFields(
      ElementInstanceArtifact.Builder elementInstanceArtifactBuilder,
      String elementName,
      String expectedField,
      ElementSchemaArtifact elementSchemaArtifact,
      Map<String, Map<Integer, List<String>>> groupedData,
      Map<String, Integer> elementInstanceCounts,
      int i){

    var fieldSchemaArtifact = elementSchemaArtifact.getFieldSchemaArtifact(expectedField);
    var valueConstraints = fieldSchemaArtifact.valueConstraints();
    var isMultiple = fieldSchemaArtifact.isMultiple();
    var fieldType = FieldType.getFieldType(fieldSchemaArtifact);

    //If the element instance is not empty, then set specific controlled term fields
    if(!isEmptyElementInstance(elementInstanceCounts, elementName, elementSchemaArtifact)){
//     if (elementName.equals(DATA_FILE_CONTRIBUTORS.getValue())  //Set Contributor Identifier Scheme to ORCiD
//          && expectedField.equals(CONTRIBUTOR_IDENTIFIER_SCHEME.getValue())
//          && groupedData.containsKey(contributorIdentifierPath)
//          && groupedData.get(contributorIdentifierPath).get(i)!= null){
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, orcid, Optional.empty()));
//      } else if (elementName.equals(DATA_FILE_CREATORS.getValue())  //Set Creator Identifier Scheme to ORCiD
//          && expectedField.equals(CREATOR_IDENTIFIER_SCHEME.getValue())
//          && groupedData.containsKey(creatorIdentifierPath)
//          && groupedData.get(creatorIdentifierPath).get(i)!= null){
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, orcid, Optional.empty()));
//      } else if (elementName.equals(DATA_FILE_CREATORS.getValue())  //Set Creator Affiliation Identifier Scheme to ROR
//          && expectedField.equals(CREATOR_AFFILIATION_IDENTIFIER_SCHEME.getValue())
//          && groupedData.containsKey(creatorAffiliationIdPath)
//          && groupedData.get(creatorAffiliationIdPath).get(i)!= null){
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, ror, Optional.empty()));
//      } else if (elementName.equals(DATA_FILE_CREATORS.getValue()) //Set Creator Given Name
      if( elementName.equals(DATA_FILE_CREATORS.getValue())
          && expectedField.equals(CREATOR_GIVEN_NAME.getValue())
          && groupedData.containsKey(creatorNamePath)){
        var name = groupedData.get(creatorNamePath).get(i).get(0);
        if(name != null){
          var givenName = name.split(" ")[0];
          elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
              fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, givenName, Optional.empty()));
        }
      } else if (elementName.equals(DATA_FILE_CREATORS.getValue()) //Set Creator Family Name
          && expectedField.equals(CREATOR_FAMILY_NAME.getValue())
          && groupedData.containsKey(creatorNamePath)){
        var name = groupedData.get(creatorNamePath).get(i).get(0);
        if(name != null){
          var familyName = name.split(" ")[1];
          elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
              fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, familyName, Optional.empty()));
        }
      } else if (elementName.equals(DATA_FILE_RELATED_RESOURCES.getValue())  //Set Related Resource Identifier Type to URL
          && expectedField.equals(RELATED_RESOURCE_IDENTIFER_TYPE.getValue())
          && groupedData.containsKey(relatedResourceIdentifierPath)
          && groupedData.get(relatedResourceIdentifierPath).get(i) != null) {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, url, Optional.empty()));
      } else if (elementName.equals(DATA_FILE_DATES.getValue())  //Set Date Type to Created
          && expectedField.equals(EVENT_TYPE.getValue())
          && groupedData.containsKey(datePath)
          && groupedData.get(datePath).get(i) != null)  {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, created, Optional.empty()));
//      } else if (elementName.equals(DATA_FILE_PARENT_STUDIES.getValue())  //Set Study Identifier Scheme to URL
//          && expectedField.equals(STUDY_IDENTIFIER_SCHEME.getValue())
//          && groupedData.containsKey(studyIdentifierPath)
//          && isValidURL(groupedData.get(studyIdentifierPath).get(i))) {
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, url, Optional.empty()));
//      } else if(elementName.equals(DATA_FILE_TITLES.getValue())  //Set Data File Title Language to en
//          && expectedField.equals(LANGUAGE.getValue())){
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, "en", Optional.empty()));
      } else{
        var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildEmptyFieldInstance(fieldType, valueConstraints);
        buildWithFieldInstanceArtifact(elementInstanceArtifactBuilder, fieldInstanceArtifact, expectedField, isMultiple);
      }
    } else{
      var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildEmptyFieldInstance(fieldType, valueConstraints);
      buildWithFieldInstanceArtifact(elementInstanceArtifactBuilder, fieldInstanceArtifact, expectedField, isMultiple);
    }
  }

  private static void buildWithFieldInstanceArtifact(ElementInstanceArtifact.Builder elementInstanceArtifactBuilder, FieldInstanceArtifact fieldInstanceArtifact, String fieldName, boolean isMultiple){
    if(isMultiple){
      elementInstanceArtifactBuilder.withMultiInstanceFieldInstances(fieldName, List.of(fieldInstanceArtifact));
    } else{
      elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(fieldName, fieldInstanceArtifact);
    }
  }

  private static boolean isEmptyElementInstance(Map<String, Integer> elementInstanceCounts, String element, ElementSchemaArtifact elementSchemaArtifact){
    if(elementInstanceCounts.containsKey(element)){
      return false;
    }

    var childElements = elementSchemaArtifact.getElementNames();
    for (var childElement : childElements){
      var childElementSchemaArtifact = elementSchemaArtifact.getElementSchemaArtifact(childElement);
      if(!isEmptyElementInstance(elementInstanceCounts, childElement, childElementSchemaArtifact)){
        return false;
      }
    }
    return true;
  }

  private static boolean isValidURL(String url){
    if(url == null){
      return false;
    }
    try{
      new URL(url);
      return true;
    } catch (MalformedURLException e){
      return false;
    }
  }

  public static void processKeywords(String input, TemplateInstanceArtifact.Builder templateInstanceArtifactBuilder) throws URISyntaxException {
    var subjectsInstances = new ArrayList<FieldInstanceArtifact>();
    var keywordsInstances = new ArrayList<FieldInstanceArtifact>();
    if(input != null) {
      var mesh = MeshCsvReader.readCSVToMap();
      String[] keywords = input.split(",");
      for(var keyword: keywords){
        keyword = keyword.trim();

        //add Subject field
        if (mesh.containsKey(keyword)){
          var classId = mesh.get(keyword);
          var subjectField = ControlledTermFieldInstance.builder().withValue(new URI(classId)).withLabel(keyword).build();
          subjectsInstances.add(subjectField);
        }

        //add Keyword
        keywordsInstances.add(new TextFieldGenerator().buildFieldInstance(keyword));
      }
    }
    templateInstanceArtifactBuilder.withMultiInstanceFieldInstances(SUBJECTS.getValue(), subjectsInstances);
    templateInstanceArtifactBuilder.withMultiInstanceFieldInstances(KEYWORDS.getValue(), keywordsInstances);
  }
}
