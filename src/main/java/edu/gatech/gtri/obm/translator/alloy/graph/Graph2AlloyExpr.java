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


public class Graph2AlloyExpr {

  public static Graph createGraph() {
    System.setProperty("org.graphstream.ui", "swing");

    Graph graph = new SingleGraph("test");
    graph.setStrict(true);
    graph.setAttribute("ui.stylesheet", styleSheet);
    return graph;
  }


  public static void main(String[] args) {

    Graph graph = createGraph();

    Node p0Node = GraphUtil.ex1(graph);
    // Node p0Node = Examples.simplesequence(graph);// OPathList[0] = ONode(p1)
    // Node p0Node = Examples.fork(graph); // [OPathList[0] = OListAND[ONode(p1), ONode(p2)]
    // Node p1Node = Examples.decision(graph);// [OPathList[0] = OListOR[ONode(p1), ONode(p2)]
    // Node p0Node = Examples.forkjoin(graph);
    // Node p0Node = Examples.decisionmerge(graph);
    // Node p0Node = Examples.allcontrol(graph);
    // Node p0Node = Examples.loop(graph);

    display(graph);
    Set<Node> visited = new HashSet<>();
    Map<IObject, IObject> fn = new HashMap<>();
    start(new ONode(p0Node), fn, visited);
    print(fn);


    // Optional<Edge> oneofEdges = p0Node.leavingEdges().filter(e -> isOneOf(e)).findAny();
    // ONode o = new ONode(p0Node, oneofEdges.isPresent());
    // Map<IObject, IObject> map = happensBefore(o, oneofEdges.isPresent());
    // print(map);

    // exploreBreathFirst(p0Node);
    // exoloreDepthFirst(p0Node);
  }

  public static void start(IObject osource, Map<IObject, IObject> hb, Set<Node> _visited) {

    // for loop p1 source return OListOR with p2 because a edge is oneof...
    IObject fn = function(osource, _visited);
    if (fn == null)
      return;
    hb.put(osource, fn);

    if (fn instanceof OListOR) {
      // Node n = findCommonChildNode((OListOR) fn);// find all OListOR node are connecting to
      for (int i = 0; i < ((OListOR) fn).size(); i++) {
        IObject io = (IObject) ((OListOR) fn).get(i);
        if (io instanceof ONode) {
          // TODO this between shared node will be ignored for now
          // Node sharedNode = findCommonChildNode(((OListOR) fn));
          // System.out.println("sharedNode by " + fn + " is " + sharedNode); // for loop p2
          // sharedNode
          // returns p2 but below
          // next returns (p2+p3)
          // if (sharedNode != null) {
          // hb.put(fn, new ONode(sharedNode));
          // }
          ONode next_source = (ONode) ((OListOR) fn).get(i);
          IObject next = function(next_source, _visited);
          if (next != null) {
            hb.put(fn, next);
            start(next, hb, _visited);
            break; // TODO assume all nodes in OListOR are entering to next node for now
          }
        } else
          System.out.println(io);
      }

    } else if (fn instanceof OListAND) {
      for (int i = 0; i < ((OListAND) fn).size(); i++) {
        ONode next_source = (ONode) ((OListAND) fn).get(i);
        IObject next = function(next_source, _visited);
        if (next != null) {
          hb.put(next_source, next);
          start(next, hb, _visited);
        }
      }
    } else { // ONode
      ONode next_source = (ONode) fn;
      IObject next = function(next_source, _visited);
      if (next != null) {
        hb.put(next_source, next);
        start(next, hb, _visited);
      }
    }

  }

  // wip
  public static Node findCommonChildNode(OListOR or) {
    Map<Integer, Node> map = new HashMap<>();

    for (int i = 0; i < or.size(); i++) {
      Node sharedNode = findMultipleIncomingEdges(((ONode) or.get(i)).getNode());
      // List<Node> zz = findAllMultipleIncomingEdges(((ONode) or.get(i)).getNode()); //loop p2
      // return p2 only
      System.out.println("sharedNode" + sharedNode);
      map.put(i, sharedNode);
    }
    Node n = map.get(0);
    int counter = 1;
    while (n == map.get(counter++)) {
    }
    if (counter == map.size() + 1)
      return n;
    else
      return null;

  }

  public static List<Node> findAllMultipleIncomingEdges(Node source) {
    List<Node> nodes = new ArrayList<>();
    Iterator<? extends Node> k = source.getBreadthFirstIterator();
    while (k.hasNext()) {
      Node next = k.next();
      if (next.getInDegree() > 1)
        nodes.add(next);
    }
    return nodes;
  }

  public static Node findMultipleIncomingEdges(Node source) {
    Iterator<? extends Node> k = source.getBreadthFirstIterator();
    while (k.hasNext()) {
      Node next = k.next();
      if (next.getInDegree() > 1)
        return next;
    }
    return null;
  }

