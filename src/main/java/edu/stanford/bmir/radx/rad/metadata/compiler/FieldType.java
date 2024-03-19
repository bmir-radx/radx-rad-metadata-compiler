package edu.stanford.bmir.radx.rad.metadata.compiler;

import com.fasterxml.jackson.annotation.JsonValue;
import org.metadatacenter.artifacts.model.core.FieldSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.FieldInputType;

import java.util.HashMap;
import java.util.Map;

import static org.metadatacenter.model.ModelNodeNames.*;

public enum FieldType {
  TEXTFIELD("textfield"),
  TEXTAREA("textarea"),
  PHONE_NUMBER("phone-number"),
  EMAIL("email"),
  RADIO("radio"),
  CHECKBOX("checkbox"),
  LIST("list"),
  LINK("link"),
  NUMERIC("numeric"),
  TEMPORAL("temporal"),
  ATTRIBUTE_VALUE("attribute-value"),
  SECTION_BREAK("section-break"),
  RICHTEXT("richtext"),
  IMAGE("image"),
  YOUTUBE("youtube"),
  CONTROLLED_TERM("controlled-term");

  private final String text;
  private static final Map<FieldInputType, FieldType> typeMapping = new HashMap<>();

  FieldType(String text) {
    this.text = text;
  }

  public String getText() {
    return this.text;
  }

  static {
    typeMapping.put(FieldInputType.TEXTFIELD, TEXTFIELD);
    typeMapping.put(FieldInputType.TEXTAREA, TEXTAREA);
    typeMapping.put(FieldInputType.PHONE_NUMBER, PHONE_NUMBER);
    typeMapping.put(FieldInputType.EMAIL, EMAIL);
    typeMapping.put(FieldInputType.RADIO, RADIO);
    typeMapping.put(FieldInputType.CHECKBOX, CHECKBOX);
    typeMapping.put(FieldInputType.LIST, LIST);
    typeMapping.put(FieldInputType.LINK, LINK);
    typeMapping.put(FieldInputType.NUMERIC, NUMERIC);
    typeMapping.put(FieldInputType.TEMPORAL, TEMPORAL);
    typeMapping.put(FieldInputType.ATTRIBUTE_VALUE, ATTRIBUTE_VALUE);
    typeMapping.put(FieldInputType.SECTION_BREAK, SECTION_BREAK);
    typeMapping.put(FieldInputType.RICHTEXT, RICHTEXT);
    typeMapping.put(FieldInputType.IMAGE, IMAGE);
    typeMapping.put(FieldInputType.YOUTUBE, YOUTUBE);
  }

//  public static FieldType getFieldType(FieldInputType inputType) {
//    return typeMapping.get(inputType);
//  }

  public static FieldType getFieldType(FieldSchemaArtifact fieldSchemaArtifact){
    var expectedFieldValueConstraint = fieldSchemaArtifact.valueConstraints();
    var expectedFieldType = typeMapping.get(fieldSchemaArtifact.fieldUi().inputType());
    if (expectedFieldValueConstraint.isPresent() && expectedFieldValueConstraint.get().isControlledTermValueConstraint()) {
      expectedFieldType = FieldType.CONTROLLED_TERM;
    }
    return expectedFieldType;
  }
}
