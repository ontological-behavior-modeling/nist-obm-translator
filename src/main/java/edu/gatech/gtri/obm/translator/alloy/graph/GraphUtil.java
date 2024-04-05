package edu.gatech.gtri.obm.translator.alloy.graph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

// TODO: Auto-generated Javadoc
/** The Class GraphUtil. */
public class GraphUtil {

  /** The id counter. */
  public static long idCounter = 0;

  /**
   * Simplesequence.
   *
   * @param graph the graph
   * @return the node
   */
  public static Node simplesequence(Graph graph) {
    System.out.println("simplesequence...");
    Node p0Node = createNode(graph, "p0");
    String p0 = p0Node.getId();
    String p1 = createNode(graph, "p1").getId();
    Edge p0p1 = graph.addEdge("p0p1", p0, p1, true);
    return p0Node;
  }

  /**
   * Fork.
   *
   * @param graph the graph
   * @return the node
   */
  public static Node fork(Graph graph) {
    System.out.println("fork...");
    Node p0Node = createNode(graph, "p0");
    String p0 = p0Node.getId();
    Node p1Node = createNode(graph, "p1");
    String p1 = p1Node.getId();
    String p2 = createNode(graph, "p2").getId();
    Edge p0p1 = graph.addEdge("p0p1", p0, p1, true);
    Edge p0p2 = graph.addEdge("p0p2", p0, p2, true);
    return p1Node;
  }

  /**
   * Decision.
   *
   * @param graph the graph
   * @return the node
   */
  public static Node decision(Graph graph) {
    System.out.println("decision...");
    Node p0Node = createNode(graph, "p0");
    String p0 = p0Node.getId();
    String p1 = createNode(graph, "p1").getId();
    String p2 = createNode(graph, "p2").getId();
    Edge p0p1 = graph.addEdge("p0p1", p0, p1, true);
    Edge p0p2 = graph.addEdge("p0p2", p0, p2, true);
    p0p1.setAttribute("oneof", true);
    p0p2.setAttribute("oneof", true);
    return p0Node;
  }

  /**
   * Forkjoin.
   *
   * @param graph the graph
   * @return the node
   */
  public static Node forkjoin(Graph graph) {
    System.out.println("fork -> join...");
    Node p0Node = createNode(graph, "p0");
    String p0 = p0Node.getId();
    String p1 = createNode(graph, "p1").getId();
    String p2 = createNode(graph, "p2").getId();
    Edge p0p1 = graph.addEdge("p0p1", p0, p1, true);
    Edge p0p2 = graph.addEdge("p0p2", p0, p2, true);
    String p3 = createNode(graph, "p3").getId();
    Edge p1p3 = graph.addEdge("p1p3", p1, p3, true);
    Edge p2p3 = graph.addEdge("p2p3", p2, p3, true);
    return p0Node;
  }

  /**
   * Decisionmerge.
   *
   * @param graph the graph
   * @return the node
   */
  public static Node decisionmerge(Graph graph) {
    System.out.println("decision -> merge...");
    Node p0Node = createNode(graph, "p0");
    String p0 = p0Node.getId();
    String p1 = createNode(graph, "p1").getId();
    String p2 = createNode(graph, "p2").getId();
    Edge p0p1 = graph.addEdge("p0p1", p0, p1, true);
    Edge p0p2 = graph.addEdge("p0p2", p0, p2, true);
    p0p1.setAttribute("oneof", true);
    p0p2.setAttribute("oneof", true);
    String p3 = createNode(graph, "p3").getId();
    Edge p1p3 = graph.addEdge("p1p3", p1, p3, true);
    Edge p2p3 = graph.addEdge("p2p3", p2, p3, true);
    p1p3.setAttribute("oneof", true);
    p2p3.setAttribute("oneof", true);
    return p0Node;
  }

