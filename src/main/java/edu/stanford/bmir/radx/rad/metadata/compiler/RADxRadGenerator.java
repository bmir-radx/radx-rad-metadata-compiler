package edu.stanford.bmir.radx.rad.metadata.compiler;

import edu.stanford.bmir.radx.rad.metadata.compiler.form.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxRadFieldsConstant.*;

public class RADxRadGenerator {
  public RADxRadMetadata generateRADxRadMetadata(Map<String, String> dataMap){
    return new RADxRadMetadata.Builder()
        .setContactPIs(generateContactPIs(dataMap))
        .setCreators(generateCreators(dataMap))
        .setNihReporter(generateNIHReporter(dataMap))
        .setStudy(generateStudy(dataMap))
        .setDataFile(generateDataFile(dataMap))
        .setDataDictionaryFileName(dataMap.get(DATA_DICTIONARY_FILE_NAME.getValue()))
        .build();
  }

  private List<Person> generateContactPIs(Map<String, String> dataMap) {
    List<Person> contactPIs = new ArrayList<>();

    Set<String> piIndices = dataMap.keySet().stream()
        .filter(key -> key.startsWith("pi_"))
        .map(key -> key.replaceAll("\\D+", "")) // Replace all non-digits
        .collect(Collectors.toSet());

    for (String index : piIndices) {
      // Set properties for the creator
      Name name = new Name.Builder()
          .setFullName(dataMap.get(PI_FULLNAME.getValue() + index))
          .setFirstName(dataMap.get(PI_FIRSTNAME.getValue() + index))
          .setMiddleName(dataMap.get(PI_MIDDLENAME.getValue() + index))
          .setLastName(dataMap.get(PI_LASTNAME.getValue() + index))
          .build();

      Affiliation affiliation = new Affiliation.Builder()
          .setAffiliationName(dataMap.get(PI_INST_NAME.getValue() + index))
          .setAffiliationIdentifier(dataMap.get(PI_INST_ID.getValue() + index))
          .setAffiliationIdentifierScheme(dataMap.get(PI_INST_ID_TYPE + index))
          .build();

      Person contactPI = new Person.Builder()
          .setName(name)
          .setAffiliation(affiliation)
          .setProfileId(dataMap.get(PI_PROFILE_ID.getValue() + index))
          .setOrcid(dataMap.get(PI_ORCID.getValue() + index))
          .setRole(dataMap.get(PI_ROLE.getValue() + index))
          .build();

      contactPIs.add(contactPI);
    }

    return contactPIs;
  }
  private List<Person> generateCreators(Map<String, String> dataMap) {
    List<Person> creators = new ArrayList<>();

    Set<String> creatorIndices = dataMap.keySet().stream()
        .filter(key -> key.startsWith("creator_"))
        .map(key -> key.replaceAll("\\D+", "")) // Replace all non-digits
        .collect(Collectors.toSet());

    for (String index : creatorIndices) {
      // Set properties for the creator
      Name name = new Name.Builder()
          .setFullName(dataMap.get(CREATOR_FULLNAME.getValue() + index))
          .setFirstName(dataMap.get(CREATOR_FULLNAME.getValue() + index))
          .setMiddleName(dataMap.get(CREATOR_FULLNAME.getValue() + index))
          .setLastName(dataMap.get(CREATOR_FULLNAME.getValue() + index))
          .build();

      Affiliation affiliation = new Affiliation.Builder()
          .setAffiliationName(dataMap.get(CREATOR_AFFILIATION_NAME.getValue() + index))
          .setAffiliationIdentifier(dataMap.get(CREATOR_AFFILIATION_IDENTIFIER.getValue() + index))
          .build();

      Person creator = new Person.Builder()
          .setName(name)
          .setAffiliation(affiliation)
          .setProfileId(dataMap.get(CREATOR_PROFILE_ID.getValue() + index))
          .setOrcid(dataMap.get(CREATOR_ORCID.getValue() + index))
          .build();

      creators.add(creator);
    }

    return creators;
  }

  private NIHReporter generateNIHReporter(Map<String, String> dataMap){
    return new NIHReporter.Builder()
        .setNihReporterAbstract(dataMap.get(NIH_REPORTER_ABSTRACT.getValue()))
        .setNihReporterNarrative(dataMap.get(NIH_REPORTER_NARRATIVE.getValue()))
        .build();
  }

  private Study generateStudy(Map<String, String> dataMap){
    return new Study.Builder()
        .setProjectTitle(dataMap.get(PROJECT_TITLE.getValue()))
        .setNihProjectId(dataMap.get(NIH_PROJECT_ID.getValue()))
        .setSubproject(dataMap.get(SUBPROJECT.getValue()))
        .setDbGapStudyId(dataMap.get(DBGAP_STUDY_ID.getValue()))
        .setDescriptionOfProject(dataMap.get(DESCRIPTION_OF_PROJECT.getValue()))
        .setKeywords(dataMap.get(KEYWORDS.getValue()))
        .setPublicationUrl(dataMap.get(PUBLICATION_URL.getValue()))
        .setStudyIncludeProspectiveOrRetrospectiveHumanSamples(STUDY_INCLUDE_PROSPECTIVE_OR_RETROSPECTIVE_HUMAN_SAMPLES.getValue())
        .setCollectedNIHMinimumCDEsForAllDatasets(dataMap.get(COLLECTED_NIH_MINIMUM_CEDS_FOR_ALL_DATASETS.getValue()))
        .setSpecimenTypeUsed(dataMap.get(SPECIMEN_TYPE_USED.getValue()))
        .setMethodOfDataAnalysis(dataMap.get(METHOD_OF_DATA_ANALYSIS.getValue()))
        .build();
  }

  private DataFile generateDataFile(Map<String, String> dataMap){
    return new DataFile.Builder()
        .setDataFileTitle(dataMap.get(DATA_FILE_TITLE.getValue()))
        .setDataFileName(dataMap.get(DATA_FILE_NAME.getValue()))
        .setDataFileCreationDateTime(dataMap.get(DATA_FILE_CREATION_DATETIME.getValue()))
        .setDataFileSha256Digest(dataMap.get(DATA_FILE_SHA256_DIGEST.getValue()))
        .build();
  }
}