  public static int getOutDegreeMinusSelfLoop(Node node) {
    int numOfSelfLoop = 0;
    Iterator<Edge> iter = node.leavingEdges().iterator();
    while (iter.hasNext()) {
      if (iter.next().getTargetNode() == node)
        numOfSelfLoop++;
    }
    return node.getOutDegree() - numOfSelfLoop;
  }

  public static IObject function(IObject source, Set<Node> _visited) {


    if (source instanceof ONode) {

      Node sourceNode = ((ONode) source).getNode();

      System.out.println(((ONode) source).getName() + " visiting....");

      if (_visited.contains(sourceNode)) {
        System.out.println(sourceNode.getAttribute(ONode.node_attribute_name) + " already visited");
        return null;
      } else
        _visited.add(sourceNode);
      Optional<Edge> oneofEdges = sourceNode.leavingEdges().filter(e -> isOneOf(e)).findAny();
      if (oneofEdges.isPresent()) {
        OListOR or = new OListOR();
        sourceNode.leavingEdges().forEach(e -> {
          Node targetNode = e.getTargetNode();
          if (targetNode.getOutDegree() <= 1 || getOutDegreeMinusSelfLoop(targetNode) <= 1)
            or.add(new ONode(targetNode));
          else {
            System.out.println(targetNode); // p1start
            IObject zz = function(new ONode(targetNode), _visited);
            // System.out.println(zz); //OListAND
            if (zz != null)
              or.add(zz);
          }
        });
        return or;
      } else if (sourceNode.leavingEdges().count() == 1) {
        return new ONode(sourceNode.leavingEdges().iterator().next().getTargetNode());
      } else if (sourceNode.leavingEdges().count() > 0) {
        OListAND and = new OListAND();
        sourceNode.leavingEdges().forEach(e -> {
          Node targetNode = e.getTargetNode();
          if (targetNode.getOutDegree() <= 1 || getOutDegreeMinusSelfLoop(targetNode) <= 1)
            and.add(new ONode(targetNode));
          else {
            // System.out.println(targetNode); // p3start
            IObject zz = function(new ONode(targetNode), _visited);
            // System.out.println(zz); // OListOR
            if (zz != null)
              and.add(zz);
          }
        });
        return and;
      } else
        return null;
    }
    return null;
  }



  public static List<String> getFnString(Map<IObject, IObject> _map) {
    List<String> hbFunctionFilter = new ArrayList<>();
    for (Iterator<IObject> iter = _map.keySet().iterator(); iter.hasNext();) {
      IObject key = iter.next();
      // System.out.println(
      // "FunctionFiltered: " + key.toStringAlloy() + " -> " + _map.get(key).toStringAlloy());
      for (Iterator<String> iter1 = key.toStringAlloy().iterator(); iter1.hasNext();) {
        String keyString = iter1.next();
        for (Iterator<String> iter2 = _map.get(key).toStringAlloy().iterator(); iter2.hasNext();)
          hbFunctionFilter.add(keyString + ", " + iter2.next());
      }
    }
    return hbFunctionFilter;
  }

  public static void print(Map<IObject, IObject> _map) {
    List<String> hbFunctionFilter = getFnString(_map);
    for (int i = 0; i < hbFunctionFilter.size(); i++) {
      System.out.println(i + ": " + hbFunctionFilter.get(i));
    }
  }


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
          } else
            return new ONode(target, oneof);
        } else { // join
          return new ONode(target, oneof);
        }
      }
    } else if (target.leavingEdges().count() > 1) {
      OListAND listAnd = new OListAND();
      for (Iterator<Edge> iter = target.leavingEdges().iterator(); iter.hasNext();) {
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

  public static boolean isOneOf(Edge edge) {
    if (edge.getAttribute("oneof") == null)
      return false;
    return (Boolean) edge.getAttribute("oneof");
  }

  public static Node getNodeByName(Graph graph, String _nodeName) {
    for (Node node : graph) {
      if (node.getAttribute(ONode.node_attribute_name).equals(_nodeName))
        return node;
    }
    return null;
  }


  protected static String styleSheet =
      "node {" + "   fill-color: black; text-color: black; text-alignment:above; text-size:20;"
          + "}" + "node.marked {" + "   fill-color: red;" + "} edge { fill-color: black;} ";

  public static void display(Graph graph) {
    graph.setAttribute("ui.stylesheet", styleSheet);
    for (Node node : graph)
      node.setAttribute("ui.label", node.getAttribute(ONode.node_attribute_name));
    for (int i = 0; i < graph.getEdgeCount(); i++)
      graph.getEdge(i).setAttribute("ui.label",
          graph.getEdge(i).getAttribute("oneof") != null ? "oneof" : "");
    graph.display();
  }

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

  public static void exoloreDepthFirst(Node source) {
    DepthFirstIterator k = new DepthFirstIterator(source);

    while (k.hasNext()) {
      Node next = k.next();
      System.out.println(next.getAttribute(ONode.node_attribute_name) + " " + k.getDepthOf(next));
      next.setAttribute("ui.class", "marked");
      sleep();
    }
  }

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

  public static void sleep() {
    try {
      Thread.sleep(1000);
    } catch (Exception e) {
    }
  }

}