  /**
   * Allcontrol.
   *
   * @param graph the graph
   * @return the node
   */
  public static Node allcontrol(Graph graph) {
    System.out.println("fork -> join-> decision -> merge");
    Node p1Node = createNode(graph, "p1");
    String p1 = p1Node.getId();
    String p2 = createNode(graph, "p2").getId();
    String p3 = createNode(graph, "p3").getId();
    String p4 = createNode(graph, "p4").getId();
    String p5 = createNode(graph, "p5").getId();
    String p6 = createNode(graph, "p6").getId();
    String p7 = createNode(graph, "p7").getId();

    Edge p1p2 = graph.addEdge("p1p2", p1, p2, true);
    Edge p1p3 = graph.addEdge("p1p3", p1, p3, true);
    Edge p2p4 = graph.addEdge("p2p4", p2, p4, true);
    Edge p3p5 = graph.addEdge("p3p4", p3, p4, true);

    Edge p4p5 = graph.addEdge("p4p5", p4, p5, true);
    Edge p4p6 = graph.addEdge("p4p6", p4, p6, true);
    Edge p5p7 = graph.addEdge("p5p7", p5, p7, true);
    Edge p6p7 = graph.addEdge("p6p7", p6, p7, true);
    p4p5.setAttribute("oneof", true);
    p4p6.setAttribute("oneof", true);
    p5p7.setAttribute("oneof", true);
    p6p7.setAttribute("oneof", true);

    return p1Node;
  }

  /**
   * Loop.
   *
   * @param graph the graph
   * @return the node
   */
  public static Node loop(Graph graph) {
    System.out.println("loop");
    Node p1Node = createNode(graph, "p1");
    String p1 = p1Node.getId();
    String p2 = createNode(graph, "p2").getId();
    String p3 = createNode(graph, "p3").getId();

    Edge p1p2 = graph.addEdge("p1p2", p1, p2, true);
    Edge p2p3 = graph.addEdge("p2p3", p2, p3, true);
    Edge p2p2 = graph.addEdge("p2p2", p2, p2, true);
    p1p2.setAttribute("oneof", true);
    p2p3.setAttribute("oneof", true);
    p2p2.setAttribute("oneof", true);

    return p1Node;
  }

