package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.List;

public interface IObject {
  int numOneOf = 0;

  public List<String> toStringAlloy();

  public abstract void sort();
}
