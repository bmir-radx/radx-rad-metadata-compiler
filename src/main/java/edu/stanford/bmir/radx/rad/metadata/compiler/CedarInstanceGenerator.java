package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.bmir.radx.rad.metadata.compiler.form.Person;
import edu.stanford.bmir.radx.rad.metadata.compiler.form.RADxRadMetadata;
import org.metadatacenter.artifacts.model.core.*;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxRadFieldsConstant.*;
import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxSpecificationMetadataConstant.*;

public class CedarInstanceGenerator {
  private final String personUri = "http://vocab.fairdatacollective.org/gdmt/Person";
  private final String orcidUri = "https://orcid.org/";
  private final String rorUri = "https://ror.org/";
  private final String projectLeaderUri = "http://vocab.fairdatacollective.org/gdmt/ProjectLeader";
  private final String createdUri = "http://vocab.fairdatacollective.org/gdmt/Created";
  private final String urlUri = "http://vocab.fairdatacollective.org/gdmt/URL";
  private final String dateType = "xsd:dateTime";
  private final String isBasedOnUri = "https://repo.metadatacenter.org/templates/c691629c-1183-4425-9a12-26201eab1a10";
  private ContextGenerator contextGenerator = new ContextGenerator();
  private EmptyArtifactChecker emptyArtifactChecker = new EmptyArtifactChecker();

  public TemplateInstanceArtifact generateTemplate(RADxRadMetadata radxRadMetadata, JsonNode templateNode) throws URISyntaxException {
    JsonSchemaArtifactReader jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    TemplateSchemaArtifact templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);

