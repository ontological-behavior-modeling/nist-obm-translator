package edu.gatech.gtri.obm.alloy.translator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Utils {

  /**
   * Convert a given string with 1st letter upper case and others to be lower case.
   * 
   * @param s - the input string
   * @return the converted string
   */
  public static String firstCharUpper(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }


  /**
   * Add values to the given map if the given values is not null or size() != 0 for the given key.
   * 
   * @param map - a map (key = string, value = set of strings)
   * @param key - A string key of the map
   * @param values - Set<String> value of the map
   */
  public static void addToHashMap(Map<String, Set<String>> map, String key, Set<String> values) {
    if (values == null || values.size() == 0)
      return;
    map.computeIfAbsent(key, v -> new HashSet<String>()).addAll(values);
  }


}
