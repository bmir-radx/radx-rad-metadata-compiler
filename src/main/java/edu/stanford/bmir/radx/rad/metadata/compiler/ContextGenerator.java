package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;

import java.net.URI;
import java.util.Map;

public class ContextGenerator {
  public static void generateTemplateInstanceContext(TemplateSchemaArtifact templateSchemaArtifact,
                                              TemplateInstanceArtifact.Builder templateInstanceArtifactBuilder) {
    var context = templateSchemaArtifact.getChildPropertyUris();
    for(Map.Entry<String, URI> entry : context.entrySet()){
      templateInstanceArtifactBuilder.withJsonLdContextEntry(entry.getKey(), entry.getValue());
    }
  }

  public static void generateElementInstanceContext(ElementSchemaArtifact elementSchemaArtifact,
                                             ElementInstanceArtifact.Builder elementInstanceArtifactBuilder){
    var context = elementSchemaArtifact.getChildPropertyUris();
    for(Map.Entry<String, URI> entry : context.entrySet()){
      elementInstanceArtifactBuilder.withJsonLdContextEntry(entry.getKey(), entry.getValue());
    }
  }
}
