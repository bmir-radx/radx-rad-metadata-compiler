package edu.stanford.bmir.radx.rad.metadata.compiler;

import edu.stanford.bmir.radx.rad.metadata.compiler.form.Person;
import edu.stanford.bmir.radx.rad.metadata.compiler.form.RADxRadMetadata;
import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxSpecificationMetadataConstant.*;

public class CedarInstanceGenerator {
  private final String personUri = "http://vocab.fairdatacollective.org/gdmt/Person";
  private final String orcidUri = "https://orcid.org/";
  private final String rorUri = "https://ror.org/";
  private final String projectLeaderUri = "http://vocab.fairdatacollective.org/gdmt/ProjectLeader";
  private final String createdUri = "http://vocab.fairdatacollective.org/gdmt/Created";
  private final String urlUri = "http://vocab.fairdatacollective.org/gdmt/URL";

  public TemplateInstanceArtifact generateTemplate(RADxRadMetadata radxRadMetadata) throws URISyntaxException {
    return TemplateInstanceArtifact.builder()
        .withElementInstances(DATA_FILE_CONTRIBUTORS.getField(), generateContributorElementsList(radxRadMetadata))
        .withElementInstances(DATA_FILE_CREATORS.getField(), generateCreatorElementsList(radxRadMetadata))
        .withElementInstance(DATA_FILE_PARENT_STUDIES.getField(), generateParentStudiesElement(radxRadMetadata))
        .build();

  }

