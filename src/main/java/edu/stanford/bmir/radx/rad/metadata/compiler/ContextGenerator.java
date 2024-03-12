package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.bmir.radx.metadata.validator.lib.JsonLoader;
import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ContextGenerator {
  public void generateTemplateInstanceContext(TemplateSchemaArtifact templateSchemaArtifact,
                                              TemplateInstanceArtifact.Builder templateInstanceArtifactBuilder) {
    var context = templateSchemaArtifact.getChildPropertyUris();
    for(Map.Entry<String, URI> entry : context.entrySet()){
      templateInstanceArtifactBuilder.withJsonLdContextEntry(entry.getKey(), entry.getValue());
    }
  }

  public void generateElementInstanceContext(ElementSchemaArtifact elementSchemaArtifact,
                                             ElementInstanceArtifact.Builder elementInstanceArtifactBuilder){
    var context = elementSchemaArtifact.getChildPropertyUris();
    for(Map.Entry<String, URI> entry : context.entrySet()){
      elementInstanceArtifactBuilder.withJsonLdContextEntry(entry.getKey(), entry.getValue());
    }
  }
}
