package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.Optional;

public interface FieldGenerator<T extends FieldInstanceArtifact> {
  default T buildFieldInstance(String value) {
    return buildFieldInstance(value, Optional.empty());
  }
  T buildFieldInstance(String value, Optional<ValueConstraints> valueConstraints);
}
