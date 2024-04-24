package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.NumericFieldInstance;
import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.Optional;

public class NumericFieldGenerator implements FieldGenerator{

  @Override
  public FieldInstanceArtifact buildWithValue(String value, Optional<ValueConstraints> valueConstraints) {
    XsdNumericDatatype numberType;
    if(valueConstraints.isPresent()){
      numberType = valueConstraints.get().asNumericValueConstraints().numberType();
    } else{
      numberType = XsdNumericDatatype.DOUBLE;
    }

    var fieldInstanceArtifactBuilder = NumericFieldInstance.builder();
    FieldInstanceArtifact fieldInstanceArtifact;
    if(value != null){
      fieldInstanceArtifact = fieldInstanceArtifactBuilder
          //TODO: need to convert to different Number based on number type
          .withValue(Double.valueOf(value))
          .withType(numberType)
          .build();
    } else{
      fieldInstanceArtifact = fieldInstanceArtifactBuilder.build();
    }

    return fieldInstanceArtifact;
  }

  @Override
  public FieldInstanceArtifact buildEmptyFieldInstanceArtifact() {
    return NumericFieldInstance.builder().build();
  }
}
