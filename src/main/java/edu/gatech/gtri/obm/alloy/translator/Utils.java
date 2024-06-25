package edu.gatech.gtri.obm.alloy.translator;

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
}
