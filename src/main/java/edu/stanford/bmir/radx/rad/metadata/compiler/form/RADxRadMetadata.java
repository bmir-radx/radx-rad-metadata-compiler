package edu.stanford.bmir.radx.rad.metadata.compiler.form;

import java.util.List;

public class RADxRadMetadata {
  private final List<Person> contactPIs;
  private final List<Person> creators;
  private final NIHReporter nihReporter;
  private final Study study;
  private final DataFile dataFile;
  private final String dataDictionaryFileName;

  private RADxRadMetadata(Builder builder) {
    this.contactPIs = builder.contactPIs;
    this.creators = builder.creators;
    this.nihReporter = builder.nihReporter;
    this.study = builder.study;
    this.dataFile = builder.dataFile;
    this.dataDictionaryFileName = builder.dataDictionaryFileName;
  }

  public List<Person> getContactPIs() {
    return contactPIs;
  }

  public List<Person> getCreators() {
    return creators;
  }

  public NIHReporter getNihReporter() {
    return nihReporter;
  }

  public Study getStudy() {
    return study;
  }

  public DataFile getDataFile() {
    return dataFile;
  }

  public String getDataDictionaryFileName() {
    return dataDictionaryFileName;
  }

  public static class Builder {
    private List<Person> contactPIs;
    private List<Person> creators;
    private NIHReporter nihReporter;
    private Study study;
    private DataFile dataFile;
    private String dataDictionaryFileName;

    public Builder setContactPIs(List<Person> contactPIs) {
      this.contactPIs = contactPIs;
      return this;
    }

    public Builder setCreators(List<Person> creators) {
      this.creators = creators;
      return this;
    }

    public Builder setNihReporter(NIHReporter nihReporter) {
      this.nihReporter = nihReporter;
      return this;
    }

    public Builder setStudy(Study study) {
      this.study = study;
      return this;
    }

    public Builder setDataFile(DataFile dataFile) {
      this.dataFile = dataFile;
      return this;
    }

    public Builder setDataDictionaryFileName(String dataDictionaryFileName) {
      this.dataDictionaryFileName = dataDictionaryFileName;
      return this;
    }

    public RADxRadMetadata build() {
      return new RADxRadMetadata(this);
    }
  }
}
