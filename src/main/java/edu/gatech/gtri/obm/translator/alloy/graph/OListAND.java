package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.List;

public class OListAND extends OListANDOR {


  public List<String> toStringAlloy() {
    List<String> ls = new ArrayList<>();
    for (IObject io : list) {
      List<String> ls2 = ((IObject) io).toStringAlloy();
      for (String s : ls2)
        ls.add(s);
    }
    return ls;
  }

  public String toString() {
    return list.toString();
  }
}
