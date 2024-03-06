package edu.stanford.bmir.radx.rad.metadata.compiler.form;

public class Affiliation {
  private final String affiliationName;
  private final String affiliationIdentifier;
  private final String affiliationIdentifierScheme;

  private Affiliation(Builder builder) {
    this.affiliationName = builder.affiliationName;
    this.affiliationIdentifier = builder.affiliationIdentifier;
    this.affiliationIdentifierScheme = builder.affiliationIdentifierScheme;
  }

  public String getAffiliationName() {
    return affiliationName;
  }

  public String getAffiliationIdentifier() {
    return affiliationIdentifier;
  }

  public String getAffiliationIdentifierScheme() {
    return affiliationIdentifierScheme;
  }

  public static class Builder {
    private String affiliationName;
    private String affiliationIdentifier;
    private String affiliationIdentifierScheme;

    public Builder setAffiliationName(String affiliationName) {
      this.affiliationName = affiliationName;
      return this;
    }

    public Builder setAffiliationIdentifier(String affiliationIdentifier) {
      this.affiliationIdentifier = affiliationIdentifier;
      return this;
    }

    public Builder setAffiliationIdentifierScheme(String affiliationIdentifierScheme) {
      this.affiliationIdentifierScheme = affiliationIdentifierScheme;
      return this;
    }

    public Affiliation build() {
      return new Affiliation(this);
    }
  }
}
