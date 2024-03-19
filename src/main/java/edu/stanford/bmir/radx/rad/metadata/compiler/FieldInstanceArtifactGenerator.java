package edu.stanford.bmir.radx.rad.metadata.compiler;

import edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators.*;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.FieldSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FieldInstanceArtifactGenerator {
  private final Map<FieldType, FieldGenerator> fieldGenerators;

  public FieldInstanceArtifactGenerator() {
    fieldGenerators = Map.ofEntries(
        Map.entry(FieldType.LINK, new LinkFieldGenerator()),
        Map.entry(FieldType.TEXTFIELD, new TextFieldGenerator()),
        Map.entry(FieldType.TEXTAREA, new TextAreaFieldGenerator()),
        Map.entry(FieldType.NUMERIC, new NumericFieldGenerator()),
        Map.entry(FieldType.TEMPORAL, new TemporalFieldGenerator()),
        Map.entry(FieldType.RADIO, new RadioFieldGenerator()),
        Map.entry(FieldType.PHONE_NUMBER, new PhoneNumberFieldGenerator()),
        Map.entry(FieldType.EMAIL, new EmailGenerator()),
        Map.entry(FieldType.CHECKBOX, new CheckBoxFieldGenerator()),
        Map.entry(FieldType.LIST, new ListGenerator()),
        Map.entry(FieldType.CONTROLLED_TERM, new ControlledTermGenerator())
    );
  }

  public FieldInstanceArtifact buildFieldInstanceWithValues(FieldType fieldType, String value, Optional<ValueConstraints> valueConstraints) {
    FieldGenerator generator = fieldGenerators.get(fieldType);
    return generator.buildWithValue(value, valueConstraints);
  }

  public FieldInstanceArtifact buildEmptyFieldInstance(FieldType fieldType) {
    FieldGenerator generator = fieldGenerators.get(fieldType);
    return generator.buildEmptyFieldInstanceArtifact();
  }

  public Map<String, FieldInstanceArtifact> buildAttributeValueField(Map<String, String> spreadsheetData, List<String> spreadsheetFields) {
    Map<String, FieldInstanceArtifact> attributeValueFieldInstances = new HashMap<>();
    if (spreadsheetFields != null) {
      for (var spreadsheetField : spreadsheetFields) {
        var spreadsheetValue = spreadsheetData.get(spreadsheetField);
        if (spreadsheetValue != null && !spreadsheetValue.equals("")) {
          attributeValueFieldInstances.put(spreadsheetField,
              fieldGenerators.get(FieldType.TEXTFIELD).buildWithValue(spreadsheetValue));
        }
      }
    }
    return attributeValueFieldInstances;
  }
}
