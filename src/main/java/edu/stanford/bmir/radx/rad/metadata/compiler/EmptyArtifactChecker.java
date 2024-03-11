package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;

import java.util.List;
import java.util.Map;

public class EmptyArtifactChecker {
  public ElementInstanceArtifact getOrEmptyElementInstanceArtifact(ElementInstanceArtifact artifact) {
    // If the artifact is empty based on the checks, return a new, empty ElementInstanceArtifact
    if (isEmpty(artifact)) {
      return ElementInstanceArtifact.builder().build();
    } else {
      // Otherwise, return the original artifact
      return artifact;
    }
  }

  private boolean isEmpty(ElementInstanceArtifact elementInstanceArtifact) {
    // Check single field instances
    for (FieldInstanceArtifact fieldArtifact : elementInstanceArtifact.singleInstanceFieldInstances().values()) {
      if (!isEmptyFieldInstanceArtifact(fieldArtifact)) {
        return false;
      }
    }

    // Check multi field instances
    for (List<FieldInstanceArtifact> fieldArtifacts : elementInstanceArtifact.multiInstanceFieldInstances().values()) {
      for (FieldInstanceArtifact fieldArtifact : fieldArtifacts) {
        if (!isEmptyFieldInstanceArtifact(fieldArtifact)) {
          return false;
        }
      }
    }

    // Check single element instances
    for (ElementInstanceArtifact elementArtifact : elementInstanceArtifact.singleInstanceElementInstances().values()) {
      if (!isEmptyElementInstanceArtifact(elementArtifact)) {
        return false;
      }
    }

    // Check multi instance element instances
    for (List<ElementInstanceArtifact> elementArtifacts : elementInstanceArtifact.multiInstanceElementInstances().values()) {
      for (ElementInstanceArtifact elementArtifact : elementArtifacts) {
        if (!isEmptyElementInstanceArtifact(elementArtifact)) {
          return false;
        }
      }
    }

    //Check attribute value instances
    for (Map<String, FieldInstanceArtifact> fieldArtifacts : elementInstanceArtifact.attributeValueFieldInstances().values()) {
      for (FieldInstanceArtifact fieldArtifact : fieldArtifacts.values()) {
        if (!isEmptyFieldInstanceArtifact(fieldArtifact)) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean isEmptyFieldInstanceArtifact(FieldInstanceArtifact fieldArtifact) {
    return fieldArtifact.jsonLdValue().isEmpty() && fieldArtifact.jsonLdId().isEmpty();
  }

  private boolean isEmptyElementInstanceArtifact(ElementInstanceArtifact elementArtifact) {
    return isEmpty(elementArtifact);
  }
}
