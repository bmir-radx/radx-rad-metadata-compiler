package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.Optional;

public interface FieldGenerator {
  default FieldInstanceArtifact buildFieldInstance(String value) {
    return buildFieldInstance(value, Optional.empty());
  }
  FieldInstanceArtifact buildFieldInstance(String value, Optional<ValueConstraints> valueConstraints);
}
