package edu.stanford.bmir.radx.rad.metadata.compiler.form;

public class Name {
  private final String fullName;
  private final String firstName;
  private final String middleName;
  private final String lastName;

  private Name(Builder builder) {
    this.fullName = builder.fullName;
    this.firstName = builder.firstName;
    this.middleName = builder.middleName;
    this.lastName = builder.lastName;
  }

  public String getFullName() {
    return fullName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public static class Builder {
    private String fullName;
    private String firstName;
    private String middleName;
    private String lastName;

    public Builder setFullName(String fullName) {
      this.fullName = fullName;
      return this;
    }

    public Builder setFirstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder setMiddleName(String middleName) {
      this.middleName = middleName;
      return this;
    }

    public Builder setLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Name build() {
      return new Name(this);
    }
  }
}
