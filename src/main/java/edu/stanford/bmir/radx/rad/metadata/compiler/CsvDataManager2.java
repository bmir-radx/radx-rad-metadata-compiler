package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvDataManager2 {
  private final Pattern FIELD_PATTERN = Pattern.compile("^(?!study_include_prospective_or_retrospective_human_samples)(.+?)_?(\\d*)$");
  private final String FIRST_NAME_PATTERN = "^(pi|creator)_firstname_\\d+$";
  private final String MIDDLE_NAME_PATTERN = "^(pi|creator)_middlename_\\d+$";
  private final String CREATOR_FULLNAME_PREFIX = "creator_fullname";
  private final String CREATOR_PREFIX = "creator_";
  private final String CREATOR_ROLE = "Creator";
  private final String DATA_FILE_CREATION_DATETIME = "data_file_creation_dateTime";
  private final String CONTRIBUTOR_ELEMENT_PATH = "/contributors";
  private final String CONTRIBUTOR_ROLE_PATH = "/contributors/contributorRoles";
  private final String AUXILIARY_METADATA_KV_PAIRS_PATH = "/Auxiliary Metadata/Data File Descriptive Key-Value Pairs";
  private final Map<String, List<String>> attributeValueMap = new HashMap<>(); //normalized path-> List<csv fields>

  //field path to value map, the path should be with index. e.g. /Keyword[1]
  private final Map<String, String> groupedData = new HashMap<>();

  // count the instance for field, the last index should be normalized. e.g. /Contributors[1]/Contributor identifiers[1]/Contributor identifier
  private final Map<String, Integer> fieldsCounts = new HashMap<>();

  // count the instance for element, including parentElement and childElement, also normalized the last index.
  // e.g. "/Contributors":3, "/Contributors[1]/Contributor identifiers":2
  private final Map<String, Integer> elementCounts = new HashMap<>();
  private int piNumber = 0;
  private boolean hasGetPiNumber = false;

  public Map<String, List<String>> getAttributeValueMap() {
    return attributeValueMap;
  }

  public Map<String, String> getGroupedData() {
    return groupedData;
  }

  public Map<String, Integer> getFieldsCounts() {
    return fieldsCounts;
  }

  public Map<String, Integer> getElementCounts() {
    return elementCounts;
  }

  public void groupData(Map<String, String> csvData,
                        Map<String, String> csv2templatePath,
                        TemplateSchemaArtifact templateSchemaArtifact){
    var isFirstVersionTemplate = isFirstVersionTemplate(templateSchemaArtifact);

    for (Map.Entry<String, String> entry : csvData.entrySet()) {
      var hasId = false;
      String key = entry.getKey();
      String value = entry.getValue();

      //TODO keywords, publication url(av?), pi_role and specimen_type_used
      Matcher matcher = FIELD_PATTERN.matcher(key);
      if (matcher.find()) {
        String csvAttribute = matcher.group(1);
        String indexStr = matcher.group(2);
        Integer index = indexStr.isEmpty() ? 1 : Integer.parseInt(indexStr);

        var isFirstName = key.matches(FIRST_NAME_PATTERN);
        var isMiddleName = key.matches(MIDDLE_NAME_PATTERN);
        //Special handling for Contributor|Creator Given Name Fields
        if(isFirstName && value != null){
          value = reconstructFirstName(csvData, key, value);
        }

        var path = csv2templatePath.get(csvAttribute);

        //If it is attribute-value type, add to attributeValueMap
        if(AttributeValueFieldUtil.isAttributeValue(templateSchemaArtifact, path)){
          attributeValueMap.computeIfAbsent(path, k -> new ArrayList<>()).add(key);
          updateAvElementCount(path);
        }
        else{
          if(isField(path)){
            processValues(value, path, null, hasId);
          } else if(!isMiddleName){
            hasId = isFirstVersionTemplate? false :updateHasIdStatus(path, csvData);
            parseElementFieldValue(key, value, csvAttribute, path, index, hasId, isFirstVersionTemplate);
          }
        }
      } else{ // precision handling for field "study_include_prospective_or_retrospective_human_samples - effective_Feb_2021"
        var path = csv2templatePath.get(key);
        attributeValueMap.computeIfAbsent(path, k -> new ArrayList<>()).add(key);
        updateAvElementCount(path);
      }
    };
  }

  private String reconstructFirstName(Map<String, String> csvData, String path, String value){
    String middleNameKey = path.replace("firstname", "middlename");
    String middleNameValue = csvData.getOrDefault(middleNameKey, "");
    return value + " " + middleNameValue;
  }

  private boolean isField(String path){
    String[] pathParts = path.split("/");
    return pathParts.length == 2;
  }

  private void processValues(String value, String path, Integer parentElementIndex, boolean hasId){
    if(value != null && !value.isEmpty()){
      String[] values = value.split("\\|");
      for (int i=0; i<values.length; i++) {
        var val = values[i].trim();
        var indexedPath = getIndexedPath(path, parentElementIndex, i, hasId);
        groupedData.put(indexedPath, val);
        updateElementCount(indexedPath);
        updateFieldCount(indexedPath);
      }
    }
  }

  /**
   * This method contains precision handling for:
   * 1. Manually path data_file_creation_dateTime and save the original value in Auxiliary Metadata for Specification 1.0
   * 2. Creator elements for Specification 2.0
   */
  private void parseElementFieldValue(String csvField, String value, String csvAttribute, String mappedPath, Integer parentElementIndex, boolean hasId, boolean isFirstVersionTemplate){
    if(csvAttribute.startsWith(DATA_FILE_CREATION_DATETIME) && isFirstVersionTemplate){
      //path the dateTime value to date
      //save the original value to Auxiliary Metadata Key-Value Pair.
      value = convertToDate(value);
      attributeValueMap.computeIfAbsent(AUXILIARY_METADATA_KV_PAIRS_PATH, k -> new ArrayList<>()).add(csvField);
      updateAvElementCount(AUXILIARY_METADATA_KV_PAIRS_PATH);
    }

    if(csvAttribute.startsWith(CREATOR_PREFIX) && !isFirstVersionTemplate){
      if(csvAttribute.startsWith(CREATOR_FULLNAME_PREFIX)){
        if(!hasGetPiNumber){
          piNumber = elementCounts.getOrDefault(CONTRIBUTOR_ELEMENT_PATH, 0);
          hasGetPiNumber = true;
        }

        parentElementIndex += piNumber;
        //add creator role
        processValues(CREATOR_ROLE, CONTRIBUTOR_ROLE_PATH, parentElementIndex, hasId);
      } else{
        parentElementIndex += piNumber;
      }
    }

    processValues(value, mappedPath, parentElementIndex, hasId);
  }

  private String getIndexedPath(String path, Integer parentElementIndex, Integer fieldIndex, boolean hasId){
    //get parent element and child element, child element is null if not a nested element field
    //add index to parent element
    //add index to child element. If hasId, index is 2, otherwise is 1
    //return the indexedString
    // Split the path into parts
    String[] pathParts = path.split("/");
    String parentElement = pathParts.length>2 ? pathParts[1] : null;
    String childElement = pathParts.length == 4 ? pathParts[2] : null;
    String field = pathParts[pathParts.length - 1];

    // Initialize the indexed path with the parent element
    String indexedPath = "";
    if(parentElement != null){
      parentElementIndex--; //let index start from 0
      indexedPath = "/" + parentElement + "[" + parentElementIndex + "]";
    }

    // Add index to child element if present
    if (childElement != null) {
      int childIndex = hasId ? 1 : 0;
      String indexedChildElement = childElement + "[" + childIndex + "]";
      indexedPath += "/" + indexedChildElement;
    }

    // Add field index to field
    String indexedField = field + "[" + fieldIndex + "]";
    indexedPath += "/" + indexedField;

    return indexedPath;
  }

  private void updateElementCount(String indexedPath){
    String[] pathParts = indexedPath.split("/");
    Pattern pattern = Pattern.compile("^(.*?)(\\[(\\d+)\\])?$");

    for (int i = 1; i < pathParts.length - 1; i++) {
      String part = pathParts[i];
      Matcher matcher = pattern.matcher(part);

      if (matcher.find()) {
        String indexStr = matcher.group(3);

        // Default to 0 if no index is found
        int index = indexStr != null ? Integer.parseInt(indexStr) : 0;
        index++;

        // Reconstruct the full element path up to this part
        String elementPath = "/" + String.join("/", java.util.Arrays.copyOfRange(pathParts, 1, i + 1));
        var normalizedPath = removeLastIndex(elementPath);
        elementCounts.put(normalizedPath, Math.max(elementCounts.getOrDefault(normalizedPath, 1), index));
      }
    }
  }

  private void updateAvElementCount(String path){
    String[] pathParts = path.split("/");
    String parentElement = pathParts.length>2 ? pathParts[1] : null;
    String parentElementPath = "/" + parentElement;
    if(!elementCounts.containsKey(parentElementPath)){
      elementCounts.put(parentElementPath, 1);
    }
  }

  private void updateFieldCount(String indexedPath){
    //normalize the field index
    var normalizedPath = removeLastIndex(indexedPath);
    fieldsCounts.put(normalizedPath, fieldsCounts.getOrDefault(normalizedPath, 0) + 1);
  }

  private String removeLastIndex(String indexedPath){
    var lastBracketIndex = indexedPath.lastIndexOf('[');
    return indexedPath.substring(0, lastBracketIndex) + indexedPath.substring(lastBracketIndex).replaceAll("\\[\\d+\\]", "");
  }

  private boolean updateHasIdStatus(String path, Map<String, String> csvData){
    var hasId = false;
    if (path.matches("(creator|pi)_orcid_\\d+")) {
      String profileIdPath = path.replace("orcid", "profileid");
      if (csvData.containsKey(profileIdPath) && csvData.get(profileIdPath) != null && !csvData.get(profileIdPath).isEmpty() && !csvData.get(profileIdPath).isBlank()) {
        hasId = true;
      }
    }
    return hasId;
  }

  private boolean isFirstVersionTemplate(TemplateSchemaArtifact templateSchemaArtifact){
    return TemplateVersion.isFirstVersion(templateSchemaArtifact.jsonLdId());
  }

  private String convertToDate(String dateTime){
    return dateTime.split("T")[0];
  }
}
