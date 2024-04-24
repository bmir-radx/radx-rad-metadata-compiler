package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.CheckboxFieldInstance;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.Optional;

public class CheckBoxFieldGenerator implements FieldGenerator{
  @Override
  public FieldInstanceArtifact buildWithValue(String value, Optional<ValueConstraints> valueConstraints) {
    var fieldInstanceArtifactBuilder = CheckboxFieldInstance.builder();
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
    return CheckboxFieldInstance.builder().build();
  }
}
