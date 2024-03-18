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
  public final static Map<String, List<String>> attributeValueMap = new HashMap<>(); //path-> List<spreadsheet fields>
  public final static Map<String, Map<Integer, String>> groupedData = new HashMap<>(); //{path->{index: value}}
  public final static Map<String, Integer> elementInstanceCounts = new HashMap<>(); //{element: instances counts }

  public static void groupData(Map<String, String> spreadsheetData,
                                Map<String, String> spreadsheet2templatePath,
                                TemplateSchemaArtifact templateSchemaArtifact){

    for (Map.Entry<String, String> entry : spreadsheetData.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      //skip keywords value which needs precision handling
      if(!Objects.equals(key, KEYWORDS.getValue())){
        Matcher matcher = FIELD_PATTERN.matcher(key);
        if (matcher.find()) {
          String spreadsheetField = matcher.group(1);
          String indexStr = matcher.group(2);
          Integer index = indexStr.isEmpty() ? 1 : Integer.parseInt(indexStr);

          var isFirstName = key.matches(FIRST_NAME_PATTERN);
          var isMiddleName = key.matches(MIDDLE_NAME_PATTERN);

          var path = spreadsheet2templatePath.get(spreadsheetField);

          //If it is attribute-value type, add to attributeValueMap
          if(AttributeValueFieldUtil.isAttributeValue(templateSchemaArtifact, path)){
            attributeValueMap.computeIfAbsent(path, k -> new ArrayList<>()).add(key);
          } else{
            //update element instances counts
            //TODO: need to update Map<String, Integer> elementInstanceCounts, add childElement: counts to map as well!!!
            var element = path.split("/")[1];
            elementInstanceCounts.merge(element, index, Math::max);

            //Special handling for Contributor|Creator Given Name Fields
            if(isFirstName && value != null){
              String middleNameKey = key.replace("firstname", "middlename");
              String middleNameValue = spreadsheetData.getOrDefault(middleNameKey, "");
              value = value + " " + middleNameValue;
            }

            // Skip adding to groupedData if it's a middle name field
            if(!isMiddleName){
              groupedData.computeIfAbsent(path, k -> new HashMap<>())
                  .put(index, value);
            }
          }
        }
      }
    };
  }
}
