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
  private static final FieldInstanceArtifactGenerator fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator();

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
    var parentElementName = getParentElementName(path);
    var parentElementPath = "/" + parentElementName;
    var fieldType = FieldType.getFieldType(fieldSchemaArtifact);

    if(elementCount.containsKey(parentElementPath)){
      if(parentElementName.equals(DATA_FILE_CONTRIBUTORS.getValue())){
        patchContributorElementInstance(path, groupedData, fieldType, builder);
      } else if (parentElementName.equals(DATA_FILE_CREATORS.getValue())) {
        patchCreatorElementInstance(path, groupedData, fieldType, builder);
      } else if (parentElementName.equals(DATA_FILE_DATES.getValue())) {
        patchDateElementInstance(path, groupedData, fieldType, builder);
      } else if (parentElementName.equals(DATA_FILE_PARENT_STUDIES.getValue())) {
        patchParentStudyElementInstance(path, groupedData, fieldType, builder);
      } else if (parentElementName.equals(DATA_FILE_TITLES.getValue())) {
        patchTitleElementInstance(path, fieldType, builder);
      }
    }

    if (parentElementName.equals(DATA_FILE_LANGUAGE.getValue())) {
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
    String dependentFieldPath;
    
    if(fieldName.equals(CONTRIBUTOR_IDENTIFIER_SCHEME.getValue())){
      dependentFieldPath = getDependentFieldPath(path, fieldName, CONTRIBUTOR_IDENTIFIER.getValue());
      if(groupedData.containsKey(dependentFieldPath)
          && groupedData.get(dependentFieldPath)!= null){
        builder.withSingleInstanceFieldInstance(fieldName,
            fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, orcid, Optional.empty()));
      }
    } else if (fieldName.equals(CONTRIBUTOR_AFFILIATION_IDENTIFIER_SCHEME.getValue())) {
      dependentFieldPath = getDependentFieldPath(path, fieldName, CONTRIBUTOR_AFFILIATION_IDENTIFIER.getValue());
      if(groupedData.containsKey(dependentFieldPath) && groupedData.get(dependentFieldPath)!= null){
        var affiliationId = groupedData.get(dependentFieldPath);
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
    String dependentFieldPath;

    if(fieldName.equals(CREATOR_IDENTIFIER_SCHEME.getValue())){
      dependentFieldPath = getDependentFieldPath(path, fieldName, CREATOR_IDENTIFIER.getValue());
      if(groupedData.containsKey(dependentFieldPath)
          && groupedData.get(dependentFieldPath)!= null){
        builder.withSingleInstanceFieldInstance(fieldName,
            fieldInstanceArtifactGenerator.buildSingleInstanceWithValue(fieldType, orcid, Optional.empty()));
      }
    } else if (fieldName.equals(CREATOR_AFFILIATION_IDENTIFIER_SCHEME.getValue())) {
      dependentFieldPath = getDependentFieldPath(path, fieldName, CREATOR_AFFILIATION_IDENTIFIER.getValue());
      if(groupedData.containsKey(dependentFieldPath) && groupedData.get(dependentFieldPath)!= null){
        var affiliationId = groupedData.get(dependentFieldPath);
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
    var dependentFieldPath = getDependentFieldPath(path, fieldName, DATE.getValue());

    if(fieldName.equals(EVENT_TYPE.getValue())
        && groupedData.containsKey(dependentFieldPath)
        && groupedData.get(dependentFieldPath) != null)  {
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
    var dependentFieldPath = getDependentFieldPath(path, fieldName, STUDY_IDENTIFIER.getValue());

    if(fieldName.equals(STUDY_IDENTIFIER_SCHEME.getValue())
        && groupedData.containsKey(dependentFieldPath)
        && isValidURL(groupedData.get(dependentFieldPath)))  {
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
    if(instanceNumber != null && instanceNumber >= 0) {
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
    if(instanceNumber != null && instanceNumber >= 0) {
      var elementInstances = new ArrayList<ElementInstanceArtifact>();
      var fieldInstanceGenerator = new FieldInstanceArtifactGenerator();
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

  private static String getDependentFieldPath(String originPath, String currentField, String replaceField){
    return originPath.replace(currentField, replaceField) + "[0]";
  }
}
