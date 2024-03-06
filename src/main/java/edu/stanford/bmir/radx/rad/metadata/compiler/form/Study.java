package edu.stanford.bmir.radx.rad.metadata.compiler.form;

public class Study {
  private final String projectTitle;
  private final String nihProjectId;
  private final String subproject;
  private final String dbGapStudyId;
  private final String descriptionOfProject;
  private final String keywords;
  private final String publicationUrl;
  private final String studyIncludeProspectiveOrRetrospectiveHumanSamples;
  private final String collectedNIHMinimumCDEsForAllDatasets;
  private final String specimenTypeUsed;
  private final String methodOfDataAnalysis;

  private Study(Builder builder) {
    this.projectTitle = builder.projectTitle;
    this.nihProjectId = builder.nihProjectId;
    this.subproject = builder.subproject;
    this.dbGapStudyId = builder.dbGapStudyId;
    this.descriptionOfProject = builder.descriptionOfProject;
    this.keywords = builder.keywords;
    this.publicationUrl = builder.publicationUrl;
    this.studyIncludeProspectiveOrRetrospectiveHumanSamples = builder.studyIncludeProspectiveOrRetrospectiveHumanSamples;
    this.collectedNIHMinimumCDEsForAllDatasets = builder.collectedNIHMinimumCDEsForAllDatasets;
    this.specimenTypeUsed = builder.specimenTypeUsed;
    this.methodOfDataAnalysis = builder.methodOfDataAnalysis;
  }

  public String getProjectTitle() { return projectTitle; }
  public String getNihProjectId() { return nihProjectId; }
  public String getSubproject() { return subproject; }
  public String getDbGapStudyId() { return dbGapStudyId; }
  public String getDescriptionOfProject() { return descriptionOfProject; }
  public String getKeywords() { return keywords; }
  public String getPublicationUrl() { return publicationUrl; }
  public String getStudyIncludeProspectiveOrRetrospectiveHumanSamples() { return studyIncludeProspectiveOrRetrospectiveHumanSamples; }
  public String getCollectedNIHMinimumCDEsForAllDatasets() { return collectedNIHMinimumCDEsForAllDatasets; }
  public String getSpecimenTypeUsed() { return specimenTypeUsed; }
  public String getMethodOfDataAnalysis() { return methodOfDataAnalysis; }

  public static class Builder {
    private String projectTitle;
    private String nihProjectId;
    private String subproject;
    private String dbGapStudyId;
    private String descriptionOfProject;
    private String keywords;
    private String publicationUrl;
    private String studyIncludeProspectiveOrRetrospectiveHumanSamples;
    private String collectedNIHMinimumCDEsForAllDatasets;
    private String specimenTypeUsed;
    private String methodOfDataAnalysis;

    public Builder setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; return this; }
    public Builder setNihProjectId(String nihProjectId) { this.nihProjectId = nihProjectId; return this; }
    public Builder setSubproject(String subproject) { this.subproject = subproject; return this; }
    public Builder setDbGapStudyId(String dbGapStudyId) { this.dbGapStudyId = dbGapStudyId; return this; }
    public Builder setDescriptionOfProject(String descriptionOfProject) { this.descriptionOfProject = descriptionOfProject; return this; }
    public Builder setKeywords(String keywords) { this.keywords = keywords; return this; }
    public Builder setPublicationUrl(String publicationUrl) { this.publicationUrl = publicationUrl; return this; }
    public Builder setStudyIncludeProspectiveOrRetrospectiveHumanSamples(String studyIncludeProspectiveOrRetrospectiveHumanSamples) { this.studyIncludeProspectiveOrRetrospectiveHumanSamples = studyIncludeProspectiveOrRetrospectiveHumanSamples; return this; }
    public Builder setCollectedNIHMinimumCDEsForAllDatasets(String collectedNIHMinimumCDEsForAllDatasets) { this.collectedNIHMinimumCDEsForAllDatasets = collectedNIHMinimumCDEsForAllDatasets; return this; }
    public Builder setSpecimenTypeUsed(String specimenTypeUsed) { this.specimenTypeUsed = specimenTypeUsed; return this; }
    public Builder setMethodOfDataAnalysis(String methodOfDataAnalysis) { this.methodOfDataAnalysis = methodOfDataAnalysis; return this; }

    public Study build() {
      return new Study(this);
    }
  }
}
