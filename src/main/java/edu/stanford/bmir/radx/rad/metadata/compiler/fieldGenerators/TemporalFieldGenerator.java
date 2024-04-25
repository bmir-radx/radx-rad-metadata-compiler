package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemporalFieldInstance;
import org.metadatacenter.artifacts.model.core.fields.XsdTemporalDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.Optional;

public class TemporalFieldGenerator implements FieldGenerator{
  @Override
  public FieldInstanceArtifact buildFieldInstance(String value, Optional<ValueConstraints> valueConstraints) {
    XsdTemporalDatatype temporalType;
    if(valueConstraints.isPresent()){
      temporalType = valueConstraints.get().asTemporalValueConstraints().temporalType();
    } else{
      temporalType = XsdTemporalDatatype.DATETIME;
    }

    var fieldInstanceArtifactBuilder = TemporalFieldInstance.builder();
    if(value != null){
      fieldInstanceArtifactBuilder
          .withValue(value)
          .withType(temporalType);
    } else{
      if(valueConstraints.isPresent()){
        var defaultValue = valueConstraints.get().defaultValue();
        defaultValue.ifPresent(defaultValue1 -> {
          var v = defaultValue1.asTemporalDefaultValue().value();
          if (!v.equals("")) {
            fieldInstanceArtifactBuilder.withValue(v).withType(temporalType);
          }
        });
      }
    }

    return fieldInstanceArtifactBuilder.build();
  }
}
