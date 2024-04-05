package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.graphstream.graph.BreadthFirstIterator;
import org.graphstream.graph.DepthFirstIterator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

// TODO: Auto-generated Javadoc
/** The Class Graph2AlloyExpr. */
public class Graph2AlloyExpr {

  /** The graph. */
  Graph graph;

  /**
   * Creates the graph.
   *
   * @return the graph
   */
  public static Graph createGraph() {
    System.setProperty("org.graphstream.ui", "swing");

    Graph graph = new SingleGraph("test");
    graph.setStrict(true);
    graph.setAttribute("ui.stylesheet", styleSheet);
    return graph;
  }

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {

    Graph2AlloyExpr ga = new Graph2AlloyExpr();
    try {
      Graph graph = ga.getGraph();
      GraphUtil.loop(ga.getGraph());
      // GraphUtil.simplesequence(ga.getGraph());
      // GraphUtil.decision(ga.getGraph());
      // GraphUtil.forkjoin(ga.getGraph());
      // GraphUtil.allcontrol(graph);
      // GraphUtil.ex1(graph);
      display(graph);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Map<IObject, IObject> hb = ga.getHappensBeforeFunction();
    System.out.println(hb);
    print(hb);
    Map<IObject, IObject> hbinv = ga.getHappensBeforeInvFunction();
    System.out.println(hbinv);
    print(hbinv);
  }

  /** Instantiates a new graph 2 alloy expr. */
  public Graph2AlloyExpr() {
    this.graph = createGraph();
  }

  /**
   * Gets the graph.
   *
   * @return the graph
   */
  public Graph getGraph() {
    if (this.graph == null) this.graph = createGraph();
    return this.graph;
  }

  /**
   * Adds the node.
   *
   * @param _name the name
   */
  public void addNode(String _name) {
    GraphUtil.createNode(graph, _name);
  }

  /**
   * Adds the edge.
   *
   * @param _name the name
   * @param _sourceName the source name
   * @param _targetName the target name
   * @return the edge
   */
  public Edge addEdge(String _name, String _sourceName, String _targetName) {
    return graph.addEdge(
        _name,
        Graph2AlloyExpr.getNodeByName(graph, _sourceName),
        Graph2AlloyExpr.getNodeByName(graph, _targetName),
        true);
  }

  /** Display. */
  public void display() {
    graph.display();
  }

  /**
   * Gets the happens before function.
   *
   * @return the happens before function
   */
  public Map<IObject, IObject> getHappensBeforeFunction() {

    // display(graph);
    Set<Node> visited = new HashSet<>();
    Map<IObject, IObject> fn = new HashMap<>();
    Optional<Node> op = graph.nodes().filter(n -> !visited.contains(n)).findFirst();
    while (op.isPresent()) {
      functionHBFn(fn, new ONode(op.get()), visited);
      op = graph.nodes().filter(n -> !visited.contains(n)).findFirst();
    }
    return fn;
  }

  /**
   * Gets the happens before inv function.
   *
   * @return the happens before inv function
   */
  public Map<IObject, IObject> getHappensBeforeInvFunction() {

    // display(graph);
    Set<Node> visited = new HashSet<>();
    Map<IObject, IObject> fn = new HashMap<>();
    Optional<Node> op = graph.nodes().filter(n -> !visited.contains(n)).findFirst();
    while (op.isPresent()) {
      functionHBInvFn(fn, new ONode(op.get()), visited);
      op = graph.nodes().filter(n -> !visited.contains(n)).findFirst();
    }
    return fn;
  }

  /**
   * Function HB fn.
   *
   * @param hb the hb
   * @param source the source
   * @param _visited the visited
   */
  public void functionHBFn(Map<IObject, IObject> hb, IObject source, Set<Node> _visited) {

    if (source instanceof ONode) {
      Node sourceNode = ((ONode) source).getNode();
      System.out.println(source.toString());

      _visited.add(sourceNode);

      Optional<Edge> oneofEdges = sourceNode.leavingEdges().filter(e -> isOneOf(e)).findAny();
      if (oneofEdges.isPresent()) {

        // merging = source -> target, another source -> the same target
        if (sourceNode.leavingEdges().count() == 1) {
          OListOR sourceOR = new OListOR();

          Node targetNode = sourceNode.getLeavingEdge(0).getTargetNode();
          targetNode
              .enteringEdges()
              .forEach(
                  edge -> {
                    // TODO validate: all edges should be one of and all sourceNodes should have one
                    // leaving
                    // edge.
                    Node sourceNodeOrOtherSourceNode = edge.getSourceNode();
                    if (sourceNodeOrOtherSourceNode
                        != targetNode) { // change p1-> p1+p2 to p1->p2 for
                      // self-loop
                      _visited.add(sourceNodeOrOtherSourceNode); // sourceNode is added twice but ok
                      sourceOR.add(new ONode(sourceNodeOrOtherSourceNode));
                    }
                  });
          hb.put(sourceOR, new ONode(targetNode));
        } else { // decision- sourceNode.leavingEdges().count() > 1 source -> target1 and source ->
          // target2
          OListOR targetOR = new OListOR();
          sourceNode
              .leavingEdges()
              .forEach(
                  edge -> {
                    Node targetNode = edge.getTargetNode();
                    targetOR.add(new ONode(targetNode));
                  });
          hb.put(source, targetOR);
        }

      } else {
        // join or simplesequence
        if (sourceNode.leavingEdges().count() == 1) {
          hb.put(source, new ONode(sourceNode.getLeavingEdge(0).getTargetNode()));
        } // merge
        else if (sourceNode.leavingEdges().count() > 1) {
          OListAND targetAND = new OListAND();
          sourceNode
              .leavingEdges()
              .forEach(
                  edge -> {
                    Node targetNode = edge.getTargetNode();
                    // all targetNode should be not one-of
                    targetAND.add(new ONode(targetNode));
                  });
          hb.put(source, targetAND);
        }
      }
    } // not ONode
  }

  /**
   * Function HB inv fn.
   *
   * @param hb the hb
   * @param inode the inode
   * @param _visited the visited
   */
  public static void functionHBInvFn(Map<IObject, IObject> hb, IObject inode, Set<Node> _visited) {

    if (inode instanceof ONode) {
      Node targetNode = ((ONode) inode).getNode();
      _visited.add(targetNode);

      Optional<Edge> oneofEdges = targetNode.enteringEdges().filter(e -> isOneOf(e)).findAny();
      if (oneofEdges.isPresent()) {
        // decision = sourceNode -> target && sourceNode -> different targetNodes
        if (targetNode.enteringEdges().count() == 1) {
          OListOR targetOR = new OListOR();
          Node sourceNode = targetNode.getEnteringEdge(0).getSourceNode();
          // add targetNodes from sourceNode(targetNode's sourceNode) to targetOR
          // the target and other targets connected from the target's source
          // TODO: validate other edges from the sourceNode should be one-of and the other
          // targetNods should have one one-of entering edges
          sourceNode
              .leavingEdges()
              .forEach(
                  edge -> {
                    Node otherTargetNodesFromTheSource = edge.getTargetNode(); // this include the
                    // targetNode
                    if (otherTargetNodesFromTheSource
                        != sourceNode) { // change p2 -> p2+p3 to p2-> p3 for
                      // self-loop
                      _visited.add(
                          otherTargetNodesFromTheSource); // the targetNode in argument is added
                      // twice but ok
                      targetOR.add(new ONode(otherTargetNodesFromTheSource));
                    }
                  });
          hb.put(new ONode(sourceNode), targetOR);
        } else { // must be > 1 since at least one found as one-of
          OListOR sourceOR = new OListOR();
          // merge =
          targetNode
              .enteringEdges()
              .forEach(
                  edge -> {
                    Node sourceNode = edge.getSourceNode();
                    sourceOR.add(new ONode(sourceNode));
                  });
          hb.put(sourceOR, new ONode(targetNode));
        }
      } else { // not one of
        long targetNodeEnteringCount = targetNode.enteringEdges().count();
        // fork or ss
        if (targetNodeEnteringCount == 1)
          hb.put(new ONode(targetNode.getEnteringEdge(0).getSourceNode()), new ONode(targetNode));
        // join
        else if (targetNodeEnteringCount > 1) {
          OListAND andSource = new OListAND();
          targetNode
              .enteringEdges()
              .forEach(
                  edge -> {
                    andSource.add(new ONode(edge.getSourceNode()));
                  });
          hb.put(andSource, new ONode(targetNode));
        }
      }
    }
  }

  /**
   * Gets the out degree minus self loop.
   *
   * @param node the node
   * @return the out degree minus self loop
   */
  public static int getOutDegreeMinusSelfLoop(Node node) {
    int numOfSelfLoop = 0;
    Iterator<Edge> iter = node.leavingEdges().iterator();
    while (iter.hasNext()) {
      if (iter.next().getTargetNode() == node) numOfSelfLoop++;
    }
    return node.getOutDegree() - numOfSelfLoop;
  }

  /**
   * {p1=p3, p0=[p1, p2], p2=p3}.
   *
   * @param _map the map
   * @return 0: p1, p3 1: p0, p1 2: p0, p2 3: p2, p3
   */
  public static List<String> getFnString(Map<IObject, IObject> _map) {

    List<String> hbFunctionFilter = new ArrayList<>();
    for (IObject key : _map.keySet()) {
      for (String keyString : key.toStringAlloy()) { // p1, p0, p2
        for (String value : _map.get(key).toStringAlloy()) { // p1, (p1, p2), p3
          hbFunctionFilter.add(keyString + ", " + value);
        }
      }
    }
    return hbFunctionFilter;
  }

  /**
   * Prints the.
   *
   * @param _map the map
   */
  public static void print(Map<IObject, IObject> _map) {
    System.out.println(_map);
    List<String> hbFunctionFilter = getFnString(_map);
    for (int i = 0; i < hbFunctionFilter.size(); i++) {
      System.out.println(i + ": " + hbFunctionFilter.get(i));
    }
  }

  /**
   * Happens before.
   *
   * @param source the source
   * @param leavingEdgeOneOf the leaving edge one of
   * @return the map
   */
  public static Map<IObject, IObject> happensBefore(ONode source, boolean leavingEdgeOneOf) {
    Map<IObject, IObject> hb = new HashMap<>();

    if (leavingEdgeOneOf) {
      OListOR or = new OListOR();
      source.getNode().leavingEdges().forEach(e -> or.add(get(e.getTargetNode(), true)));
      if (or.size() > 0) {
        System.out.println("OR-adding to map key = " + source.getName() + " value = " + or);

        hb.put(source, or);
        for (int i = 0; i < or.size(); i++) {
          IObject o = (IObject) or.get(i);
          if (o instanceof ONode) {
            Map<IObject, IObject> newMap = happensBefore((ONode) or.get(i), true);
            IObject v = newMap.get((ONode) or.get(i));
            hb.put(or, v); // both OR nodes should pointing to the same node.
            break;
          }
        }
      }

    } else if (source.getNode().leavingEdges().count() > 0) {
      OListAND and = new OListAND();
      source.getNode().leavingEdges().forEach(e -> and.add(get(e.getTargetNode(), false)));
      if (and.size() > 0) {
        System.out.println("AND-adding to map key = " + source.getName() + " value = " + and);
        hb.put(source, and);
        for (int i = 0; i < and.size(); i++) {
          IObject o = (IObject) and.get(i);
          if (o instanceof ONode) {
            Map<IObject, IObject> newMap = happensBefore(((ONode) and.get(i)), false);
            hb.putAll(newMap);
          }
        }
      }
    }
    return hb;
  }

  /**
   * Gets the.
   *
   * @param target the target
   * @param oneof the oneof
   * @return the i object
   */
  public static IObject get(Node target, boolean oneof) {
    ONode node = new ONode(target, oneof);
    System.out.println("get: " + target.getAttribute(ONode.node_attribute_name));

    if (target.leavingEdges().count() == 1) {
      Edge edge = target.leavingEdges().iterator().next();
      Node newTarget = edge.getTargetNode();
      if (newTarget.enteringEdges().count() > 1) {
        if (isOneOf(edge) == true) { // merge
          int numOfOneOf = node.minusOneOf();
          if (numOfOneOf == 0) {
            return new ONode(target, oneof);
          } else return new ONode(target, oneof);
        } else { // join
          return new ONode(target, oneof);
        }
      }
    } else if (target.leavingEdges().count() > 1) {
      OListAND listAnd = new OListAND();
      for (Iterator<Edge> iter = target.leavingEdges().iterator(); iter.hasNext(); ) {
        Edge edge = iter.next();
        Node newTarget = edge.getTargetNode();
        // listAnd.add(get(newTarget, oneof));
        listAnd.add(get(newTarget, oneof));
      }
      return listAnd;
    } else { // no target's leaving Edges
      // if (oneof)
      return new ONode(target, oneof);
      // else
      // return new OPathList(new ONode(target, false));
    }
    return null;
  }

  /**
   * Checks if is one of.
   *
   * @param edge the edge
   * @return true, if is one of
   */
  public static boolean isOneOf(Edge edge) {
    if (edge.getAttribute("oneof") == null) return false;
    return (Boolean) edge.getAttribute("oneof");
  }

  /**
   * Gets the node by name.
   *
   * @param graph the graph
   * @param _nodeName the node name
   * @return the node by name
   */
  public static Node getNodeByName(Graph graph, String _nodeName) {
    for (Node node : graph) {
      if (node.getAttribute(ONode.node_attribute_name).equals(_nodeName)) return node;
    }
    return null;
  }

  /** The style sheet. */
  protected static String styleSheet =
      "node {"
          + "   fill-color: black; text-color: black; text-alignment:above; text-size:20;"
          + "}"
          + "node.marked {"
          + "   fill-color: red;"
          + "} edge { fill-color: black;} ";

  /**
   * Display.
   *
   * @param graph the graph
   */
  public static void display(Graph graph) {
    graph.setAttribute("ui.stylesheet", styleSheet);
    for (Node node : graph)
      node.setAttribute("ui.label", node.getAttribute(ONode.node_attribute_name));
    for (int i = 0; i < graph.getEdgeCount(); i++)
      graph
          .getEdge(i)
          .setAttribute("ui.label", graph.getEdge(i).getAttribute("oneof") != null ? "oneof" : "");
    graph.display();
  }

  /**
   * Explore.
   *
   * @param source the source
   */
  public static void explore(Node source) {

    // Map<Integer, IObject> z = new HashMap<>();
    // Optional<Edge> oneofEdges =
    // source.leavingEdges().filter(e -> ((Boolean) e.getAttribute("oneof")) == true).findAny();
    //
    // int index = 1;
    // Stream<Edge> leavingEdges = source.leavingEdges();
    // if (oneofEdges.isPresent()) {
    // OListOR listOr = new OListOR();
    // source.leavingEdges().forEach(e -> listOr.add(new ONode(e.getTargetNode(), true)));
    // z.put(index, listOr);
    // } else if (source.leavingEdges().count() > 1) {
    // OListAND listAnd = new OListAND();
    // source.leavingEdges().forEach(e -> listAnd.add(new ONode(e.getTargetNode())));
    // z.put(index, listAnd);
    // } else {
    // source.leavingEdges().forEach(e -> {
    // ONode on = new ONode(e.getTargetNode());
    // z.put(index, on);
    // });
    // }
  }

  /**
   * Exolore depth first.
   *
   * @param source the source
   */
  public static void exoloreDepthFirst(Node source) {
    DepthFirstIterator k = new DepthFirstIterator(source);

    while (k.hasNext()) {
      Node next = k.next();
      System.out.println(next.getAttribute(ONode.node_attribute_name) + " " + k.getDepthOf(next));
      next.setAttribute("ui.class", "marked");
      sleep();
    }
  }

  /**
   * Explore breath first.
   *
   * @param source the source
   */
  public static void exploreBreathFirst(Node source) {
    BreadthFirstIterator k = new BreadthFirstIterator(source);
    // Iterator<? extends Node> k = source.getBreadthFirstIterator(true);

    while (k.hasNext()) {
      Node next = k.next();
      System.out.println(next.getAttribute(ONode.node_attribute_name) + " " + k.getDepthOf(next));
      next.setAttribute("ui.class", "marked");
      sleep();
    }
  }

  /** Sleep. */
  public static void sleep() {
    try {
      Thread.sleep(1000);
    } catch (Exception e) {
    }
  }
}
