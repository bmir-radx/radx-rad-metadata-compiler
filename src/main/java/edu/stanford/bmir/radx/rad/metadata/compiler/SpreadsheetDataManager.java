package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.stanford.bmir.radx.rad.metadata.compiler.RadxRadFieldsConstant.KEYWORDS;

public class SpreadsheetDataManager {
  private final Pattern FIELD_PATTERN = Pattern.compile("^(?!study_include_prospective_or_retrospective_human_samples)(.+?)_?(\\d*)$");
  private final String FIRST_NAME_PATTERN = "^(pi|creator)_firstname_\\d+$";
  private final String MIDDLE_NAME_PATTERN = "^(pi|creator)_middlename_\\d+$";
  public final Map<String, List<String>> attributeValueMap = new HashMap<>(); //path-> List<spreadsheet fields>
  public final Map<String, Map<Integer, List<String>>> groupedData = new HashMap<>(); //{path->{index: [value1, value2]}}
  public final Map<String, Integer> elementInstanceCounts = new HashMap<>(); //{element: instances counts }

  public void groupData(Map<String, String> spreadsheetData,
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
            //Update element instances counts
            //TODO: need to update Map<String, Integer> elementInstanceCounts, add childElement: counts to map as well
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
                  .computeIfAbsent(index, k -> new ArrayList<>())
                  .add(value);
            }
          }
        } else{ // precision handling for field "study_include_prospective_or_retrospective_human_samples - effective_Feb_2021"
          var path = spreadsheet2templatePath.get(key);
          attributeValueMap.computeIfAbsent(path, k -> new ArrayList<>()).add(key);
        }
      }
    };

//    //merge contributor and creator data
//    mergeContributorAndCreatorData(groupedData);
  }

  /**
   * The RADx Metadata Specification 2.0 delete Data File Creators element, need to merge Creators metadata into Data File Contributors
   */
  private void mergeContributorAndCreatorData(Map<String, Map<Integer, List<String>>> groupedData) {
    String contributors = "Data File Contributors";
    String creators = "Data File Creators";

    Map<String, String> contributorToCreatorMap = new HashMap<>();
    contributorToCreatorMap.put("/Data File Contributors/Contributor Name", "/Data File Creators/Creator Name");
    contributorToCreatorMap.put("/Data File Contributors/Contributor Given Name", "/Data File Creators/Creator Given Name");
    contributorToCreatorMap.put("/Data File Contributors/Contributor Family Name", "/Data File Creators/Creator Family Name");
    contributorToCreatorMap.put("/Data File Contributors/Contributor Identifier", "/Data File Creators/Creator Identifier");
    contributorToCreatorMap.put("/Data File Contributors/Contributor Role", "/Data File Creators/Creator Role");
    contributorToCreatorMap.put("/Data File Contributors/Contributor Affiliation", "/Data File Creators/Creator Affiliation");
    contributorToCreatorMap.put("/Data File Contributors/Contributor Affiliation Identifier", "/Data File Creators/Creator Affiliation Identifier");
    contributorToCreatorMap.put("/Data File Contributors/Contributor Affiliation Identifier Scheme", "/Data File Creators/Creator Affiliation Identifier Scheme");

    var contributorCounts = elementInstanceCounts.get(contributors);
    var creatorCounts = elementInstanceCounts.get(creators);

    for (Map.Entry<String, String> entry : contributorToCreatorMap.entrySet()) {
      String contributorKey = entry.getKey();
      String creatorKey = entry.getValue();

      Map<Integer, List<String>> contributorData = groupedData.getOrDefault(contributorKey, new HashMap<>());
      Map<Integer, List<String>> creatorData = groupedData.getOrDefault(creatorKey, new HashMap<>());

      int contributorInstances = contributorData.size();
      int creatorInstances = creatorData.size();

      for (var i=1; i<=creatorCounts; i++) {
        List<String> creatorValues = creatorData.get(i);
        if(creatorValues != null){
          contributorData.put(contributorCounts + i, creatorValues);
        }
      }

      //update groupedData
      groupedData.put(contributorKey, contributorData);
      groupedData.remove(creatorKey);

      //update elementInstanceCounts
      elementInstanceCounts.put(contributors, contributorCounts + creatorCounts);
      elementInstanceCounts.remove(creators);
    }
  }
}
