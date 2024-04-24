package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TextFieldInstance;
import org.metadatacenter.artifacts.model.core.ValidationHelper;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.Optional;

public class TextFieldGenerator implements FieldGenerator{
  @Override
  public FieldInstanceArtifact buildWithValue(String value, Optional<ValueConstraints> valueConstraints) {
    var fieldInstanceArtifactBuilder = TextFieldInstance.builder();
    FieldInstanceArtifact fieldInstanceArtifact;
    if(value != null){
      fieldInstanceArtifact = fieldInstanceArtifactBuilder
          .withValue(value)
          .build();
    } else{
      fieldInstanceArtifact = fieldInstanceArtifactBuilder.build();
    }
    return fieldInstanceArtifact;
  }

  @Override
  public FieldInstanceArtifact buildEmptyFieldInstanceArtifact() {
    return TextFieldInstance.builder().build();
  }
}
