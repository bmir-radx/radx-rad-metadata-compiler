package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;

public class AttributeValueFieldUtil {
  public static boolean isAttributeValue(TemplateSchemaArtifact templateSchemaArtifact, String specificationPath){
    var templateReporter = new TemplateReporter(templateSchemaArtifact);
    var attributeValueInstanceArtifact = templateReporter.getFieldSchema(specificationPath);
    return attributeValueInstanceArtifact.map(fieldSchemaArtifact -> fieldSchemaArtifact.fieldUi().isAttributeValue()).orElse(false);
  }
}
