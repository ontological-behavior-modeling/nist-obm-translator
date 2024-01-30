package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class OListOR. */
public class OListOR extends OListANDOR {

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    String s = "(";
    for (IObject io : list) {
      s += io.toString() + "+ ";
    }
    return s.substring(0, s.lastIndexOf("+")).trim() + ")";
  }

  /**
   * To string alloy.
   *
   * @return the list
   */
  public List<String> toStringAlloy() {
    List<String> ls = new ArrayList<>();
    String s = "";
    for (IObject io : list) {
      s += "+" + io;
    }
    if (s.startsWith("+")) s = s.substring(s.indexOf("+") + 1);
    ls.add(s);
    return ls;
  }
}
