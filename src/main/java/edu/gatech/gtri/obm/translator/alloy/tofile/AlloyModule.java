package edu.gatech.gtri.obm.translator.alloy.tofile;

import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class AlloyModule. */
public class AlloyModule {

  /** The module name. */
  private final String moduleName;

  /** The facts. */
  private Expr facts;

  /** The signatures. */
  private final List<Sig> signatures;

  /** The commands. */
  private final Command[] commands;

  /**
   * Instantiates a new alloy module.
   *
   * @param moduleName the module name
   * @param signatures the signatures
   * @param facts the facts
   * @param commands the commands
   */
  public AlloyModule(String moduleName, List<Sig> signatures, Expr facts, Command[] commands) {
    this.moduleName = moduleName;
    this.facts = facts;
    this.signatures = signatures;
    this.commands = commands;
  }

  /**
   * Gets the module name.
   *
   * @return the module name
   */
  public String getModuleName() {
    return moduleName;
  }

  /**
   * Gets the facts.
   *
   * @return the facts
   */
  public Expr getFacts() {
    return facts;
  }

  /**
   * Gets the signatures.
   *
   * @return the signatures
   */
  public List<Sig> getSignatures() {
    return new ArrayList<>(signatures);
  }

  /**
   * Gets the commands.
   *
   * @return the commands
   */
  public Command[] getCommands() {
    return Arrays.copyOf(commands, commands.length);
  }
}
