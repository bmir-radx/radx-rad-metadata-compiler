package edu.stanford.bmir.radx.rad.metadata.compiler;

import edu.stanford.bmir.radx.rad.metadata.compiler.fieldGenerators.*;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.OrcidField;
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

  public <T extends FieldInstanceArtifact> T buildSingleInstanceWithValue(FieldType fieldType, String value, Optional<ValueConstraints> valueConstraints) {
    FieldGenerator<T> generator = (FieldGenerator<T>) fieldGenerators.get(fieldType);
    return generator.buildFieldInstance(value, valueConstraints);
  }

  public <T extends FieldInstanceArtifact> List<T> buildMultipleInstancesWithValues(FieldType fieldType, List<String> values, Optional<ValueConstraints> valueConstraints) {
    List<T> instances = new ArrayList<>();
    for(var value: values){
      instances.add(buildSingleInstanceWithValue(fieldType, value, valueConstraints));
    }
    return instances;
  }

  public <T extends FieldInstanceArtifact> T buildSingleEmptyInstance(FieldType fieldType, Optional<ValueConstraints> valueConstraints) {
    FieldGenerator<T> generator = (FieldGenerator<T>) fieldGenerators.get(fieldType);
    return generator.buildFieldInstance(null, valueConstraints);
  }

  public <T extends FieldInstanceArtifact> List<T> buildMultipleEmptyInstances(FieldType fieldType, Optional<ValueConstraints> valueConstraints) {
    return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  public <T extends FieldInstanceArtifact> LinkedHashMap<String, T> buildAttributeValueField(Map<String, String> csvData, List<String> csvFields) {
    LinkedHashMap<String, T> attributeValueFieldInstances = new LinkedHashMap<>();
    if (csvFields != null) {
      for (var csvField : csvFields) {
        var value = csvData.get(csvField);
        if (value != null && !value.equals("")) {
          FieldGenerator<T> generator = (FieldGenerator<T>) fieldGenerators.get(FieldType.TEXTFIELD);
          attributeValueFieldInstances.put(csvField, generator.buildFieldInstance(value, Optional.empty()));
        }
      }
    }
    return attributeValueFieldInstances;
  }
}
