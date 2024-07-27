package edu.stanford.bmir.radx.rad.metadata.compiler;

import java.util.EnumSet;
import java.util.Set;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxSpecificationMetadataConstant.*;

public enum VersionSpecial {
  FIRST_VERSION_SPECIAL(EnumSet.of(DATA_FILE_SUBJECTS, DATA_FILE_RELATED_RESOURCES));
  private final Set<RadxSpecificationMetadataConstant> elements;

  VersionSpecial(Set<RadxSpecificationMetadataConstant> elements) {
    this.elements = elements;
  }

  public Set<RadxSpecificationMetadataConstant> getElements() {
    return elements;
  }

  public boolean isFirstVersionSpecialty(String element) {
    for (RadxSpecificationMetadataConstant constant : FIRST_VERSION_SPECIAL.elements) {
      if (constant.getValue().equals(element)) {
        return true;
      }
    }
    return false;
  }
}
