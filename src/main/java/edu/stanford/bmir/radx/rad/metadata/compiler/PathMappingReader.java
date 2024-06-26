package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PathMappingReader {
  public Map<String, String> readCsv2templatePath(String pathToFile) throws IOException {
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
