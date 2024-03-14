package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class ControlledTermGenerator implements FieldGenerator{
  @Override
  public FieldInstanceArtifact buildWithValue(String value, Optional<ValueConstraints> valueConstraints) {
    var fieldInstanceArtifactBuilder = FieldInstanceArtifact.controlledTermFieldInstanceBuilder();
    FieldInstanceArtifact fieldInstanceArtifact;
    if(value != null){
      try{
        fieldInstanceArtifact = fieldInstanceArtifactBuilder
            .withValue(new URI(value))
            .build();
      } catch (URISyntaxException e){
        throw new RuntimeException(e);
      }

    } else{
      fieldInstanceArtifact = fieldInstanceArtifactBuilder.build();
    }

    return fieldInstanceArtifact;
  }

  @Override
  public FieldInstanceArtifact buildEmptyFieldInstanceArtifact() {
    return FieldInstanceArtifact.controlledTermFieldInstanceBuilder().build();
  }
}
