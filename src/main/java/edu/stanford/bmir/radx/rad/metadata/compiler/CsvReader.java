package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class SpreadsheetReader {
  public Map<String, String> readRadxRadSpreadsheet(String pathToFile) throws IOException {
    FileInputStream excelFile = new FileInputStream(new File(pathToFile));
    Workbook workbook = WorkbookFactory.create(excelFile);
    Sheet datatypeSheet = workbook.getSheetAt(0);
    Map<String, String> spreadsheetData = new HashMap<String, String>();

    for (int rowIndex = 1; rowIndex <= datatypeSheet.getLastRowNum(); rowIndex++) {
      var currentRow = datatypeSheet.getRow(rowIndex);
      if(currentRow != null){
        var field = currentRow.getCell(0);
        var value = currentRow.getCell(1);
        if (field != null && !field.getStringCellValue().equals("") && value!=null) {
          var metadata = getCellValueAsString(value);
          if(metadata != null && !metadata.equals("")){
            spreadsheetData.put(field.getStringCellValue(), metadata);
          }
        }
      }
    }
    workbook.close();
    return spreadsheetData;
  }

  public Map<String, String> readSpreadsheet2templatePath(String pathToFile) throws IOException {
    FileInputStream excelFile = new FileInputStream(pathToFile);
    Workbook workbook = WorkbookFactory.create(excelFile);
    Sheet datatypeSheet = workbook.getSheetAt(0);
    Map<String, String> template2Spreadsheet = new HashMap<String, String>();

    for (int rowIndex = 1; rowIndex <= datatypeSheet.getLastRowNum(); rowIndex++) {
      var currentRow = datatypeSheet.getRow(rowIndex);
      if (currentRow != null) {
        var radxRadfield = currentRow.getCell(0);
        var path = currentRow.getCell(1);
        if (radxRadfield != null) {
          var cleanedPath = cleanPathString(path.getStringCellValue());
          template2Spreadsheet.put(radxRadfield.getStringCellValue(), cleanedPath);
        }
      }
    }
    workbook.close();
    return template2Spreadsheet;
  }

  private String getCellValueAsString(Cell cell){
    switch (cell.getCellType()) {
      case STRING -> {
        return cell.getStringCellValue();
      }
      case NUMERIC -> {
        if (DateUtil.isCellDateFormatted(cell)) {
          Date date = cell.getDateCellValue();
          LocalDateTime localDateTime = date.toInstant()
              .atZone(ZoneId.systemDefault())
              .toLocalDateTime();
          LocalDate localDate = localDateTime.toLocalDate();
          return localDate.toString();
        } else {
          // Convert numeric value to String
//          return Double.toString(cell.getNumericCellValue());
          double value = cell.getNumericCellValue();
          if (Math.floor(value) == value) {
            // It's a whole number, convert to String without a decimal point
            return String.valueOf((long)value);
          } else {
            // It's a decimal number, convert to String as is
            return String.valueOf(value);
          }
        }
      }
      case BOOLEAN -> {
        return Boolean.toString(cell.getBooleanCellValue());
      }
      default -> {
        return null;
      }
    }
  }

  private String cleanPathString(String templatePath){
    String[] paths = templatePath.split("/");
    StringBuilder cleanedPath = new StringBuilder();

    for(String path: paths){
      String stripedPath = path.strip();
      if(!stripedPath.isEmpty()){
        cleanedPath.append("/" + stripedPath);
      }
    }
    return cleanedPath.toString();
  }
}
