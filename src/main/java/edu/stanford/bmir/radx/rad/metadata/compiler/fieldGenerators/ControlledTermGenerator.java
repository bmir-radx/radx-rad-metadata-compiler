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

public class ControlledTermGenerator implements FieldGenerator<ControlledTermFieldInstance>{
  @Override
  public ControlledTermFieldInstance buildFieldInstance(String value, Optional<ValueConstraints> valueConstraints) {
    var fieldInstanceArtifactBuilder = ControlledTermFieldInstance.builder();
    if(value != null && !value.equals("")){
      // Precision handling for co-PI, contact-PI, and data-PI
      String piRegex = "(?i)co[- ]?PI";
      String contactPiRegex = "(?i)contact[- ]?PI";
      String dataPiRegex = "(?i)data[- ]?PI";
      value = value.replaceAll(piRegex, "PI");
      value = value.replaceAll(contactPiRegex, "contact-PI");
      value = value.replaceAll(dataPiRegex, "data-PI");

      String label;
      URI iri;
      if(getIri(value).isPresent()){
        label = getLabel(value);
        iri = URI.create(getIri(value).get());
      } else{
        label = value;
        iri = URI.create(MapInitializer.createControlledTermsMap().get(value));
      }
      fieldInstanceArtifactBuilder
          .withLabel(label)
          .withValue(iri);

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

  private String getLabel(String value) {
    var trimmedValue = value.trim();
    var openParIndex = trimmedValue.lastIndexOf('(');
    if(openParIndex == -1) {
      return trimmedValue;
    }
    var substring = trimmedValue.substring(0, openParIndex);
    if(substring.startsWith("[") && substring.endsWith("]")) {
      return substring.substring(1, substring.length() - 1).trim();
    }
    else {
      return substring.trim();
    }
  }

  private Optional<String> getIri(String value) {
    var trimmedValue = value.trim();
    if(!trimmedValue.endsWith(")")) {
      return Optional.empty();
    }
    var openParIndex = trimmedValue.lastIndexOf('(');
    if(openParIndex == -1) {
      return Optional.empty();
    }
    return Optional.of((trimmedValue.substring(openParIndex + 1, trimmedValue.length() - 1)));
  }
}
