package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.List;

// TODO: Auto-generated Javadoc
/** The Interface IObject. */
public interface IObject {

  /** The num one of. */
  int numOneOf = 0;

  /**
   * To string alloy.
   *
   * @return the list
   */
  public List<String> toStringAlloy();

  /** Sort. */
  public abstract void sort();
}