  /**
   * Ex 1.
   *
   * @param graph the graph
   * @return the node
   */
  public static Node ex1(Graph graph) {
    Node p0Node = createNode(graph, "p0");
    String p0 = p0Node.getId();

    String p1 = createNode(graph, "p1").getId();
    String p21start = createNode(graph, "p21start").getId();
    String p211 = createNode(graph, "p211").getId();
    String p212 = createNode(graph, "p212").getId();
    String p213 = createNode(graph, "p213").getId();
    String p21end = createNode(graph, "p21end").getId();
    String p22 = createNode(graph, "p22").getId();
    Node pfNode = createNode(graph, "pf");
    String pf = pfNode.getId();

    Edge p0p1 = graph.addEdge("p0p1", p0, p1, true);
    p0p1.setAttribute("oneof", true);
    Edge p1pf = graph.addEdge("p1pf", p1, pf, true);
    p1pf.setAttribute("oneof", true);

    Edge p0p21start = graph.addEdge("p0p21start", p0, p21start, true);
    p0p21start.setAttribute("oneof", true);
    Edge p21startp211 = graph.addEdge("p21startp211", p21start, p211, true);
    Edge p21startp212 = graph.addEdge("p21startp212", p21start, p212, true);
    Edge p21startp213 = graph.addEdge("p21startp213", p21start, p213, true);
    Edge p211p21end = graph.addEdge("p211p21end", p211, p21end, true);
    Edge p212p21end = graph.addEdge("p212p21end", p212, p21end, true);
    Edge p213p21end = graph.addEdge("p213p21end", p213, p21end, true);
    Edge p21endp2f = graph.addEdge("p21endp2f", p21end, pf, true);
    p21endp2f.setAttribute("oneof", true);

    Edge p0p22 = graph.addEdge("p0p22", p0, p22, true);
    p0p22.setAttribute("oneof", true);
    Edge p22pf = graph.addEdge("p22pf", p22, pf, true);
    p22pf.setAttribute("oneof", true);
    p21endp2f.setAttribute("oneof", true);

    String p231 = createNode(graph, "p231").getId();
    String p232 = createNode(graph, "p232").getId();
    String p233 = createNode(graph, "p233").getId();
    Edge p0p231 = graph.addEdge("p0p231", p0, p231, true);
    Edge p0p232 = graph.addEdge("p0p232", p0, p232, true);
    Edge p0p233 = graph.addEdge("p0p233", p0, p233, true);
    p0p231.setAttribute("oneof", true);
    p0p232.setAttribute("oneof", true);
    p0p233.setAttribute("oneof", true);
    Edge p231pf = graph.addEdge("p231pf", p231, pf, true);
    Edge p232pf = graph.addEdge("p232pf", p232, pf, true);
    Edge p233pf = graph.addEdge("p233pf", p233, pf, true);
    p231pf.setAttribute("oneof", true);
    p232pf.setAttribute("oneof", true);
    p233pf.setAttribute("oneof", true);

    String p3start = createNode(graph, "p3start").getId();
    Edge p0p3start = graph.addEdge("p0p3start", p0, p3start, true);
    p0p3start.setAttribute("oneof", true);
    String p31 = createNode(graph, "p31").getId();
    String p32 = createNode(graph, "p32").getId();
    String p3end = createNode(graph, "p3end").getId();

    String p33start = createNode(graph, "p33start").getId();
    String p331 = createNode(graph, "p331").getId();
    String p332 = createNode(graph, "p332").getId();
    String p333 = createNode(graph, "p333").getId();

    Edge p3startp31 = graph.addEdge("p3startp31", p3start, p31, true);
    Edge p3startp32 = graph.addEdge("p3startp32", p3start, p32, true);
    Edge p31p3end = graph.addEdge("p31p3end", p31, p3end, true);
    Edge p32p3end = graph.addEdge("p32p3end", p32, p3end, true);
    Edge p3endpf = graph.addEdge("p3endpf", p3end, pf, true);
    p3endpf.setAttribute("oneof", true);

    Edge p3startp33start = graph.addEdge("p3startp33start", p3start, p33start, true);

    Edge p33startp331 = graph.addEdge("p33startp331", p33start, p331, true);
    Edge p33startp332 = graph.addEdge("p33startp332", p33start, p332, true);
    Edge p33startp333 = graph.addEdge("p33startp333", p33start, p333, true);
    p33startp331.setAttribute("oneof", true);
    p33startp332.setAttribute("oneof", true);
    p33startp333.setAttribute("oneof", true);

    Edge p331pf = graph.addEdge("p331pf", p331, pf, true);
    Edge p332pf = graph.addEdge("p332pf", p332, pf, true);
    Edge p333pf = graph.addEdge("p333pf", p333, pf, true);
    p331pf.setAttribute("oneof", true);
    p332pf.setAttribute("oneof", true);
    p333pf.setAttribute("oneof", true);
    return pfNode;
  }

  /**
   * Creates the node.
   *
   * @param graph the graph
   * @param name the name
   * @return the node
   */
  public static Node createNode(Graph graph, String name) {
    String p1Id = createID();
    Node p1 = graph.addNode(p1Id);
    p1.setAttribute(ONode.node_attribute_name, name);
    p1.setAttribute(ONode.type, name.toUpperCase());
    return p1;
  }

  /**
   * Creates the node.
   *
   * @param graph the graph
   * @param name the name
   * @param type the type
   * @return the node
   */
  public static Node createNode(Graph graph, String name, String type) {
    String p1Id = createID();
    Node p1 = graph.addNode(p1Id);
    p1.setAttribute(ONode.node_attribute_name, name);
    p1.setAttribute(ONode.type, type);
    return p1;
  }

  /**
   * Creates the ID.
   *
   * @return the string
   */
  public static synchronized String createID() {
    return String.valueOf(idCounter++);
  }
}
