package edu.gatech.gtri.obm.translator.alloy.tofile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;

public class AlloyModule {

  private final String moduleName;
  private Expr facts;
  private final List<Sig> signatures;
  private final Command[] commands;

  public AlloyModule(String moduleName, List<Sig> signatures, Expr facts, Command[] commands) {
    this.moduleName = moduleName;

    // if (facts instanceof ExprList && ((ExprList) facts).args.size() > 0) {
    // ExprList lfacts = (ExprList) facts;
    // Map<String, Expr> map = new HashMap<>();
    // List<String> keys = new ArrayList<>();
    // for (int i = 0; i < lfacts.args.size(); i++) {
    // map.put(lfacts.args.get(i).toString(), lfacts.args.get(i));
    // keys.add(lfacts.args.get(i).toString());
    // }
    // System.out.println(keys);
    // Collections.sort(keys);
    // System.out.println(keys);
    //
    // this.facts = null; // reset
    // this.facts = map.get(keys.get(0));
    // for (int i = 1; i < keys.size(); i++) {
    // System.out.println(keys.get(i));
    // this.facts = this.facts.and(map.get(keys.get(i)));
    // }
    // } else
    this.facts = facts;
    this.signatures = signatures;
    this.commands = commands;
  }

  public String getModuleName() {
    return moduleName;
  }

  public Expr getFacts() {
    return facts;
  }

  public List<Sig> getSignatures() {
    return new ArrayList<>(signatures);
  }

  public Command[] getCommands() {
    return Arrays.copyOf(commands, commands.length);
  }
}
