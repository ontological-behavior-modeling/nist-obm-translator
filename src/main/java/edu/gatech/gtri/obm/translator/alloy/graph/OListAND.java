package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class OListAND. */
public class OListAND extends OListANDOR {

  /**
   * To string alloy.
   *
   * @return the list
   */
  public List<String> toStringAlloy() {
    List<String> ls = new ArrayList<>();
    for (IObject io : list) {
      List<String> ls2 = ((IObject) io).toStringAlloy();
      for (String s : ls2) ls.add(s);
    }
    return ls;
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return list.toString();
  }
}
