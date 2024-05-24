package edu.stanford.bmir.radx.rad.metadata.compiler;

import edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators.*;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.*;

public class FieldInstanceArtifactGenerator {
  final Map<FieldType, FieldGenerator<? extends FieldInstanceArtifact>> fieldGenerators;

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

  public <T extends FieldInstanceArtifact> T buildFieldInstanceWithValues(FieldType fieldType, String value, Optional<ValueConstraints> valueConstraints) {
    FieldGenerator<T> generator = (FieldGenerator<T>) fieldGenerators.get(fieldType);
    return generator.buildFieldInstance(value, valueConstraints);
  }

  public <T extends FieldInstanceArtifact> T buildEmptyFieldInstance(FieldType fieldType, Optional<ValueConstraints> valueConstraints) {
    FieldGenerator<T> generator = (FieldGenerator<T>) fieldGenerators.get(fieldType);
    return generator.buildFieldInstance(null, valueConstraints);
  }

  @SuppressWarnings("unchecked")
  public <T extends FieldInstanceArtifact> LinkedHashMap<String, T> buildAttributeValueField(Map<String, String> spreadsheetData, List<String> spreadsheetFields) {
    LinkedHashMap<String, T> attributeValueFieldInstances = new LinkedHashMap<>();
    if (spreadsheetFields != null) {
      for (var spreadsheetField : spreadsheetFields) {
        var spreadsheetValue = spreadsheetData.get(spreadsheetField);
        if (spreadsheetValue != null && !spreadsheetValue.equals("")) {
          FieldGenerator<T> generator = (FieldGenerator<T>) fieldGenerators.get(FieldType.TEXTFIELD);
          attributeValueFieldInstances.put(spreadsheetField, generator.buildFieldInstance(spreadsheetValue, Optional.empty()));
        }
      }
    }
    return attributeValueFieldInstances;
  }


  public <T extends FieldInstanceArtifact> List<T> buildMultiFieldInstances(FieldType fieldType, List<String> valueSet, Optional<ValueConstraints> valueConstraints) {
    List<T> fieldInstanceArtifactList = new ArrayList<>();
    for (String currentValue : valueSet) {
      T fieldInstanceArtifact = buildFieldInstanceWithValues(fieldType, currentValue, valueConstraints);
      fieldInstanceArtifactList.add(fieldInstanceArtifact);
    }
    return fieldInstanceArtifactList;
  }


  public <T extends FieldInstanceArtifact> List<T> buildMultiFieldInstances(FieldType fieldType, Map<Integer, List<String>> valueSet, Optional<ValueConstraints> valueConstraints) {
    List<T> fieldInstanceArtifactList = new ArrayList<>();
    for (var entry : valueSet.entrySet()) {
      T fieldInstanceArtifact = buildFieldInstanceWithValues(fieldType, entry.getValue().get(0), valueConstraints);
      fieldInstanceArtifactList.add(fieldInstanceArtifact);
    }
    return fieldInstanceArtifactList;
  }
}
