package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.NumericFieldInstance;
import org.metadatacenter.artifacts.model.core.fields.XsdNumericDatatype;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.Optional;

public class NumericFieldGenerator implements FieldGenerator<NumericFieldInstance>{

  @Override
  public NumericFieldInstance buildFieldInstance(String value, Optional<ValueConstraints> valueConstraints) {
    var fieldInstanceArtifactBuilder = NumericFieldInstance.builder();
    XsdNumericDatatype numberType;

    if(valueConstraints.isPresent()){
      numberType = valueConstraints.get().asNumericValueConstraints().numberType();
    } else{
      numberType = XsdNumericDatatype.DOUBLE;
    }

    if(value != null){
      fieldInstanceArtifactBuilder
          //TODO: need to convert to different Number based on number type
          .withValue(Double.valueOf(value));
    } else{
      if(valueConstraints.isPresent()){
        var defaultValue = valueConstraints.get().defaultValue();
        defaultValue.ifPresent(defaultValue1 -> fieldInstanceArtifactBuilder
            .withValue(defaultValue1.asNumericDefaultValue().value()));
      }
    }

    return fieldInstanceArtifactBuilder.withType(numberType).build();
  }
}