  private ElementInstanceArtifact generateSingleContributorOrCreatorElement(Person p, RadxSpecificationMetadataConstant elementName) throws URISyntaxException {
    var typeField = FieldInstanceArtifact.builder()
        .withJsonLdId(new URI(personUri))
        .withLabel("Person")
        .build();
    var nameField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getName().getFullName())
        .build();
    var givenNameField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getName().getFirstName() + p.getName().getMiddleName())
        .build();
    var familyNameField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getName().getLastName())
        .build();
    var identifierField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getOrcid())
        .build();
    var identifierSchemeField = FieldInstanceArtifact.builder()
        .withJsonLdId(new URI(orcidUri))
        .withLabel("ORCiD")
        .build();
    var affiliationField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getAffiliation().getAffiliationName())
        .build();
    var affiliationIdentifierField = FieldInstanceArtifact.builder()
        .withJsonLdValue(p.getAffiliation().getAffiliationIdentifier())
        .build();
    //TODO: check
    var affiliationIdentifierSchemeField = FieldInstanceArtifact.builder()
        .withJsonLdId(new URI(rorUri))
        .withLabel("ROR")
        .build();
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
          .withFieldInstance(CONTRIBUTOR_TYPE.getField(), typeField)
          .withFieldInstance(CONTRIBUTOR_NAME.getField(), nameField)
          .withFieldInstance(CONTRIBUTOR_GIVEN_NAME.getField(), givenNameField)
          .withFieldInstance(CONTRIBUTOR_FAMILY_NAME.getField(), familyNameField)
          .withFieldInstance(CONTRIBUTOR_IDENTIFIER.getField(), identifierField)
          .withFieldInstance(CONTRIBUTOR_IDENTIFIER_SCHEME.getField(), identifierSchemeField)
          .withFieldInstance(CONTRIBUTOR_AFFILIATION.getField(), affiliationField)
          .withFieldInstance(CONTRIBUTOR_AFFILIATION_IDENTIFIER.getField(), affiliationIdentifierField)
          .withFieldInstance(CONTRIBUTOR_AFFILIATION_IDENTIFIER_SCHEME.getField(), affiliationIdentifierSchemeField)
          .withFieldInstance(CONTRIBUTOR_EMAIL.getField(), emailField)
          .withFieldInstance(CONTRIBUTOR_ROLE.getField(), contributorRoleField)
          .build();
    } else{
      elementInstanceArtifact = ElementInstanceArtifact.builder()
          .withFieldInstance(CREATOR_TYPE.getField(), typeField)
          .withFieldInstance(CREATOR_NAME.getField(), nameField)
          .withFieldInstance(CREATOR_GIVEN_NAME.getField(), givenNameField)
          .withFieldInstance(CREATOR_FAMILY_NAME.getField(), familyNameField)
          .withFieldInstance(CREATOR_IDENTIFIER.getField(), identifierField)
          .withFieldInstance(CREATOR_IDENTIFIER_SCHEME.getField(), identifierSchemeField)
          .withFieldInstance(CREATOR_AFFILIATION.getField(), affiliationField)
          .withFieldInstance(CREATOR_AFFILIATION_IDENTIFIER.getField(), affiliationIdentifierField)
          .withFieldInstance(CREATOR_AFFILIATION_IDENTIFIER_SCHEME.getField(), affiliationIdentifierSchemeField)
          .withFieldInstance(CREATOR_EMAIL.getField(), emailField)
          .withFieldInstance(CREATOR_ROLE.getField(), creatorRoleField)
          .build();
    }

    return elementInstanceArtifact;
  }

  private List<ElementInstanceArtifact> generateContributorElementsList(RADxRadMetadata radxRadMetadata) throws URISyntaxException {
    List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();
    for(int i = 0; i < radxRadMetadata.getContactPIs().size(); i++){
      var contributor = radxRadMetadata.getContactPIs().get(i);
      elementInstanceArtifacts.add(generateSingleContributorOrCreatorElement(contributor, DATA_FILE_CONTRIBUTORS));
    }
    return  elementInstanceArtifacts;
  }

  private List<ElementInstanceArtifact> generateCreatorElementsList(RADxRadMetadata radxRadMetadata) throws URISyntaxException {
    List<ElementInstanceArtifact> elementInstanceArtifacts = new ArrayList<>();
    for(int i = 0; i < radxRadMetadata.getContactPIs().size(); i++){
      var creator = radxRadMetadata.getContactPIs().get(i);
      elementInstanceArtifacts.add(generateSingleContributorOrCreatorElement(creator, DATA_FILE_CREATORS));
    }
    return  elementInstanceArtifacts;
  }

  private ElementInstanceArtifact generateParentStudiesElement(RADxRadMetadata radxRadMetadata){
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

    return ElementInstanceArtifact.builder()
        .withFieldInstance(STUDY_IDENTIFIER_SCHEME.getField(), studyIdentifierSchemeField)
        .withFieldInstance(STUDY_END_DATE.getField(), studyEndDateField)
        .withFieldInstance(STUDY_START_DATE.getField(), studyStartDateField)
        .withFieldInstance(STUDY_IDENTIFIER.getField(), studyIdentifierField)
        .withFieldInstance(PHS_IDENTIFIER.getField(), phsIdentifierField)
        .withFieldInstance(STUDY_NAME.getField(), studyNameField)
        .build();
  }

  private ElementInstanceArtifact generateSubjectsElement(RADxRadMetadata radxRadMetadata){
    var subjectIdentifierField = FieldInstanceArtifact.builder().build();
    var keywordField = FieldInstanceArtifact.builder()
        //TODO make keywords a list?
        .withJsonLdValue(radxRadMetadata.getStudy().getKeywords())
        .build();
    return ElementInstanceArtifact.builder()
        .withFieldInstance(SUBJECT_IDENTIFIER.getField(), subjectIdentifierField)
        .withFieldInstance(KEYWORD.getField(), keywordField)
        .build();
  }

  private ElementInstanceArtifact generateRelatedResourcesElement(RADxRadMetadata radxRadMetadata) throws URISyntaxException {
    var relatedResourceIdentifier = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getStudy().getPublicationUrl())
        .build();
    var relatedResourceIdentifierType = FieldInstanceArtifact.builder()
        .withJsonLdId(new URI(urlUri))
        .withLabel("URL")
        .build();
    var relatedResourceFileName = FieldInstanceArtifact.builder().build();
    var relatedResourceTypeCategory = FieldInstanceArtifact.builder().build();
    var relatedResourceRelation = FieldInstanceArtifact.builder().build();

    return ElementInstanceArtifact.builder()
        .withFieldInstance(RELATED_RESOURCE_IDENTIFER.getField(), relatedResourceIdentifier)
        .withFieldInstance(RELATED_RESOURCE_IDENTIFER_TYPE.getField(), relatedResourceIdentifierType)
        .withFieldInstance(RELATED_RESOURCE_FILE_NAME.getField(), relatedResourceFileName)
        .withFieldInstance(RELATED_RESOURCE_TYPE_CATEGORY.getField(), relatedResourceTypeCategory)
        .withFieldInstance(RELATED_RESOURCE_RELATION.getField(), relatedResourceRelation)
        .build();
  }

  private ElementInstanceArtifact generateTitlesElement(RADxRadMetadata radxRadMetadata){
    var titleField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getDataFile().getDataFileTitle())
        .build();
    var languageField = FieldInstanceArtifact.builder().build();

    return ElementInstanceArtifact.builder()
        .withFieldInstance(TITLE.getField(), titleField)
        .withFieldInstance(LANGUAGE.getField(), languageField)
        .build();
  }

  private ElementInstanceArtifact generateDataDictionaryElement(RADxRadMetadata radxRadMetadata){
    var dataDictionaryFileNameField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getDataDictionaryFileName())
        .build();

    return ElementInstanceArtifact.builder()
        .withFieldInstance(DATA_DICTIONARY_FILE_NAME.getField(), dataDictionaryFileNameField)
        .build();
  }

  private ElementInstanceArtifact generateDatesElement(RADxRadMetadata radxRadMetadata) throws URISyntaxException {
    var dateField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getDataFile().getDataFileCreationDateTime())
        .build();
    var eventTypeField = FieldInstanceArtifact.builder()
        .withJsonLdId(new URI(createdUri))
        .withLabel("Created")
        .build();

    return ElementInstanceArtifact.builder()
        .withFieldInstance(DATA_FILE_DATES.getField(), dateField)
        .withFieldInstance(EVENT_TYPE.getField(), eventTypeField)
        .build();
  }

  private ElementInstanceArtifact generateIdentityElement(RADxRadMetadata radxRadMetadata){
    var identifierField = FieldInstanceArtifact.builder().build();
    var identifierTypeField = FieldInstanceArtifact.builder().build();
    var fileNameField = FieldInstanceArtifact.builder().build();
    var versionField = FieldInstanceArtifact.builder().build();
    var sha256DigestField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getDataFile().getDataFileSha256Digest())
        .build();

    return ElementInstanceArtifact.builder()
        .withFieldInstance(IDENTIFIER.getField(), identifierField)
        .withFieldInstance(IDENTIFIER_TYPE.getField(), identifierTypeField)
        .withFieldInstance(FILE_NAME.getField(), fileNameField)
        .withFieldInstance(VERSION.getField(), versionField)
        .withFieldInstance(SHA256_DIGEST.getField(), sha256DigestField)
        .build();
  }

  private ElementInstanceArtifact generateAuxiliaryMetadata(RADxRadMetadata radxRadMetadata){
    //TODO attribute values
    var additionalCommentaryField = FieldInstanceArtifact.builder()
        .withJsonLdValue(radxRadMetadata.getStudy().getDescriptionOfProject())
        .build();
    return ElementInstanceArtifact.builder()
        .withFieldInstance(ADDITIONAL_COMMENTARY.getField(), additionalCommentaryField)
        .build();
  }
}
