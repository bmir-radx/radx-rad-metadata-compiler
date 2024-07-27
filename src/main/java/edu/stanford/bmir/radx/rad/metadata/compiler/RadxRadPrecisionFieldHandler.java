package edu.stanford.bmir.radx.rad.metadata.compiler;

import edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators.TextFieldGenerator;
import org.metadatacenter.artifacts.model.core.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static edu.stanford.bmir.radx.rad.metadata.compiler.FieldType.CONTROLLED_TERM;
import static edu.stanford.bmir.radx.rad.metadata.compiler.FieldType.TEXTFIELD;
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
  private final static String relatedResourceIdentifierPath = "/Data File Related Resources[0]/Related Resource Identifier";
  private final static String datePath = "/Data File Dates/Date";
  private final static String studyIdentifierPath = "/Data File Parent Studies/Study Identifier";
  private final static String keywordPath = "/Data File Subjects[0]/Keyword";
  private final static String primaryLangPath = "/Primary Language";
  private static final FieldInstanceArtifactGenerator2 fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator2();

  public static void firstVersionElementPatch(String elementName,
                                              Map<String, Integer> fieldCount,
                                              Map<String, String> groupedData,
                                              TemplateSchemaArtifact templateSchemaArtifact,
                                              TemplateInstanceArtifact.Builder templateInstanceArtifactBuilder) throws URISyntaxException {
    if(elementName.equals(DATA_FILE_SUBJECTS.getValue())){
      addDataFileSubjectsElement(fieldCount, groupedData, templateSchemaArtifact, templateInstanceArtifactBuilder);
    }
    else if (elementName.equals(DATA_FILE_RELATED_RESOURCES.getValue())){
      addDataFileRelatedResourcesElement(fieldCount, groupedData, templateSchemaArtifact, templateInstanceArtifactBuilder);
    }
  }

  public static void firstVersionFieldPath(String path,
                                           Map<String, Integer> elementCount,
                                           Map<String, String> groupedData,
                                           FieldSchemaArtifact fieldSchemaArtifact,
                                           ElementInstanceArtifact.Builder builder){
    //get parentElement name
    var parentElement = getParentElementName(path);
    var fieldType = FieldType.getFieldType(fieldSchemaArtifact);

    if(elementCount.containsKey(parentElement)){
      if(parentElement.equals(DATA_FILE_CONTRIBUTORS.getValue())){
        patchContributorElementInstance(path, groupedData, fieldType, builder);
      } else if (parentElement.equals(DATA_FILE_CREATORS.getValue())) {
        patchCreatorElementInstance(path, groupedData, fieldType, builder);
      } else if (parentElement.equals(DATA_FILE_DATES.getValue())) {
        patchDateElementInstance(path, groupedData, fieldType, builder);
      } else if (parentElement.equals(DATA_FILE_PARENT_STUDIES.getValue())) {
        patchParentStudyElementInstance(path, groupedData, fieldType, builder);
      } else if (parentElement.equals(DATA_FILE_TITLES.getValue())) {
        patchTitleElementInstance(path, fieldType, builder);
      }
    }

    if (parentElement.equals(DATA_FILE_LANGUAGE.getValue())) {
      patchLanguageElementInstance(path, fieldType, builder);
    }
  }

  /**
   * Set Contributor Identifier Scheme to ORCiD
   * Set Contributor Affiliation Scheme to ROR|UEI
   */
  private static void patchContributorElementInstance(String path,
                                                      Map<String, String> groupedData,
                                                      FieldType fieldType,
                                                      ElementInstanceArtifact.Builder builder){
    var fieldName = getFieldName(path);
    String dependentFileName;
    
    if(fieldName.equals(CONTRIBUTOR_IDENTIFIER_SCHEME.getValue())){
      dependentFileName = path.replace(fieldName, CONTRIBUTOR_IDENTIFIER.getValue());
      if(groupedData.containsKey(dependentFileName)
          && groupedData.get(dependentFileName)!= null){
        builder.withSingleInstanceFieldInstance(fieldName,
            fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, orcid, Optional.empty()));
      }
    } else if (fieldName.equals(CONTRIBUTOR_AFFILIATION_IDENTIFIER_SCHEME.getValue())) {
      dependentFileName = path.replace(fieldName, CONTRIBUTOR_AFFILIATION_IDENTIFIER.getValue());
      if(groupedData.containsKey(dependentFileName) && groupedData.get(dependentFileName)!= null){
        var affiliationId = groupedData.get(dependentFileName);
        if (affiliationId.startsWith("ror")){
          builder.withSingleInstanceFieldInstance(fieldName,
              fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, ror, Optional.empty()));
        } else{
          builder.withSingleInstanceFieldInstance(fieldName,
              fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, uei, Optional.empty()));
        }
      }
    }
  }

  /**
   * Set Creator Identifier Scheme to ORCiD
   * Set Creator Affiliation Scheme to ROR|UEI
   */
  private static void patchCreatorElementInstance(String path,
                                                  Map<String, String> groupedData,
                                                  FieldType fieldType,
                                                  ElementInstanceArtifact.Builder builder){
    var fieldName = getFieldName(path);
    String dependentFileName;

    if(fieldName.equals(CREATOR_IDENTIFIER_SCHEME.getValue())){
      dependentFileName = path.replace(fieldName, CREATOR_IDENTIFIER.getValue());
      if(groupedData.containsKey(dependentFileName)
          && groupedData.get(dependentFileName)!= null){
        builder.withSingleInstanceFieldInstance(fieldName,
            fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, orcid, Optional.empty()));
      }
    } else if (fieldName.equals(CREATOR_AFFILIATION_IDENTIFIER_SCHEME.getValue())) {
      dependentFileName = path.replace(fieldName, CREATOR_AFFILIATION_IDENTIFIER.getValue());
      if(groupedData.containsKey(dependentFileName) && groupedData.get(dependentFileName)!= null){
        var affiliationId = groupedData.get(dependentFileName);
        if (affiliationId.startsWith("ror")){
          builder.withSingleInstanceFieldInstance(fieldName,
              fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, ror, Optional.empty()));
        } else{
          builder.withSingleInstanceFieldInstance(fieldName,
              fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, uei, Optional.empty()));
        }
      }
    }
  }

  /**
   * Set Event Type to Created
   */
  private static void patchDateElementInstance(String path,
                                               Map<String, String> groupedData,
                                               FieldType fieldType,
                                               ElementInstanceArtifact.Builder builder){
    var fieldName = getFieldName(path);
    var dependentFileName = path.replace(fieldName, DATE.getValue());

    if(fieldName.equals(EVENT_TYPE.getValue())
        && groupedData.containsKey(dependentFileName)
        && groupedData.get(dependentFileName) != null)  {
      builder.withSingleInstanceFieldInstance(fieldName,
          fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, created, Optional.empty()));
    }
  }

  /**
   * Set Study Identifier Scheme to URL if the id is a resolvable URL
   */
  private static void patchParentStudyElementInstance(String path,
                                         Map<String, String> groupedData,
                                         FieldType fieldType,
                                         ElementInstanceArtifact.Builder builder){
    var fieldName = getFieldName(path);
    var dependentFileName = path.replace(fieldName, STUDY_IDENTIFIER.getValue());

    if(fieldName.equals(STUDY_IDENTIFIER_SCHEME.getValue())
        && groupedData.containsKey(dependentFileName)
        && isValidURL(groupedData.get(dependentFileName)))  {
      builder.withSingleInstanceFieldInstance(fieldName,
          fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, url, Optional.empty()));
    }
  }

  /**
   * Set Language to en
   */
  private static void patchTitleElementInstance(String path,
                                                FieldType fieldType,
                                                ElementInstanceArtifact.Builder builder){
    var fieldName = getFieldName(path);
    if(fieldName.equals(LANGUAGE.getValue()))  {
      builder.withSingleInstanceFieldInstance(fieldName,
          fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, "en", Optional.empty()));
    }
  }

  private static void patchLanguageElementInstance(String path,
                                                   FieldType fieldType,
                                                   ElementInstanceArtifact.Builder builder){
    var fieldName = getFieldName(path);
    if(fieldName.equals(PRIMARY_LANGUAGE.getValue()))  {
      builder.withSingleInstanceFieldInstance(fieldName,
          fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, "en", Optional.empty()));
    }
  }
  private static String getParentElementName(String path){
    return path.replaceAll("\\[\\d+\\]", "").split("/")[1];
  }

  private static String getFieldName(String path){
    String[] pathParts = path.replaceAll("\\[\\d+\\]", "").split("/");
    return pathParts[pathParts.length - 1];
  }

