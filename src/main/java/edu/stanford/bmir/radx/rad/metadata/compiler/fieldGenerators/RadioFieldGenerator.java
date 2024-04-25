package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.RadioFieldInstance;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.Optional;

public class RadioFieldGenerator implements FieldGenerator{
  @Override
  public FieldInstanceArtifact buildFieldInstance(String value, Optional<ValueConstraints> valueConstraints) {
    var fieldInstanceArtifactBuilder = RadioFieldInstance.builder();
    if(value != null){
      fieldInstanceArtifactBuilder.withValue(value);
    } else{
      if(valueConstraints.isPresent()){
        var defaultValue = valueConstraints.get().defaultValue();
        defaultValue.ifPresent(defaultValue1 -> {
          var v = defaultValue1.asTextDefaultValue().value();
          if (!v.equals("")) {
            fieldInstanceArtifactBuilder.withValue(v);
          }
        });
      }
    }
    return fieldInstanceArtifactBuilder.build();
  }
}
