package edu.stanford.bmir.radx.rad.metadata.compiler;

public enum RadxRadFieldsConstant {
  PI_FULLNAME("pi_fullname_"),
  PI_FIRSTNAME("pi_firstname_"),
  PI_MIDDLENAME("pi_middlename_"),
  PI_LASTNAME("pi_lastname_"),
  PI_PROFILE_ID("pi_profileid_"),
  PI_ORCID("pi_orcid_"),
  PI_ROLE("pi_role_"),
  PI_INST_NAME("pi_inst_name_"),
  PI_INST_ID("pi_inst_id_"),
  PI_INST_ID_TYPE("pi_inst_id_type_"),
  CREATOR_FULLNAME("creator_fullname_"),
  CREATOR_FIRSTNAME("creator_firstname_"),
  CREATOR_MIDDLENAME("creator_middlename_"),
  CREATOR_LASTNAME("creator_lastname_"),
  CREATOR_PROFILE_ID("creator_profileid_"),
  CREATOR_ORCID("creator_orcid_"),
  CREATOR_INST_ID("creator_inst_id_"),
  CREATOR_INST_ID_TYPE("creator_inst_id_type_"),
  CREATOR_INST_NAME("creator_inst_name_"),
  PROJECT_TITLE("project_title"),
  SUBPROJECT("subproject"),
  NIH_PROJECT_ID("nih_project_id"),
  NIH_REPORTER_ABSTRACT("nih_reporter_abstract"),
  NIH_REPORTER_NARRATIVE("nih_reporter_narrative"),
  DBGAP_STUDY_ID("dbGaP_study_ID"),
  DESCRIPTION_OF_PROJECT("description_of_project"),
  KEYWORDS("keywords"),
  PUBLICATION_URL("publication_url"),
  STUDY_INCLUDE_PROSPECTIVE_OR_RETROSPECTIVE_HUMAN_SAMPLES("study_include_prospective_or_retrospective_human_samples"),
  COLLECTED_NIH_MINIMUM_CEDS_FOR_ALL_DATASETS("collected_NIH_Minimum_CDEs_for_all_datasets"),
  SPECIMEN_TYPE_USED("specimen_type_used"),
  METHOD_OF_DATA_ANALYSIS("method_of_data_analysis - software_statistical_approach"),
  DATA_FILE_TITLE("data_file_title"),
  DATA_FILE_NAME("data_file_name"),
  DATA_DICTIONARY_FILE_NAME("data_dictionary_file_name"),
  DATA_FILE_CREATION_DATETIME("data_file_creation_dateTime"),
  DATA_FILE_SHA256_DIGEST("data_file_sha256_digest");

  private final String value;
  RadxRadFieldsConstant(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
