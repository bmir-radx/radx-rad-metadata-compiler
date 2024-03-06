package edu.stanford.bmir.radx.rad.metadata.compiler.form;

public class Person {
  private final Name name;
  private final Affiliation affiliation;
  private final String profileId;
  private final String orcid;
  private final String role;

  private Person(Builder builder) {
    this.name = builder.name;
    this.affiliation = builder.affiliation;
    this.profileId = builder.profileId;
    this.orcid = builder.orcid;
    this.role = builder.role;
  }

  public Name getName() {
    return name;
  }

  public Affiliation getAffiliation() {
    return affiliation;
  }

  public String getProfileId() {
    return profileId;
  }

  public String getOrcid() {
    return orcid;
  }

  public String getRole() {
    return role;
  }

  public static class Builder {
    private Name name;
    private Affiliation affiliation;
    private String profileId;
    private String orcid;
    private String role;

    public Builder setName(Name name) {
      this.name = name;
      return this;
    }

    public Builder setAffiliation(Affiliation affiliation) {
      this.affiliation = affiliation;
      return this;
    }

    public Builder setProfileId(String profileId) {
      this.profileId = profileId;
      return this;
    }

    public Builder setOrcid(String orcid) {
      this.orcid = orcid;
      return this;
    }

    public Builder setRole(String role) {
      this.role = role;
      return this;
    }

    public Person build() {
      return new Person(this);
    }
  }
}
