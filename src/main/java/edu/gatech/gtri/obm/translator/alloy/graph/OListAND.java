package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.List;

public class OListAND<E> extends ArrayList<E> implements IObject {

  public OListAND() {
    super();
  }

  public OListAND(IObject o) {
    super();
    this.add((E) o);
  }



  public List<String> toStringAlloy() {
    List<String> ls = new ArrayList<>();
    for (E io : this) {
      List<String> ls2 = ((IObject) io).toStringAlloy();
      for (String s : ls2)
        ls.add(s);
    }
    return ls;
  }
}
