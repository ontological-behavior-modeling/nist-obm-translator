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
