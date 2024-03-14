package edu.stanford.bmir.radx.rad.metadata.compiler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MeshCsvReader {

  /***
   * This method read the MESH ontology branch csv file
   * And return a map that key is prefLabel and value is class id.
   */
  public static Map<String, String> readCSVToMap() {
    Map<String, String> map = new HashMap<>();

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(
            MeshCsvReader.class.getClassLoader().getResourceAsStream("MESH.csv")))) {

      // Skip the header line
      br.readLine();

      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split(",");
        map.put(values[1], values[0]);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return map;
  }
}
