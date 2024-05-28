package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TextAreaFieldInstance;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.Optional;

public class TextAreaFieldGenerator implements FieldGenerator<TextAreaFieldInstance>{
  @Override
  public TextAreaFieldInstance buildFieldInstance(String value, Optional<ValueConstraints> valueConstraints) {
    var fieldInstanceArtifactBuilder = TextAreaFieldInstance.builder();
    if(value != null && !value.equals("")){
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
