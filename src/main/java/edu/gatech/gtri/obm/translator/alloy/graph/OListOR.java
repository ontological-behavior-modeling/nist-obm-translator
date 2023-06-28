package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.List;

public class OListOR implements IObject {

  List<IObject> list;

  public OListOR() {
    list = new ArrayList<>();
  }

  public OListOR(IObject o) {
    this();
    this.list.add(o);
  }

  public int size() {
    return list.size();
  }

  public void add(IObject _o) {
    this.list.add(_o);
  }

  public IObject get(int _index) {
    return list.get(_index);
  }

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
