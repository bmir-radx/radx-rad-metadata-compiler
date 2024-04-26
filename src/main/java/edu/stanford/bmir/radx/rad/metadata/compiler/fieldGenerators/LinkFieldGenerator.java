package edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators;

import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.LinkFieldInstance;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class LinkFieldGenerator implements FieldGenerator{
  @Override
  public FieldInstanceArtifact buildFieldInstance(String value, Optional<ValueConstraints> valueConstraints) {
    var fieldInstanceArtifactBuilder = LinkFieldInstance.builder();
    if(value != null){
      try{
        fieldInstanceArtifactBuilder.withValue(new URI(value));
      } catch (URISyntaxException e){
        throw new RuntimeException(e);
      }
    } else{
      if(valueConstraints.isPresent()){
        var defaultValue = valueConstraints.get().defaultValue();
        defaultValue.ifPresent(defaultValue1 -> fieldInstanceArtifactBuilder.withValue(defaultValue1.asLinkDefaultValue().value()));
        defaultValue.ifPresent(defaultValue1 -> {
          var v = defaultValue1.asLinkDefaultValue().value();
          if (!v.toString().equals("")) {
            fieldInstanceArtifactBuilder.withValue(v);
          }
        });
      }
    }
    return fieldInstanceArtifactBuilder.build();
  }
}
