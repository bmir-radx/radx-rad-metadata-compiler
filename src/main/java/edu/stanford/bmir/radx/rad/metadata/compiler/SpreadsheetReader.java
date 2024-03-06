package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        if (field != null && value != null) {
          fieldValues.put(field.getStringCellValue(), getCellValueAsString(value));
        }
      }
    }
    workbook.close();
    return fieldValues;
  }

  public Map<String, FieldArtifact> readSpreadsheet2Template(String pathToFile) throws IOException {
    FileInputStream excelFile = new FileInputStream(new File(pathToFile));
    Workbook workbook = WorkbookFactory.create(excelFile);
    Sheet datatypeSheet = workbook.getSheetAt(0);
    Map<String, FieldArtifact> spreadsheet2Template = new HashMap<String, FieldArtifact>();

    for (int rowIndex = 1; rowIndex <= datatypeSheet.getLastRowNum(); rowIndex++) {
      var currentRow = datatypeSheet.getRow(rowIndex);
      if (currentRow != null) {
        var radxRadfield = currentRow.getCell(0);
        var element = currentRow.getCell(1);
        var field = currentRow.getCell(2);
        if (radxRadfield != null) {
          spreadsheet2Template.put(radxRadfield.getStringCellValue(),
              new FieldArtifact(element.getStringCellValue(), field.getStringCellValue()));
        }
      }
    }
    workbook.close();
    return spreadsheet2Template;
  }

  private String getCellValueAsString(Cell cell){
    switch (cell.getCellType()) {
      case STRING -> {
        return cell.getStringCellValue();
      }
      case NUMERIC -> {
        if (DateUtil.isCellDateFormatted(cell)) {
          // Format date value as required
          return cell.getDateCellValue().toString();
        } else {
          // Convert numeric value to String
          return Double.toString(cell.getNumericCellValue());
        }
      }
      case BOOLEAN -> {
        return Boolean.toString(cell.getBooleanCellValue());
      }
      default -> {
        return "";
      }
    }
  }
}
