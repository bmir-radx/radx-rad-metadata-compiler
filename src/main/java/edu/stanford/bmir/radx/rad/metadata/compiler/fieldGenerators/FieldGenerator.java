package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.net.URISyntaxException;
import java.util.Optional;

public interface FieldGenerator {
  default FieldInstanceArtifact buildWithValue(String value) {
    return buildWithValue(value, Optional.empty());
  }
  FieldInstanceArtifact buildWithValue(String value, Optional<ValueConstraints> valueConstraints);
  FieldInstanceArtifact buildEmptyFieldInstanceArtifact();
}
