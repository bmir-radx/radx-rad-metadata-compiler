package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import edu.stanford.bmir.radx.rad.metadata.compiler.MapInitializer;
import org.apache.commons.lang3.tuple.Pair;
import org.metadatacenter.artifacts.model.core.ControlledTermFieldInstance;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class ControlledTermGenerator implements FieldGenerator{
  @Override
  public FieldInstanceArtifact buildFieldInstance(String value, Optional<ValueConstraints> valueConstraints) {
    var fieldInstanceArtifactBuilder = ControlledTermFieldInstance.builder();
    if(value != null){
      // Precision handling for co-PI, contact-PI, and data-PI
      String piRegex = "(?i)co[- ]?PI";
      String contactPiRegex = "(?i)contact[- ]?PI";
      String dataPiRegex = "(?i)data[- ]?PI";
      value = value.replaceAll(piRegex, "PI");
      value = value.replaceAll(contactPiRegex, "contact-PI");
      value = value.replaceAll(dataPiRegex, "data-PI");

      try{
        var URI = MapInitializer.createControlledTermsMap().get(value);
        fieldInstanceArtifactBuilder
            .withLabel(value)
            .withValue(new URI(URI));
      } catch (URISyntaxException e){
        throw new RuntimeException(e);
      }

    } else{
      if(valueConstraints.isPresent()){
        var defaultValue = valueConstraints.get().defaultValue();
        if(defaultValue.isPresent()){
          Pair<URI, String> defaultValuePair = defaultValue.get().asControlledTermDefaultValue().value();
          if(!defaultValuePair.getRight().equals("")){
            fieldInstanceArtifactBuilder
                .withValue(defaultValuePair.getLeft())
                .withLabel(defaultValuePair.getRight());
          }
        }
      }
    }

    return fieldInstanceArtifactBuilder.build();
  }
}
