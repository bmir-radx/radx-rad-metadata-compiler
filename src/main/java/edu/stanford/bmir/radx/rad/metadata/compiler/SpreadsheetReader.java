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
    Map<String, String> fieldValues = new HashMap<String, String>();

    for (int rowIndex = 1; rowIndex <= datatypeSheet.getLastRowNum(); rowIndex++) {
      var currentRow = datatypeSheet.getRow(rowIndex);
      if(currentRow != null){
        var field = currentRow.getCell(0);
        var value = currentRow.getCell(1);
        if (field != null && !field.getStringCellValue().equals("") && value!=null) {
          var metadata = getCellValueAsString(value);
          if(metadata != null && !metadata.equals(""))
          fieldValues.put(field.getStringCellValue(), metadata);
        }
      }
    }
    workbook.close();
    return fieldValues;
  }

  public Map<String, FieldPath> readSpreadsheet2Template(String pathToFile) throws IOException {
    FileInputStream excelFile = new FileInputStream(new File(pathToFile));
    Workbook workbook = WorkbookFactory.create(excelFile);
    Sheet datatypeSheet = workbook.getSheetAt(0);
    Map<String, FieldPath> spreadsheet2Template = new HashMap<String, FieldPath>();

    for (int rowIndex = 1; rowIndex <= datatypeSheet.getLastRowNum(); rowIndex++) {
      var currentRow = datatypeSheet.getRow(rowIndex);
      if (currentRow != null) {
        var radxRadfield = currentRow.getCell(0);
        var element = currentRow.getCell(1);
        var field = currentRow.getCell(2);
        if (radxRadfield != null) {
          spreadsheet2Template.put(radxRadfield.getStringCellValue(),
              new FieldPath(element.getStringCellValue(), field.getStringCellValue()));
        }
      }
    }
    workbook.close();
    return spreadsheet2Template;
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
          template2Spreadsheet.put(radxRadfield.getStringCellValue(), path.getStringCellValue());
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
}
