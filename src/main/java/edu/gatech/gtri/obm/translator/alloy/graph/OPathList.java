package edu.gatech.gtri.obm.translator.alloy.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OPathList extends ArrayList {

  public OPathList() {
    super();
  }

  public OPathList(IObject _o) {
    super();
    this.add(_o);
  }

  public String plus(String prefix) {
    return prefix.length() == 0 ? "" : "+";
  }

  public List<String> toListOfString() {
    System.out.println("happensBefore...");
    List<String> ss = new ArrayList<String>();
    for (int i = 0; i < this.size(); i++) {
      IObject o = (IObject) this.get(i);
      List<String> os = o.toStringAlloy();
      for (Iterator<String> iter = os.iterator(); iter.hasNext();)
        ss.add(iter.next());
    }
    return ss;
  }

  public List<String> toListOfStringx() {
    System.out.println("happensBefore...");
    List<String> ss = new ArrayList<String>();
    for (Iterator<IObject> iter = this.iterator(); iter.hasNext();) {
      IObject o = iter.next();

      if (o instanceof OListOR) {
        String s0 = ((OListOR) o).get(0).toString();
        for (int i = 0; i < ((OListOR) o).size(); i++) {
          s0 += "+" + ((OListOR) o).get(i).toString();
        }
        List<String> newss = new ArrayList<String>();
        for (int i = 0; i < ss.size(); i++) {
          newss.add(ss.get(i) + s0);
        }
        return newss;

      } else if (o instanceof ONode) {
        List<String> newss = new ArrayList<String>();
        ss.add(null);
        for (int i = 0; i < ss.size(); i++) {
          newss.add(ss.get(i) + plus(ss.get(i)) + ((ONode) o).getName());
        }
        ss = newss;

      } else if (o instanceof OListAND) {
        List<String> newss = new ArrayList<String>();
        for (int i = 0; i < ((OListAND) o).size(); i++) {
          for (int j = 0; j < ss.size(); j++) {
            newss.add(ss.get(j) + plus(ss.get(j)) + ((OListAND) o).get(i));
          }
        }
        ss = newss;
      } else if (o instanceof OPathList) {
        for (int i = 0; i < ((OPathList) o).size(); i++) {
          ss.add(((OPathList) o).get(i).toString());
        }
      }

    }
    return ss;

  }

  // public String toString() {
  // List<String> ss = toListOfString();
  // String s = "";
  // for (int i = 0; i < ss.size(); i++)
  // s += ss.get(i) + "\n";
  // return s;
  // }

  public void print() {
    List<String> ss = toListOfString();
    String s = "";
    for (int i = 0; i < ss.size(); i++)
      s += "[" + i + "] " + ss.get(i) + "\n";
    System.out.println(s);
  }



}
