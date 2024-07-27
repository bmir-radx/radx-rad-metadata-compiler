package edu.stanford.bmir.radx.rad.metadata.compiler;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class CsvReader {
  public Map<String, String> readCsvMetadata(String pathToFile) throws IOException {
    Map<String, String> csvData = new LinkedHashMap<>();

    try (FileReader reader = new FileReader(pathToFile);
         CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

      for (CSVRecord csvRecord : csvParser) {
        String field = csvRecord.get(0);
        String value = csvRecord.get(1);

        if (field != null && !field.isEmpty() && value != null && !value.isEmpty()) {
          csvData.put(field, value);
        }
      }
    }

    return csvData;
  }
}
