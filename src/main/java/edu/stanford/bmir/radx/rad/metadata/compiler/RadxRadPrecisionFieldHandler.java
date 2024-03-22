package edu.stanford.bmir.radx.rad.metadata.compiler;

import edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators.TextFieldGenerator;
import org.metadatacenter.artifacts.model.core.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
  private final static String contributorAffiliationIdPath = "/Data File Contributors/Contributor Affiliation Identifier";
  private final static String creatorIdentifierPath = "/Data File Creators/Creator Identifier";
  private final static String creatorAffiliationIdPath = "/Data File Creators/Creator Affiliation Identifier";
  private final static String relatedResourceIdentifierPath = "/Data File Related Resources/Related Resource Identifier";
  private final static String datePath = "/Data File Dates/Date";
  private final static String studyIdentifierPath = "/Data File Parent Studies/Study Identifier";
  private static final FieldInstanceArtifactGenerator fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator();

  /***
   * This method aims to set RADx-rad specific controlled term fields
   * such as set Contributor Type to Person
   * It adds empty field entry if it is not a specific controlled term fields
   */
  public static void addSpecificControlledTerms(
      ElementInstanceArtifact.Builder elementInstanceArtifactBuilder,
      String elementName,
      String expectedField,
      ElementSchemaArtifact elementSchemaArtifact,
      Map<String, Map<Integer, String>> groupedData,
      Map<String, Integer> elementInstanceCounts,
      int i){

    var fieldSchemaArtifact = elementSchemaArtifact.getFieldSchemaArtifact(expectedField);
    var isMultiple = fieldSchemaArtifact.isMultiple();
    var fieldType = FieldType.getFieldType(fieldSchemaArtifact);

    //If the element instance has value, then set specific controlled term fields
    if(!isEmptyElementInstance(elementInstanceCounts, elementName, elementSchemaArtifact)){
      if ((elementName.equals(DATA_FILE_CONTRIBUTORS.getValue()) && expectedField.equals(CONTRIBUTOR_TYPE.getValue())) ||
          (elementName.equals(DATA_FILE_CREATORS.getValue()) && expectedField.equals(CREATOR_TYPE.getValue()))){
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, person, Optional.empty()));
      } else if (elementName.equals(DATA_FILE_CONTRIBUTORS.getValue())
          && expectedField.equals(CONTRIBUTOR_IDENTIFIER_SCHEME.getValue())
          && groupedData.containsKey(contributorIdentifierPath)
          && groupedData.get(contributorIdentifierPath).get(i)!= null){
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, orcid, Optional.empty()));
      } else if (elementName.equals(DATA_FILE_CREATORS.getValue())
          && expectedField.equals(CREATOR_IDENTIFIER_SCHEME.getValue())
          && groupedData.containsKey(creatorIdentifierPath)
          && groupedData.get(creatorIdentifierPath).get(i)!= null){
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, orcid, Optional.empty()));
      } else if (elementName.equals(DATA_FILE_RELATED_RESOURCES.getValue())
          && expectedField.equals(RELATED_RESOURCE_IDENTIFER_TYPE.getValue())
          && groupedData.containsKey(relatedResourceIdentifierPath)
          && groupedData.get(relatedResourceIdentifierPath).get(i) != null) {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, url, Optional.empty()));
      } else if (elementName.equals(DATA_FILE_DATES.getValue())
          && expectedField.equals(EVENT_TYPE.getValue())
          && groupedData.containsKey(datePath)
          && groupedData.get(datePath).get(i) != null)  {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, created, Optional.empty()));
      } else if (elementName.equals(DATA_FILE_PARENT_STUDIES.getValue())
          && expectedField.equals(STUDY_IDENTIFIER_SCHEME.getValue())
          && groupedData.containsKey(studyIdentifierPath)
          && isValidURL(groupedData.get(studyIdentifierPath).get(i))) {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, url, Optional.empty()));
      } else if(elementName.equals(DATA_FILE_TITLES.getValue())
          && expectedField.equals(LANGUAGE.getValue())){
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            fieldInstanceArtifactGenerator.buildFieldInstanceWithValues(fieldType, "en", Optional.empty()));
      } else{
        var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildEmptyFieldInstance(fieldType);
        buildWithFieldInstanceArtifact(elementInstanceArtifactBuilder, fieldInstanceArtifact, expectedField, isMultiple);
      }
    } else{
      var fieldInstanceArtifact = fieldInstanceArtifactGenerator.buildEmptyFieldInstance(fieldType);
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

  public static void addDataFileSubjectsElement(String input, TemplateSchemaArtifact templateSchemaArtifact, TemplateInstanceArtifact.Builder templateInstanceArtifactBuilder) throws URISyntaxException {
    if(input != null) {
      var mesh = MeshCsvReader.readCSVToMap();
      String[] keywords = input.split(",");
      var elementInstances = new ArrayList<ElementInstanceArtifact>();
      for(var keyword: keywords){
        var elementInstanceArtifactBuilder = ElementInstanceArtifact.builder();
        keyword = keyword.trim();

        //add Data File Subjects/Subject Identifier
        FieldInstanceArtifact subjectIdentifierField;
        if (mesh.containsKey(keyword)){
          var classId = mesh.get(keyword);
          subjectIdentifierField = FieldInstanceArtifact.controlledTermFieldInstanceBuilder().withValue(new URI(classId)).withLabel(keyword).build();

        } else{
          subjectIdentifierField = FieldInstanceArtifact.controlledTermFieldInstanceBuilder().build();
        }
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(SUBJECT_IDENTIFIER.getValue(), subjectIdentifierField);

        //add Keyword
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(KEYWORD.getValue(), new TextFieldGenerator().buildWithValue(keyword));

        //add Subject Identifier Scheme
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(SUBJECT_IDENTIFIER_SCHEME.getValue(), new TextFieldGenerator().buildWithValue(meshUri));

        //add context
        var elementSchemaArtifact = templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_SUBJECTS.getValue());
        ContextGenerator.generateElementInstanceContext(elementSchemaArtifact, elementInstanceArtifactBuilder);

        //add @id
        IdGenerator.generateElementId(elementInstanceArtifactBuilder);

        elementInstances.add(elementInstanceArtifactBuilder.build());
      }

      templateInstanceArtifactBuilder.withMultiInstanceElementInstances(DATA_FILE_SUBJECTS.getValue(), elementInstances);
    }
  }
}
