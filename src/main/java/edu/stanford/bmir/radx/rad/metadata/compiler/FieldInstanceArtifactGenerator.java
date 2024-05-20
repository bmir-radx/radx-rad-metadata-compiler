package edu.stanford.bmir.radx.rad.metadata.compiler;

import edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators.*;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;

import java.util.*;

public class FieldInstanceArtifactGenerator {
  final Map<FieldType, FieldGenerator> fieldGenerators;

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
    return generator.buildFieldInstance(value, valueConstraints);
  }

  public FieldInstanceArtifact buildEmptyFieldInstance(FieldType fieldType, Optional<ValueConstraints> valueConstraints) {
    FieldGenerator generator = fieldGenerators.get(fieldType);
    return generator.buildFieldInstance(null, valueConstraints);
  }

  public Map<String, FieldInstanceArtifact> buildAttributeValueField(Map<String, String> spreadsheetData, List<String> spreadsheetFields) {
    Map<String, FieldInstanceArtifact> attributeValueFieldInstances = new HashMap<>();
    if (spreadsheetFields != null) {
      for (var spreadsheetField : spreadsheetFields) {
        var spreadsheetValue = spreadsheetData.get(spreadsheetField);
        if (spreadsheetValue != null && !spreadsheetValue.equals("")) {
          attributeValueFieldInstances.put(spreadsheetField,
              fieldGenerators.get(FieldType.TEXTFIELD).buildFieldInstance(spreadsheetValue));
        }
      }
    }
    return attributeValueFieldInstances;
  }

  public List<FieldInstanceArtifact> buildMultiFieldInstances(FieldType fieldType, List<String> valueSet, Optional<ValueConstraints> valueConstraints){
    var fieldInstanceArtifactList = new ArrayList<FieldInstanceArtifact>();
    for (String currentValue : valueSet) {
      var fieldInstanceArtifact = buildFieldInstanceWithValues(fieldType, currentValue, valueConstraints);
      fieldInstanceArtifactList.add(fieldInstanceArtifact);
    }
    return fieldInstanceArtifactList;
  }

  public List<FieldInstanceArtifact> buildMultiFieldInstances(FieldType fieldType, Map<Integer, List<String>> valueSet, Optional<ValueConstraints> valueConstraints){
    var fieldInstanceArtifactList = new ArrayList<FieldInstanceArtifact>();
    for (var entry : valueSet.entrySet()) {
      var fieldInstanceArtifact = buildFieldInstanceWithValues(fieldType, entry.getValue().get(0), valueConstraints);
      fieldInstanceArtifactList.add(fieldInstanceArtifact);
    }
    return fieldInstanceArtifactList;
  }
}
