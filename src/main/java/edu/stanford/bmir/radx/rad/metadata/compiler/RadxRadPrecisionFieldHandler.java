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

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxSpecificationMetadataConstant.*;

public class RadxRadPrecisionFieldHandler {
  private final static String person = "Person";
  private final static String orcid = "ORCiD";
  private final static String ror = "ROR";
  private final static String url = "URL";
  private final static String created = "Created";
  private final static String meshUri =  "http://purl.bioontology.org/ontology/MESH";
  private static final ElementInstanceArtifactGenerator ARTIFACT_INSTANCE_GENERATOR = new ElementInstanceArtifactGenerator();
  private static final FieldInstanceArtifactGenerator fieldInstanceArtifactGenerator = new FieldInstanceArtifactGenerator();

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
      Map<String, List<String>> fields,
      ElementSchemaArtifact elementSchemaArtifact) throws URISyntaxException {

    var fieldSchemaArtifact = elementSchemaArtifact.getFieldSchemaArtifact(expectedField);
    var valueConstraint = fieldSchemaArtifact.valueConstraints();
    var isMultiple = fieldSchemaArtifact.isMultiple();
    var inputType = fieldSchemaArtifact.fieldUi().inputType();
    var fieldType = FieldType.getFieldType(inputType);
    if(valueConstraint.isPresent() && valueConstraint.get().isControlledTermValueConstraint()){
      fieldType = FieldType.CONTROLLED_TERM;
    }

    var controlledTermMap = MapInitializer.createControlledTermsMap();
    String rorPrefix = controlledTermMap.get(ror);
    //If the element instance has value, then set specific controlled term fields
    if(isNonEmptyElement(fields)){
      if ((elementName.equals(DATA_FILE_CONTRIBUTORS.getValue()) && expectedField.equals(CONTRIBUTOR_TYPE.getValue())) ||
          (elementName.equals(DATA_FILE_CREATORS.getValue()) && expectedField.equals(CREATOR_TYPE.getValue()))){
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.controlledTermFieldInstanceBuilder()
                .withValue(new URI(controlledTermMap.get(person)))
                .withLabel(person)
                .build());
      } else if (elementName.equals(DATA_FILE_CONTRIBUTORS.getValue())
          && expectedField.equals(CONTRIBUTOR_IDENTIFIER_SCHEME.getValue())
          && fields.containsKey(CONTRIBUTOR_IDENTIFIER.getValue())
          && fields.get(CONTRIBUTOR_IDENTIFIER.getValue()).get(0)!= null){
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.controlledTermFieldInstanceBuilder()
                .withValue(new URI(controlledTermMap.get(orcid)))
                .withLabel(orcid)
                .build());
      } else if (elementName.equals(DATA_FILE_CREATORS.getValue())
          && expectedField.equals(CREATOR_IDENTIFIER_SCHEME.getValue())
          && fields.containsKey(CREATOR_IDENTIFIER.getValue())
          && fields.get(CREATOR_IDENTIFIER.getValue()).get(0)!= null){
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.controlledTermFieldInstanceBuilder()
                .withValue(new URI(controlledTermMap.get(orcid)))
                .withLabel(orcid)
                .build());
      } else if (elementName.equals(DATA_FILE_RELATED_RESOURCES.getValue())
          && expectedField.equals(RELATED_RESOURCE_IDENTIFER_TYPE.getValue())
          && fields.containsKey(RELATED_RESOURCE_IDENTIFER.getValue())
          && fields.get(RELATED_RESOURCE_IDENTIFER.getValue()).get(0) != null) {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.controlledTermFieldInstanceBuilder()
                .withValue(new URI(controlledTermMap.get(url)))
                .withLabel(url)
                .build());
      } else if (elementName.equals(DATA_FILE_DATES.getValue())
          && expectedField.equals(EVENT_TYPE.getValue())
          && fields.containsKey(DATE.getValue())
          && fields.get(DATE.getValue()).get(0) != null)  {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.controlledTermFieldInstanceBuilder()
                .withValue(new URI(controlledTermMap.get(created)))
                .withLabel(created)
                .build());
      } else if (elementName.equals(DATA_FILE_PARENT_STUDIES.getValue())
          && expectedField.equals(STUDY_IDENTIFIER_SCHEME.getValue())
          && fields.containsKey(STUDY_IDENTIFIER.getValue())
          && isValidURL(fields.get(STUDY_IDENTIFIER.getValue()).get(0))) {
        elementInstanceArtifactBuilder.withSingleInstanceFieldInstance(expectedField,
            FieldInstanceArtifact.controlledTermFieldInstanceBuilder()
                .withValue(new URI(controlledTermMap.get(url)))
                .withLabel(url)
                .build());
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

  private static boolean isNonEmptyElement(Map<String, List<String>> fields){
    for(Map.Entry<String, List<String>> entry : fields.entrySet()){
      for(var value : entry.getValue()){
        if (value != null){
          return true;
        }
      }
    }
    return false;
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
