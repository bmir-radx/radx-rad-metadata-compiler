package edu.stanford.bmir.radx.rad.metadata.compiler.form;

public class DataFile {
  private final String dataFileTitle;
  private final String dataFileName;
  private final String dataFileCreationDateTime;
  private final String dataFileSha256Digest;

  private DataFile(Builder builder) {
    this.dataFileTitle = builder.dataFileTitle;
    this.dataFileName = builder.dataFileName;
    this.dataFileCreationDateTime = builder.dataFileCreationDateTime;
    this.dataFileSha256Digest = builder.dataFileSha256Digest;
  }

  public String getDataFileTitle() {
    return dataFileTitle;
  }

  public String getDataFileName() {
    return dataFileName;
  }

  public String getDataFileCreationDateTime() {
    return dataFileCreationDateTime;
  }

  public String getDataFileSha256Digest() {
    return dataFileSha256Digest;
  }

  public static class Builder {
    private String dataFileTitle;
    private String dataFileName;
    private String dataFileCreationDateTime;
    private String dataFileSha256Digest;

    public Builder setDataFileTitle(String dataFileTitle) {
      this.dataFileTitle = dataFileTitle;
      return this;
    }

    public Builder setDataFileName(String dataFileName) {
      this.dataFileName = dataFileName;
      return this;
    }

    public Builder setDataFileCreationDateTime(String dataFileCreationDateTime) {
      this.dataFileCreationDateTime = dataFileCreationDateTime;
      return this;
    }

    public Builder setDataFileSha256Digest(String dataFileSha256Digest) {
      this.dataFileSha256Digest = dataFileSha256Digest;
      return this;
    }

    public DataFile build() {
      return new DataFile(this);
    }
  }
}
