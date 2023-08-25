package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.List;
import org.graphstream.graph.Node;



public class ONode implements IObject {

  public static String node_attribute_name = "name";
  public static String type = "type";

  Node n;
  int numOneOf = 0;

  public ONode(Node _n, boolean oneof) {
    this.n = _n;
    numOneOf++;

  }

  public int getNumOneOf() {
    return this.numOneOf;
  }

  public ONode(Node _n) {
    this.n = _n;
  }

  public int minusOneOf() {
    this.numOneOf--;
    return numOneOf;
  }

  public int plusOneOf() {
    this.numOneOf++;
    return numOneOf;
  }

  public String toString() {
    return getName();
  }

  public String getName() {
    return (String) n.getAttribute(node_attribute_name);
  }

  public String getType() {
    return (String) n.getAttribute(type);
  }

  public Node getNode() {
    return this.n;
  }

  @Override
  public List<String> toStringAlloy() {
    List<String> l = new ArrayList<>();
    l.add(this.getName());
    return l;
  }

  // do nothing
  public void sort() {
    return;
  }

}
