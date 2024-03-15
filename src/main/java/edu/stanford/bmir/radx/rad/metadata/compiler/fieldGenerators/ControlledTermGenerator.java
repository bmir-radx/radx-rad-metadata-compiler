package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import edu.stanford.bmir.radx.rad.metadata.compiler.MapInitializer;
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
        var URI = MapInitializer.createControlledTermsMap().get(value);
        fieldInstanceArtifact = fieldInstanceArtifactBuilder
            .withLabel(value)
            .withValue(new URI(URI))
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
