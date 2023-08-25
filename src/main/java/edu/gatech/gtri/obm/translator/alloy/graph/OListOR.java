package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.List;

public class OListOR extends OListANDOR {

  public String toString() {
    String s = "(";
    for (IObject io : list) {
      s += io.toString() + "+ ";
    }
    return s.substring(0, s.lastIndexOf("+")).trim() + ")";
  }

  public List<String> toStringAlloy() {
    List<String> ls = new ArrayList<>();
    String s = "";
    for (IObject io : list) {
      s += "+" + io;
    }
    if (s.startsWith("+"))
      s = s.substring(s.indexOf("+") + 1);
    ls.add(s);
    return ls;
  }

}
