package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxRadFieldsConstant.KEYWORDS;

public class SpreadsheetDataManager {
  private final static Pattern FIELD_PATTERN = Pattern.compile("^(?!study_include_prospective_or_retrospective_human_samples)(.+?)_?(\\d*)$");
  private final static String FIRST_NAME_PATTERN = "^(pi|creator)_firstname_\\d+$";
  private final static String MIDDLE_NAME_PATTERN = "^(pi|creator)_middlename_\\d+$";
  /***
   * Group spreadsheet data in the following format:
   * {
   *   Element:{
   *     index{
   *       field1: [value],
   *       field2: [value],
   *       filed3(attribute-value): [spreadsheetField1, spreadsheetField2]
   *     }
   *   }
   * }
   * If field is attribute-value as field3, the list of values are keys of kay-value pairs.
   * @param spreadsheetData
   * @param spreadsheet2template
   * @return
   */
  public static Map<String, Map<Integer, Map<String, List<String>>>> groupData(
      Map<String, String> spreadsheetData,
      Map<String, FieldArtifact> spreadsheet2template,
      TemplateSchemaArtifact templateSchemaArtifact){

    Map<String, Map<Integer, Map<String, List<String>>>> groupedData = new HashMap<>();

    for (Map.Entry<String, String> entry : spreadsheetData.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      if(!Objects.equals(key, KEYWORDS.getValue())){
        Matcher matcher = FIELD_PATTERN.matcher(key);
        if (matcher.find()) {
          String spreadsheetField = matcher.group(1);
          String indexStr = matcher.group(2);
          Integer index = indexStr.isEmpty() ? 1 : Integer.parseInt(indexStr);

          var isFirstName = key.matches(FIRST_NAME_PATTERN);
          var isMiddleName = key.matches(MIDDLE_NAME_PATTERN);

          var element = spreadsheet2template.get(spreadsheetField).element();
          var field = spreadsheet2template.get(spreadsheetField).field();
          var specificationPath = "/" + element + "/" + field;

          if(AttributeValueFieldUtil.isAttributeValue(templateSchemaArtifact, specificationPath)){
            groupedData.computeIfAbsent(element, k -> new HashMap<>())
                .computeIfAbsent(1, k -> new HashMap<>())
                .computeIfAbsent(field, k -> new ArrayList<>())
                .add(key);
          } else{
            //Special handling for Contributor|Creator Given Name Fields
            if(isFirstName && value != null){
              String middleNameKey = key.replace("firstname", "middlename");
              String middleNameValue = spreadsheetData.getOrDefault(middleNameKey, "");
              value = value + " " + middleNameValue;
            }

            // Skip adding to groupedData if it's a middle name field by itself
            if(!isMiddleName){
              groupedData.computeIfAbsent(element, k -> new HashMap<>())
                  .computeIfAbsent(index, k -> new HashMap<>())
                  .computeIfAbsent(field, k -> new ArrayList<>())
                  .add(value);
            }
          }
        }
      }
    };
    return groupedData;
  }

}
