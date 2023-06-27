package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OListOR extends ArrayList implements IObject {

  public OListOR() {
    super();
  }

  public OListOR(IObject o) {
    super();
    this.add(o);
  }

  public String toString() {
    String s = "(";

    for (Iterator<IObject> iter = this.iterator(); iter.hasNext();) {
      s += iter.next().toString() + "+ ";
    }
    return s.substring(0, s.lastIndexOf("+")).trim() + ")";
  }

  public List<String> toStringAlloy() {
    System.out.println("OListOR:" + this.size());
    List<String> ls = new ArrayList<>();
    String s = "";
    for (Iterator<IObject> iter = this.iterator(); iter.hasNext();) {
      s += "+" + iter.next();
    }
    if (s.startsWith("+"))
      s = s.substring(s.indexOf("+") + 1);
    ls.add(s);
    return ls;
  }
}