//  /***
//   * This method aims to set RADx-rad specific controlled term fields
//   * such as set Contributor Type to Person
//   * It adds empty field entry if it is not a specific controlled term fields
//   */
//  public static void addSpecificFields(
//      ElementInstanceArtifact.Builder elementInstanceArtifactBuilder,
//      String elementName,
//      String expectedField,
//      ElementSchemaArtifact elementSchemaArtifact,
//      Map<String, String> groupedData,
//      Map<String, Integer> elementInstanceCounts,
//      int i){
//
//    var fieldSchemaArtifact = elementSchemaArtifact.getFieldSchemaArtifact(expectedField);
//    var valueConstraints = fieldSchemaArtifact.valueConstraints();
//    var isMultiple = fieldSchemaArtifact.isMultiple();
//    var fieldType = FieldType.getFieldType(fieldSchemaArtifact);
//
//    //If the element instance is not empty, then set specific controlled term fields
//    //-----------------------start to comment out for template 2,0-----------------------------------
//    if(!isEmptyElementInstance(elementInstanceCounts, elementName, elementSchemaArtifact)){
//     if (elementName.equals(DATA_FILE_CONTRIBUTORS.getValue())  //Set Contributor Identifier Scheme to ORCiD
//          && expectedField.equals(CONTRIBUTOR_IDENTIFIER_SCHEME.getValue())
//          && groupedData.containsKey(contributorIdentifierPath)
//          && groupedData.get(contributorIdentifierPath).get(i)!= null){
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//            fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, orcid, Optional.empty()));
//      } else if (elementName.equals(DATA_FILE_CREATORS.getValue())  //Set Creator Identifier Scheme to ORCiD
//          && expectedField.equals(CREATOR_IDENTIFIER_SCHEME.getValue())
//          && groupedData.containsKey(creatorIdentifierPath)
//          && groupedData.get(creatorIdentifierPath).get(i)!= null){
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//            fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, orcid, Optional.empty()));
//      } else if (elementName.equals(DATA_FILE_CREATORS.getValue())  //Set Creator Affiliation Identifier Scheme to ROR
//          && expectedField.equals(CREATOR_AFFILIATION_IDENTIFIER_SCHEME.getValue())
//          && groupedData.containsKey(creatorAffiliationIdPath)
//          && groupedData.get(creatorAffiliationIdPath).get(i).get(0).startsWith("ror")){
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//            fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, ror, Optional.empty()));
//      } else if (elementName.equals(DATA_FILE_CREATORS.getValue()) //Set Creator Given Name
//       //---------------------end of comment out for template 2.0-----------------------------------------
////      if( elementName.equals(DATA_FILE_CREATORS.getValue())
//          && expectedField.equals(CREATOR_GIVEN_NAME.getValue())
//          && groupedData.containsKey(creatorNamePath)){
//        var name = groupedData.get(creatorNamePath).get(i).get(0);
//        if(name != null){
//          var givenName = name.split(" ")[0];
//          elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//              fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, givenName, Optional.empty()));
//        }
//      } else if (elementName.equals(DATA_FILE_CREATORS.getValue()) //Set Creator Family Name
//          && expectedField.equals(CREATOR_FAMILY_NAME.getValue())
//          && groupedData.containsKey(creatorNamePath)){
//        var name = groupedData.get(creatorNamePath).get(i).get(0);
//        if(name != null){
//          var familyName = name.split(" ")[1];
//          elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//              fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, familyName, Optional.empty()));
//        }
//      } else if (elementName.equals(DATA_FILE_DATES.getValue())  //Set Date Type to Created
//          && expectedField.equals(EVENT_TYPE.getValue())
//          && groupedData.containsKey(datePath)
//          && groupedData.get(datePath).get(i) != null)  {
//       var eventTypeInstance = fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, created, Optional.empty());
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField, eventTypeInstance);
//       //-----------------------start to comment out for template 2,0-----------------------------------
//      } else if (elementName.equals(DATA_FILE_PARENT_STUDIES.getValue())  //Set Study Identifier Scheme to URL
//          && expectedField.equals(STUDY_IDENTIFIER_SCHEME.getValue())
//          && groupedData.containsKey(studyIdentifierPath)
//          && isValidURL(groupedData.get(studyIdentifierPath).get(i).get(0))) {
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//            fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, url, Optional.empty()));
//      } else if(elementName.equals(DATA_FILE_TITLES.getValue())  //Set Data File Title Language to en
//          && expectedField.equals(LANGUAGE.getValue())){
//        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
//            fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, "en", Optional.empty()));
//       //-----------------------end of comment out for template 2,0-----------------------------------
//      } else{
//        var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildSingleEmptyInstance(fieldType, valueConstraints);
//        buildWithFieldInstanceArtifact(elementInstanceArtifactBuilder, fieldInstanceArtifact, expectedField, isMultiple);
//      }
//    } else{
//      var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildSingleEmptyInstance(fieldType, valueConstraints);
//      buildWithFieldInstanceArtifact(elementInstanceArtifactBuilder, fieldInstanceArtifact, expectedField, isMultiple);
//    }
//  }

