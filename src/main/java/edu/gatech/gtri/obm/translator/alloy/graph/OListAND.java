package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OListAND extends ArrayList implements IObject {

  public OListAND() {
    super();
  }

  public OListAND(IObject o) {
    super();
    this.add(o);
  }

  // public String toString() {
  // String s = "(";
  //
  // for (Iterator<IObject> iter = this.iterator(); iter.hasNext();) {
  // s += iter.next().toString() + "+ ";
  // }
  // return s.substring(0, s.lastIndexOf("+")).trim() + ")";
  // }
  // public List<String> toStringAlloy() {
  // System.out.println("OListAND" + this.size());
  // List<String> ls = new ArrayList<>();
  // for (Iterator<IObject> iter = this.iterator(); iter.hasNext();) {
  // IObject o = iter.next();
  //
  // List<String> ls2 = o.toStringAlloy();
  // for (Iterator<String> iters = ls2.iterator(); iters.hasNext();)
  // ls.add(iters.next());
  // }
  // return ls;
  // }
  public List<String> toStringAlloy() {
    System.out.println("OListAND" + this.size());
    List<String> ls = new ArrayList<>();
    for (Iterator<IObject> iter = this.iterator(); iter.hasNext();) {
      IObject o = iter.next();

      List<String> ls2 = o.toStringAlloy();
      for (Iterator<String> iters = ls2.iterator(); iters.hasNext();)
        ls.add(iters.next());
    }
    return ls;
  }


}
