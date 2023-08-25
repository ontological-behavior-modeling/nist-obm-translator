package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class OListANDOR implements IObject {

  List<IObject> list;

  public OListANDOR() {
    list = new ArrayList<>();
  }

  public OListANDOR(IObject o) {
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

  public abstract String toString();

  public abstract List<String> toStringAlloy();

  public void sort() {
    Map<String, IObject> map = new HashMap<>();
    List<String> names = new ArrayList<>();
    for (IObject o : list) {
      if (o instanceof ONode) {
        map.put(((ONode) o).getName(), o);
        names.add(((ONode) o).getName());
      } else {
        System.err.println("NOt supported yet");
      }
    }
    Collections.sort(names);
    list = new ArrayList<>();
    for (String name : names) {
      list.add(map.get(name));
    }
  }
}
