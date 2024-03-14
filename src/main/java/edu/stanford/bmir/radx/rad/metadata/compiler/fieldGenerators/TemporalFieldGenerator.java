package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.fields.XsdTemporalDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.Optional;

public class TemporalFieldGenerator implements FieldGenerator{
  @Override
  public FieldInstanceArtifact buildWithValue(String value, Optional<ValueConstraints> valueConstraints) {
    XsdTemporalDatatype temporalType;
    if(valueConstraints.isPresent()){
      temporalType = valueConstraints.get().asTemporalValueConstraints().temporalType();
    } else{
      temporalType = XsdTemporalDatatype.DATETIME;
    }

    var fieldInstanceArtifactBuilder = FieldInstanceArtifact.temporalFieldInstanceBuilder();
    FieldInstanceArtifact fieldInstanceArtifact;
    if(value != null){
      fieldInstanceArtifact = fieldInstanceArtifactBuilder
          .withValue(value)
          .withType(temporalType)
          .build();
    } else{
      fieldInstanceArtifact = fieldInstanceArtifactBuilder.build();
    }

    return fieldInstanceArtifact;
  }

  @Override
  public FieldInstanceArtifact buildEmptyFieldInstanceArtifact() {
    return FieldInstanceArtifact.temporalFieldInstanceBuilder().build();
  }
}