    return TemplateInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateTemplateInstanceContext(templateSchemaArtifact))
        .withIsBasedOn(new URI(isBasedOnUri))
        .withName(SCHEMA_NAME.getValue())
        .withDescription(SCHEMA_DESCRIPTION.getValue())
        .withMultiInstanceElementInstances(DATA_FILE_CONTRIBUTORS.getValue(), generateContributorElementsList(radxRadMetadata, templateSchemaArtifact))
        .withMultiInstanceElementInstances(DATA_FILE_CREATORS.getValue(), generateCreatorElementsList(radxRadMetadata, templateSchemaArtifact))
        .withMultiInstanceElementInstances(DATA_FILE_PARENT_STUDIES.getValue(), List.of(generateParentStudiesElement(radxRadMetadata, templateSchemaArtifact)))
        .withElementInstance(AUXILIARY_METADATA.getValue(), generateAuxiliaryMetadata(radxRadMetadata, templateSchemaArtifact))
        .withMultiInstanceElementInstances(DATA_FILE_SUBJECTS.getValue(), List.of(generateSubjectsElement(radxRadMetadata, templateSchemaArtifact)))
        .withMultiInstanceElementInstances(DATA_FILE_RELATED_RESOURCES.getValue(), List.of(generateRelatedResourcesElement(radxRadMetadata, templateSchemaArtifact)))
        .withMultiInstanceElementInstances(DATA_FILE_TITLES.getValue(), List.of(generateTitlesElement(radxRadMetadata, templateSchemaArtifact)))
        .withElementInstance(DATA_FILE_DATA_DICTIONARY.getValue(), generateDataDictionaryElement(radxRadMetadata, templateSchemaArtifact))
        .withMultiInstanceElementInstances(DATA_FILE_DATES.getValue(), List.of(generateDatesElement(radxRadMetadata, templateSchemaArtifact)))
        .withElementInstance(DATA_FILE_IDENTITY.getValue(), generateIdentityElement(radxRadMetadata, templateSchemaArtifact))
        .withElementInstance(DATA_FILE_LANGUAGE.getValue(), generateLanguageElement(radxRadMetadata, templateSchemaArtifact))
        .withMultiInstanceElementInstances(DATA_FILE_DESCRIPTIONS.getValue(), List.of(ElementInstanceArtifact.builder().build()))
        .withMultiInstanceElementInstances(DATA_FILE_RIGHTS.getValue(), List.of(ElementInstanceArtifact.builder().build()))
        .withMultiInstanceElementInstances(DATA_FILE_FUNDING_SOURCES.getValue(), List.of(ElementInstanceArtifact.builder().build()))
        .withMultiInstanceElementInstances(DATA_FILE_DISTRIBUTIONS.getValue(), List.of(ElementInstanceArtifact.builder().build()))
        .withElementInstance(DATA_CHARACTERISTICS_SUMMARY.getValue(), generateCharacteristicsSummaryElement(radxRadMetadata, templateSchemaArtifact))
        .withMultiInstanceElementInstances(DATA_SOURCES.getValue(), List.of(ElementInstanceArtifact.builder().build()))
        .withMultiInstanceElementInstances(DATA_STREAMS.getValue(), List.of(ElementInstanceArtifact.builder().build()))
        .withMultiInstanceElementInstances(DATA_FILE_CREATION_PROCESSES.getValue(), List.of(ElementInstanceArtifact.builder().build()))
        .withMultiInstanceElementInstances(DATA_FILE_TEMPORAL_COVERAGE.getValue(), List.of(ElementInstanceArtifact.builder().build()))
        .withMultiInstanceElementInstances(DATA_FILE_SPATIAL_COVERAGE.getValue(), List.of(ElementInstanceArtifact.builder().build()))
        .withMultiInstanceElementInstances(DATA_FILE_ELEVATION_COVERAGE.getValue(), List.of(ElementInstanceArtifact.builder().build()))
        .build();
  }

  private ElementInstanceArtifact generateSingleContributorOrCreatorElement(Person p, RadxSpecificationMetadataConstant elementName, TemplateSchemaArtifact templateSchemaArtifact) throws URISyntaxException {
    var typeField = FieldInstanceArtifact.builder()
        .withJsonLdId(new URI(personUri))
        .withLabel("Person")
        .build();
    var nameField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getName().getFullName())
        .build();
    var givenNameField = FieldInstanceArtifact.builder()
        .withJsonLdValue(getGivenName(p))
        .build();
    var familyNameField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getName().getLastName())
        .build();
    var identifierField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getOrcid())
        .build();
    FieldInstanceArtifact identifierSchemeField;
    if(p.getOrcid() != null){
      identifierSchemeField = FieldInstanceArtifact.builder()
          .withJsonLdId(new URI(orcidUri))
          .withLabel("ORCiD")
          .build();
    } else{
      identifierSchemeField = FieldInstanceArtifact.builder().build();
    }
    var affiliationField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getAffiliation().getAffiliationName())
        .build();
    var affiliationIdentifierField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getAffiliation().getAffiliationIdentifier())
        .build();
    //TODO: check
    FieldInstanceArtifact affiliationIdentifierSchemeField;
    if(p.getAffiliation().getAffiliationIdentifier() != null){
      affiliationIdentifierSchemeField = FieldInstanceArtifact.builder()
          .withJsonLdId(new URI(rorUri))
          .withLabel("ROR")
          .build();
    } else{
      affiliationIdentifierSchemeField = FieldInstanceArtifact.builder().build();
    }
    var contributorRoleField = FieldInstanceArtifact.builder()
        .withJsonLdId(new URI(projectLeaderUri))
        .withLabel("Project Leader")
        .build();
    var creatorRoleField = FieldInstanceArtifact.builder().build();
    var emailField = FieldInstanceArtifact.builder()
        .withJsonLdValue(null)
        .build();

    ElementInstanceArtifact elementInstanceArtifact;
    if(elementName.equals(DATA_FILE_CONTRIBUTORS)){
      elementInstanceArtifact = ElementInstanceArtifact.builder()
//          .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(elementName.getField())))
          .withSingleInstanceFieldInstance(CONTRIBUTOR_TYPE.getValue(), typeField)
          .withSingleInstanceFieldInstance(CONTRIBUTOR_NAME.getValue(), nameField)
          .withSingleInstanceFieldInstance(CONTRIBUTOR_GIVEN_NAME.getValue(), givenNameField)
          .withSingleInstanceFieldInstance(CONTRIBUTOR_FAMILY_NAME.getValue(), familyNameField)
          .withSingleInstanceFieldInstance(CONTRIBUTOR_IDENTIFIER.getValue(), identifierField)
          .withSingleInstanceFieldInstance(CONTRIBUTOR_IDENTIFIER_SCHEME.getValue(), identifierSchemeField)
          .withSingleInstanceFieldInstance(CONTRIBUTOR_AFFILIATION.getValue(), affiliationField)
          .withSingleInstanceFieldInstance(CONTRIBUTOR_AFFILIATION_IDENTIFIER.getValue(), affiliationIdentifierField)
          .withSingleInstanceFieldInstance(CONTRIBUTOR_AFFILIATION_IDENTIFIER_SCHEME.getValue(), affiliationIdentifierSchemeField)
          .withSingleInstanceFieldInstance(CONTRIBUTOR_EMAIL.getValue(), emailField)
          .withSingleInstanceFieldInstance(CONTRIBUTOR_ROLE.getValue(), contributorRoleField)
          .build();
    } else{
      elementInstanceArtifact = ElementInstanceArtifact.builder()
//          .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(elementName.getField())))
          .withSingleInstanceFieldInstance(CREATOR_TYPE.getValue(), typeField)
          .withSingleInstanceFieldInstance(CREATOR_NAME.getValue(), nameField)
          .withSingleInstanceFieldInstance(CREATOR_GIVEN_NAME.getValue(), givenNameField)
          .withSingleInstanceFieldInstance(CREATOR_FAMILY_NAME.getValue(), familyNameField)
          .withSingleInstanceFieldInstance(CREATOR_IDENTIFIER.getValue(), identifierField)
          .withSingleInstanceFieldInstance(CREATOR_IDENTIFIER_SCHEME.getValue(), identifierSchemeField)
          .withSingleInstanceFieldInstance(CREATOR_AFFILIATION.getValue(), affiliationField)
          .withSingleInstanceFieldInstance(CREATOR_AFFILIATION_IDENTIFIER.getValue(), affiliationIdentifierField)
          .withSingleInstanceFieldInstance(CREATOR_AFFILIATION_IDENTIFIER_SCHEME.getValue(), affiliationIdentifierSchemeField)
          .withSingleInstanceFieldInstance(CREATOR_EMAIL.getValue(), emailField)
          .withSingleInstanceFieldInstance(CREATOR_ROLE.getValue(), creatorRoleField)
          .build();
    }

    return emptyArtifactChecker.getOrEmptyElementInstanceArtifact(elementInstanceArtifact);
  }

  private List<ElementInstanceArtifact> generateContributorElementsList(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact) throws URISyntaxException {
    List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();
    for(int i = 0; i < radxRadMetadata.getContactPIs().size(); i++){
      var contributor = radxRadMetadata.getContactPIs().get(i);
      elementInstanceArtifacts.add(generateSingleContributorOrCreatorElement(contributor, DATA_FILE_CONTRIBUTORS, templateSchemaArtifact));
    }
    return  elementInstanceArtifacts;
  }

  private List<ElementInstanceArtifact> generateCreatorElementsList(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact) throws URISyntaxException {
    List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();
    for(int i = 0; i < radxRadMetadata.getContactPIs().size(); i++){
      var creator = radxRadMetadata.getContactPIs().get(i);
      elementInstanceArtifacts.add(generateSingleContributorOrCreatorElement(creator, DATA_FILE_CREATORS, templateSchemaArtifact));
    }
    return  elementInstanceArtifacts;
  }

  private ElementInstanceArtifact generateParentStudiesElement(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact){
    var studyIdentifierSchemeField = FieldInstanceArtifact.builder().build();
    var studyEndDateField = FieldInstanceArtifact.builder().build();
    var studyStartDateField = FieldInstanceArtifact.builder().build();
    var studyIdentifierField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getStudy().getNihProjectId())
        .build();
    var phsIdentifierField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getStudy().getDbGapStudyId())
        .build();
    var studyNameField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getStudy().getProjectTitle())
        .build();

    var elementInstanceArtifact = ElementInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_PARENT_STUDIES.getField())))
        .withSingleInstanceFieldInstance(STUDY_IDENTIFIER_SCHEME.getValue(), studyIdentifierSchemeField)
        .withSingleInstanceFieldInstance(STUDY_END_DATE.getValue(), studyEndDateField)
        .withSingleInstanceFieldInstance(STUDY_START_DATE.getValue(), studyStartDateField)
        .withSingleInstanceFieldInstance(STUDY_IDENTIFIER.getValue(), studyIdentifierField)
        .withSingleInstanceFieldInstance(PHS_IDENTIFIER.getValue(), phsIdentifierField)
        .withSingleInstanceFieldInstance(STUDY_NAME.getValue(), studyNameField)
        .build();

    return emptyArtifactChecker.getOrEmptyElementInstanceArtifact(elementInstanceArtifact);
  }

  private ElementInstanceArtifact generateSubjectsElement(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact){
    var subjectIdentifierField = FieldInstanceArtifact.builder().build();
    var keywordField = FieldInstanceArtifact.builder()
        //TODO make keywords a list?
        .withJsonLdValue(radxRadMetadata.getStudy().getKeywords())
        .build();
    var elementInstanceArtifact = ElementInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_SUBJECTS.getField())))
        .withSingleInstanceFieldInstance(SUBJECT_IDENTIFIER.getValue(), subjectIdentifierField)
        .withSingleInstanceFieldInstance(KEYWORD.getValue(), keywordField)
        .build();

    return emptyArtifactChecker.getOrEmptyElementInstanceArtifact(elementInstanceArtifact);
  }

  private ElementInstanceArtifact generateRelatedResourcesElement(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact) throws URISyntaxException {
    var relatedResourceIdentifier = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getStudy().getPublicationUrl())
        .build();
    FieldInstanceArtifact relatedResourceIdentifierType;
    if(radxRadMetadata.getStudy().getPublicationUrl() != null){
      relatedResourceIdentifierType = FieldInstanceArtifact.builder()
          .withJsonLdId(new URI(urlUri))
          .withLabel("URL")
          .build();
    } else{
      relatedResourceIdentifierType = FieldInstanceArtifact.builder().build();
    }

    var relatedResourceFileName = FieldInstanceArtifact.builder().build();
    var relatedResourceTypeCategory = FieldInstanceArtifact.builder().build();
    var relatedResourceRelation = FieldInstanceArtifact.builder().build();

    var elementInstanceArtifact = ElementInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_RELATED_RESOURCES.getField())))
        .withSingleInstanceFieldInstance(RELATED_RESOURCE_IDENTIFER.getValue(), relatedResourceIdentifier)
        .withSingleInstanceFieldInstance(RELATED_RESOURCE_IDENTIFER_TYPE.getValue(), relatedResourceIdentifierType)
        .withSingleInstanceFieldInstance(RELATED_RESOURCE_FILE_NAME.getValue(), relatedResourceFileName)
        .withSingleInstanceFieldInstance(RELATED_RESOURCE_TYPE_CATEGORY.getValue(), relatedResourceTypeCategory)
        .withSingleInstanceFieldInstance(RELATED_RESOURCE_RELATION.getValue(), relatedResourceRelation)
        .build();

    return emptyArtifactChecker.getOrEmptyElementInstanceArtifact(elementInstanceArtifact);
  }

  private ElementInstanceArtifact generateTitlesElement(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact){
    var titleField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getDataFile().getDataFileTitle())
        .build();
    var languageField = FieldInstanceArtifact.builder().build();

    var elementInstanceArtifact = ElementInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_TITLES.getField())))
        .withSingleInstanceFieldInstance(TITLE.getValue(), titleField)
        .withSingleInstanceFieldInstance(LANGUAGE.getValue(), languageField)
        .build();

    return emptyArtifactChecker.getOrEmptyElementInstanceArtifact(elementInstanceArtifact);
  }

  private ElementInstanceArtifact generateDataDictionaryElement(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact){
    var dataDictionaryFileNameField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getDataDictionaryFileName())
        .build();

    return ElementInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_DATA_DICTIONARY.getField())))
        .withSingleInstanceFieldInstance(RadxSpecificationMetadataConstant.DATA_DICTIONARY_FILE_NAME.getValue(), dataDictionaryFileNameField)
        .build();
  }

  private ElementInstanceArtifact generateDatesElement(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact) throws URISyntaxException {
    var dateField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getDataFile().getDataFileCreationDateTime())
        .withJsonLdType(new URI(dateType))
        .build();
    FieldInstanceArtifact eventTypeField;
    if(radxRadMetadata.getDataFile().getDataFileCreationDateTime() != null){
      eventTypeField = FieldInstanceArtifact.builder()
          .withJsonLdId(new URI(createdUri))
          .withLabel("Created")
          .build();
    } else{
      eventTypeField = FieldInstanceArtifact.builder().build();
    }

    var elementInstanceArtifact = ElementInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_DATES.getField())))
        .withSingleInstanceFieldInstance(DATA_FILE_DATES.getValue(), dateField)
        .withSingleInstanceFieldInstance(EVENT_TYPE.getValue(), eventTypeField)
        .build();

    return emptyArtifactChecker.getOrEmptyElementInstanceArtifact(elementInstanceArtifact);
  }

  private ElementInstanceArtifact generateIdentityElement(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact){
    var identifierField = FieldInstanceArtifact.builder().build();
    var identifierTypeField = FieldInstanceArtifact.builder().build();
    var fileNameField = FieldInstanceArtifact.builder().build();
    var versionField = FieldInstanceArtifact.builder().build();
    var sha256DigestField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getDataFile().getDataFileSha256Digest())
        .build();

    return ElementInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_IDENTITY.getField())))
        .withSingleInstanceFieldInstance(IDENTIFIER.getValue(), identifierField)
        .withSingleInstanceFieldInstance(IDENTIFIER_TYPE.getValue(), identifierTypeField)
        .withSingleInstanceFieldInstance(FILE_NAME.getValue(), fileNameField)
        .withSingleInstanceFieldInstance(VERSION.getValue(), versionField)
        .withSingleInstanceFieldInstance(SHA256_DIGEST.getValue(), sha256DigestField)
        .build();
  }

  private ElementInstanceArtifact generateAuxiliaryMetadata(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact){
    Map<String, FieldInstanceArtifact> attributeValueFieldInstances = new HashMap<>();
    if(radxRadMetadata.getDataFile().getDataFileName() != null) {
      attributeValueFieldInstances.put(DATA_FILE_NAME.getValue(),
          FieldInstanceArtifact.builder()
              .withJsonLdValue(radxRadMetadata.getDataFile().getDataFileName())
              .build());
    }

    if(radxRadMetadata.getNihReporter().getNihReporterAbstract() != null){
      attributeValueFieldInstances.put(NIH_REPORTER_ABSTRACT.getValue(),
          FieldInstanceArtifact.builder()
              .withJsonLdValue(radxRadMetadata.getNihReporter().getNihReporterAbstract())
              .build());
    }

    if(radxRadMetadata.getNihReporter().getNihReporterNarrative() != null){
      attributeValueFieldInstances.put(NIH_REPORTER_NARRATIVE.getValue(),
          FieldInstanceArtifact.builder()
              .withJsonLdValue(radxRadMetadata.getNihReporter().getNihReporterNarrative())
              .build());
    }

    if(radxRadMetadata.getStudy().getSubproject() != null){
      attributeValueFieldInstances.put(SUBPROJECT.getValue(),
          FieldInstanceArtifact.builder()
              .withJsonLdValue(radxRadMetadata.getStudy().getSubproject())
              .build());
    }

    if(radxRadMetadata.getStudy().getStudyIncludeProspectiveOrRetrospectiveHumanSamples() != null){
      attributeValueFieldInstances.put(STUDY_INCLUDE_PROSPECTIVE_OR_RETROSPECTIVE_HUMAN_SAMPLES.getValue(),
          FieldInstanceArtifact.builder()
              .withJsonLdValue(radxRadMetadata.getStudy().getStudyIncludeProspectiveOrRetrospectiveHumanSamples())
              .build());
    }

    if(radxRadMetadata.getStudy().getCollectedNIHMinimumCDEsForAllDatasets() != null) {
      attributeValueFieldInstances.put(COLLECTED_NIH_MINIMUM_CEDS_FOR_ALL_DATASETS.getValue(),
          FieldInstanceArtifact.builder()
              .withJsonLdValue(radxRadMetadata.getStudy().getCollectedNIHMinimumCDEsForAllDatasets())
              .build());
    }

    if(radxRadMetadata.getStudy().getSpecimenTypeUsed() != null){
      attributeValueFieldInstances.put(SPECIMEN_TYPE_USED.getValue(),
          FieldInstanceArtifact.builder()
              .withJsonLdValue(radxRadMetadata.getStudy().getSpecimenTypeUsed())
              .build());
    }

    if(radxRadMetadata.getStudy().getMethodOfDataAnalysis() != null){
      attributeValueFieldInstances.put(METHOD_OF_DATA_ANALYSIS.getValue(),
          FieldInstanceArtifact.builder()
              .withJsonLdValue(radxRadMetadata.getStudy().getMethodOfDataAnalysis())
              .build());
    }

    if(radxRadMetadata.getStudy().getDescriptionOfProject() != null){
      attributeValueFieldInstances.put(DESCRIPTION_OF_PROJECT.getValue(),
          FieldInstanceArtifact.builder()
              .withJsonLdValue(radxRadMetadata.getStudy().getDescriptionOfProject())
              .build());
    }

    var additionalCommentaryField = FieldInstanceArtifact.builder();
    if(radxRadMetadata.getStudy().getDescriptionOfProject() != null){
      additionalCommentaryField.withJsonLdValue(radxRadMetadata.getStudy().getDescriptionOfProject());
    }

    return  ElementInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(AUXILIARY_METADATA.getField())))
        .withMultiInstanceFieldInstances(ADDITIONAL_COMMENTARY.getValue(), List.of(additionalCommentaryField.build()))
        .withAttributeValueFieldInstances(DATA_FILE_DESCRIPTIVE_KEY_VALUE_PAIRS.getValue(), attributeValueFieldInstances)
        .build();
  }

  private ElementInstanceArtifact generateCharacteristicsSummaryElement(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact){
    var tableInHtmlField = FieldInstanceArtifact.builder().build();
    var tableInCsvField = FieldInstanceArtifact.builder().build();
    var tableInTsvField = FieldInstanceArtifact.builder().build();

    return ElementInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(DATA_CHARACTERISTICS_SUMMARY.getField())))
        .withSingleInstanceFieldInstance(DATA_CHARACTERISTICS_TABLE_IN_HTML.getValue(), tableInHtmlField)
        .withSingleInstanceFieldInstance(DATA_CHARACTERISTICS_TABLE_IN_CSV.getValue(), tableInCsvField)
        .withSingleInstanceFieldInstance(DATA_CHARACTERISTICS_TABLE_IN_TSV.getValue(), tableInTsvField)
        .withAttributeValueFieldInstances(DATA_CHARACTERISTICS_TABLE_IN_KEY_VALUE_PAIRS.getValue(), new HashMap<>())
        .build();
  }

  private ElementInstanceArtifact generateLanguageElement(RADxRadMetadata radxRadMetadata, TemplateSchemaArtifact templateSchemaArtifact){
    var primaryLangField = FieldInstanceArtifact.builder().build();
    var otherLangField = FieldInstanceArtifact.builder().build();

    return ElementInstanceArtifact.builder()
//        .withJsonLdContext(contextGenerator.generateElementInstanceContext(templateSchemaArtifact.getElementSchemaArtifact(DATA_FILE_LANGUAGE.getField())))
        .withSingleInstanceFieldInstance(PRIMARY_LANGUAGE.getValue(), primaryLangField)
        //TODO multiple instances
        .withMultiInstanceFieldInstances(OTHER_LANGUAGES.getValue(), List.of(otherLangField))
        .build();
  }

  private String getGivenName(Person p){
    String firstName = p.getName().getFirstName();
    String middleName = p.getName().getMiddleName();
    if(p.getName().getFirstName() != null && p.getName().getMiddleName() != null){
      return firstName + " " + middleName;
    } else if (p.getName().getFirstName() != null && p.getName().getMiddleName() == null) {
      return firstName;
    } else if (p.getName().getFirstName() == null && p.getName().getMiddleName() != null) {
      return middleName;
    } else{
      return null;
    }
  }
}