//  private static void buildWithFieldInstanceArtifact(ElementInstanceArtifact.Builder elementInstanceArtifactBuilder, FieldInstanceArtifact fieldInstanceArtifact, String fieldName, boolean isMultiple){
//    if(isMultiple){
//      elementInstanceArtifactBuilder.withMultiInstanceFieldInstances(fieldName, List.of(fieldInstanceArtifact));
//    } else{
//      elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(fieldName, fieldInstanceArtifact);
//    }
//  }
//
//  private static boolean isEmptyElementInstance(Map<String, Integer> elementInstanceCounts, String element, ElementSchemaArtifact elementSchemaArtifact){
//    if(elementInstanceCounts.containsKey(element)){
//      return false;
//    }
//
//    var childElements = elementSchemaArtifact.getElementNames();
//    for (var childElement : childElements){
//      var childElementSchemaArtifact = elementSchemaArtifact.getElementSchemaArtifact(childElement);
//      if(!isEmptyElementInstance(elementInstanceCounts, childElement, childElementSchemaArtifact)){
//        return false;
//      }
//    }
//    return true;
//  }

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

  /***
   * This method is for Specification 1.0
   */
  private static void addDataFileSubjectsElement(Map<String, Integer> fieldCount,
                                                Map<String, String> groupedData,
                                                TemplateSchemaArtifact templateSchemaArtifact,
                                                TemplateInstanceArtifact.Builder templateInstanceArtifactBuilder) throws URISyntaxException {
    var instanceNumber = fieldCount.get(keywordPath);
    if(instanceNumber >= 0) {
      var mesh = MeshCsvReader.readCSVToMap();
      var elementInstances = new ArrayList<ElementInstanceArtifact>();
      for(int i=0; i<instanceNumber; i++){
        var elementInstanceArtifactBuilder = ElementInstanceArtifact.builder();
        var currentPath = keywordPath + "[" + i + "]";
        var keyword = groupedData.get(currentPath).trim();

        //add Data File Subjects/Subject Identifier
        FieldInstanceArtifact subjectIdentifierField;
        FieldInstanceArtifact subjectIdentifierSchemeField;
        if (mesh.containsKey(keyword)){
          var classId = mesh.get(keyword);
          subjectIdentifierField = ControlledTermFieldInstance.builder().withValue(new URI(classId)).withLabel(keyword).build();
          subjectIdentifierSchemeField = fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(TEXTFIELD, meshUri, Optional.empty());
        } else{
          subjectIdentifierField = fieldInstanceArtifactGenerator.buildSingleEmptyInstance(CONTROLLED_TERM, Optional.empty());
          subjectIdentifierSchemeField = fieldInstanceArtifactGenerator.buildSingleEmptyInstance(TEXTFIELD, Optional.empty());
        }

        elementInstanceArtifactBuilder
            .withSingleInstanceFieldInstance(SUBJECT_IDENTIFIER.getValue(), subjectIdentifierField)
            .withSingleInstanceFieldInstance(KEYWORD.getValue(), new TextFieldGenerator().buildFieldInstance(keyword))
            .withSingleInstanceFieldInstance(SUBJECT_IDENTIFIER_SCHEME.getValue(), subjectIdentifierSchemeField);

        //add context
        var elementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_SUBJECTS.getValue());
        ContextGenerator.generateElementInstanceContext(elementSchemaArtifact, elementInstanceArtifactBuilder);
        //add @id
        IdGenerator.generateElementId(elementInstanceArtifactBuilder);

        elementInstances.add(elementInstanceArtifactBuilder.build());
      }
      templateInstanceArtifactBuilder.withMultiInstanceElementInstances(DATA_FILE_SUBJECTS.getValue(), elementInstances);
    } else { //empty keywords input
      templateInstanceArtifactBuilder.withMultiInstanceElementInstances(DATA_FILE_SUBJECTS.getValue(), Collections.emptyList());
    }
  }

  /***
   * This method is for Specification 1.0
   */
  private static void addDataFileRelatedResourcesElement(Map<String, Integer> fieldCounts,
                                                Map<String, String> groupedData,
                                                TemplateSchemaArtifact templateSchemaArtifact,
                                                TemplateInstanceArtifact.Builder templateInstanceArtifactBuilder) throws URISyntaxException {
    var instanceNumber = fieldCounts.get(relatedResourceIdentifierPath);
    if(instanceNumber >= 0) {
      var elementInstances = new ArrayList<ElementInstanceArtifact>();
      var fieldInstanceGenerator = new FieldInstanceArtifactGenerator2();
      for(int i=0; i<instanceNumber; i++){
        var elementInstanceArtifactBuilder = ElementInstanceArtifact.builder();
        var currentPath = relatedResourceIdentifierPath + "[" + i + "]";
        var publicationUrl = groupedData.get(currentPath).trim();

        var idInstanceArtifact = fieldInstanceGenerator.buildSingleInstanceWithValue(TEXTFIELD, publicationUrl, Optional.empty());
        var idTypeInstanceArtifact = fieldInstanceGenerator.buildSingleInstanceWithValue(CONTROLLED_TERM, "URL", Optional.empty());
        var fileNameInstanceArtifact = fieldInstanceGenerator.buildSingleInstanceWithValue(TEXTFIELD, null, Optional.empty());
        var typeCategoryInstanceArtifact = fieldInstanceGenerator.buildSingleInstanceWithValue(CONTROLLED_TERM, null, Optional.empty());
        var relationInstanceArtifact = fieldInstanceGenerator.buildSingleInstanceWithValue(TEXTFIELD, null, Optional.empty());

        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(RELATED_RESOURCE_IDENTIFER.getValue(), idInstanceArtifact)
            .withSingleInstanceFieldInstance(RELATED_RESOURCE_IDENTIFER_TYPE.getValue(), idTypeInstanceArtifact)
            .withSingleInstanceFieldInstance(RELATED_RESOURCE_FILE_NAME.getValue(), fileNameInstanceArtifact)
            .withSingleInstanceFieldInstance(RELATED_RESOURCE_TYPE_CATEGORY.getValue(), typeCategoryInstanceArtifact)
            .withSingleInstanceFieldInstance(RELATED_RESOURCE_RELATION.getValue(), relationInstanceArtifact);

        //add context
        var elementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_RELATED_RESOURCES.getValue());
        ContextGenerator.generateElementInstanceContext(elementSchemaArtifact, elementInstanceArtifactBuilder);
        //add @id
        IdGenerator.generateElementId(elementInstanceArtifactBuilder);

        elementInstances.add(elementInstanceArtifactBuilder.build());
      }
      templateInstanceArtifactBuilder.withMultiInstanceElementInstances(DATA_FILE_RELATED_RESOURCES.getValue(), elementInstances);
    } else { //empty keywords input
      templateInstanceArtifactBuilder.withMultiInstanceElementInstances(DATA_FILE_RELATED_RESOURCES.getValue(), Collections.emptyList());
    }
  }

  /***
   * This method is for Specification 2.0
   */
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
