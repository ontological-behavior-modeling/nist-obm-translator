package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/** The Class OListANDOR. */
public abstract class OListANDOR implements IObject {

  /** The list. */
  List<IObject> list;

  /** Instantiates a new o list ANDOR. */
  public OListANDOR() {
    list = new ArrayList<>();
  }

  /**
   * Instantiates a new o list ANDOR.
   *
   * @param o the o
   */
  public OListANDOR(IObject o) {
    this();
    this.list.add(o);
  }

  /**
   * Size.
   *
   * @return the int
   */
  public int size() {
    return list.size();
  }

  /**
   * Adds the.
   *
   * @param _o the o
   */
  public void add(IObject _o) {
    this.list.add(_o);
  }

  /**
   * Gets the.
   *
   * @param _index the index
   * @return the i object
   */
  public IObject get(int _index) {
    return list.get(_index);
  }

  /**
   * To string.
   *
   * @return the string
   */
  public abstract String toString();

  /**
   * To string alloy.
   *
   * @return the list
   */
  public abstract List<String> toStringAlloy();

  /** Sort. */
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
