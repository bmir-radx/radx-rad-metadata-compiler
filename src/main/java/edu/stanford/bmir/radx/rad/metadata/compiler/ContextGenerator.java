package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.bmir.radx.metadata.validator.lib.JsonLoader;
import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ContextGenerator {
  public Map<String, URI> generateTemplateInstanceContext(TemplateSchemaArtifact templateSchemaArtifact) {
    Map<String, URI> context = new HashMap<>();

    context.putAll(templateSchemaArtifact.jsonLdContext());
    context.putAll(templateSchemaArtifact.getChildPropertyUris());

    return context;
  }

  public Map<String, URI> generateElementInstanceContext(ElementSchemaArtifact elementSchemaArtifact){
    Map<String, URI> context = new HashMap<>();
    context.putAll(elementSchemaArtifact.getChildPropertyUris());
    return context;
  }
}
