package edu.stanford.bmir.radx.rad.metadata.compiler.form;

public class NIHReporter {
  private final String nihReporterAbstract;
  private final String nihReporterNarrative;

  private NIHReporter(Builder builder) {
    this.nihReporterAbstract = builder.nihReporterAbstract;
    this.nihReporterNarrative = builder.nihReporterNarrative;
  }

  public String getNihReporterAbstract() {
    return nihReporterAbstract;
  }

  public String getNihReporterNarrative() {
    return nihReporterNarrative;
  }

  public static class Builder {
    private String nihReporterAbstract;
    private String nihReporterNarrative;

    public Builder setNihReporterAbstract(String nihReporterAbstract) {
      this.nihReporterAbstract = nihReporterAbstract;
      return this;
    }

    public Builder setNihReporterNarrative(String nihReporterNarrative) {
      this.nihReporterNarrative = nihReporterNarrative;
      return this;
    }

    public NIHReporter build() {
      return new NIHReporter(this);
    }
  }
}
