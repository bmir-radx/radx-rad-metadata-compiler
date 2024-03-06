package edu.stanford.bmir.radx.rad.metadata.compiler;

public enum RadxSpecificationMetadataConstant {
  DATA_FILE_CONTRIBUTORS("Data File Contributors"),
  CONTRIBUTOR_TYPE("Contributor Type"),
  CONTRIBUTOR_NAME("Contributor Name"),
  CONTRIBUTOR_GIVEN_NAME("Contributor Given Name"),
  CONTRIBUTOR_FAMILY_NAME("Contributor Family Name"),
  CONTRIBUTOR_IDENTIFIER("Contributor Identifier"),
  CONTRIBUTOR_IDENTIFIER_SCHEME("Contributor Identifier Scheme"),
  CONTRIBUTOR_AFFILIATION("Contributor Affiliation"),
  CONTRIBUTOR_AFFILIATION_IDENTIFIER("Contributor Affiliation Identifier"),
  CONTRIBUTOR_AFFILIATION_IDENTIFIER_SCHEME("Contributor Affiliation Identifier Scheme"),
  CONTRIBUTOR_EMAIL("Contributor Email"),
  CONTRIBUTOR_ROLE("Contributor Role"),
  DATA_FILE_CREATORS("Data File Creators"),
  CREATOR_TYPE("Creator Type"),
  CREATOR_NAME("Creator Name"),
  CREATOR_GIVEN_NAME("Creator Given Name"),
  CREATOR_FAMILY_NAME("Creator Family Name"),
  CREATOR_IDENTIFIER("Creator Identifier"),
  CREATOR_IDENTIFIER_SCHEME("Creator Identifier Scheme"),
  CREATOR_AFFILIATION("Creator Affiliation"),
  CREATOR_AFFILIATION_IDENTIFIER("Creator Affiliation Identifier"),
  CREATOR_AFFILIATION_IDENTIFIER_SCHEME("Creator Affiliation Identifier Scheme"),
  CREATOR_EMAIL("Creator Email"),
  CREATOR_ROLE("Creator Role"),
  DATA_FILE_PARENT_STUDIES("Data File Parent Studies"),
  PHS_IDENTIFIER("PHS Identifier"),
  STUDY_IDENTIFIER("Study Identifier"),
  STUDY_IDENTIFIER_SCHEME("Study Identifier Scheme"),
  STUDY_NAME("Study Name"),
  STUDY_START_DATE("Study Start Date"),
  STUDY_END_DATE("Study End Date"),
  AUXILIARY_METADATA("Auxiliary Metadata"),
  DATA_FILE_DESCRIPTIVE_KEY_VALUE_PAIRS("Data File Descriptive Key-Value Pairs"),
  ADDITIONAL_COMMENTARY("Additional Commentary"),
  DATA_FILE_SUBJECTS("Data File Subjects"),
  SUBJECT_IDENTIFIER("Subject Identifier"),
  KEYWORD("Keyword"),
  DATA_FILE_RELATED_RESOURCES("Data File Related Resources"),
  RELATED_RESOURCE_IDENTIFER("Related Resource Identifier"),
  RELATED_RESOURCE_IDENTIFER_TYPE("Related Resource Identifier Type"),
  RELATED_RESOURCE_FILE_NAME("Related Resource File Name"),
  RELATED_RESOURCE_TYPE_CATEGORY("Related Resource Type Category\n"),
  RELATED_RESOURCE_RELATION("Related Resource Relation"),
  DATA_FILE_TITLES("Data File Titles"),
  TITLE("Title"),
  LANGUAGE("Language"),
  DATA_FILE_DATA_DICTIONARY("Data File Data Dictionary"),
  DATA_DICTIONARY_FILE_NAME("Data Dictionary File Name"),
  DATA_FILE_DATES("Data File Dates"),
  EVENT_TYPE("Event Type"),
  DATE("Date"),
  DATA_FILE_IDENTITY("Data File Identity"),
  IDENTIFIER("Identifier"),
  IDENTIFIER_TYPE("Identifier Type"),
  FILE_NAME("File Name"),
  VERSION("Version"),
  SHA256_DIGEST("SHA256 digest");

  private String field;

  RadxSpecificationMetadataConstant(String field) {
    this.field = field;
  }

  public String getField() {
    return field;
  }
}
