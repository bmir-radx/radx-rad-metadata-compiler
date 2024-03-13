package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.ElementInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class IdGenerator {
  private static final String TEMPLATE_IRI_PREFIX = "https://repo.metadatacenter.org/template-instances/";
  private static final String ELEMENT_IRI_PREFIX = "https://repo.metadatacenter.org/template-element-instances/";

  public static void generateTemplateId(TemplateInstanceArtifact.Builder templateInstanceArtifactBuilder) throws URISyntaxException {
    templateInstanceArtifactBuilder.withJsonLdId(new URI(
        TEMPLATE_IRI_PREFIX + UUID.randomUUID()
    ));
  }

  public static void generateElementId(ElementInstanceArtifact.Builder elementInstanceArtifactBuilder) throws URISyntaxException {
    elementInstanceArtifactBuilder.withJsonLdId(new URI(
        ELEMENT_IRI_PREFIX + UUID.randomUUID()
    ));
  }
}
