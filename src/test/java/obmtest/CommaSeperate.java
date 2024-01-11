package obmtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommaSeperate {

  static String expr =
      "[(all x | no x . o/inputs), (all x | no x . o/outputs), (all x | no x . o/steps), (all x | x . (this/Supplier <: suppliedProduct) in x . o/outputs), (all x | # x . (this/Supplier <: suppliedProduct) = 1), (all x | no x . o/steps), (all x | no x . o/inputs), (all x | x . o/outputs in x . (this/Supplier <: suppliedProduct)), (all x | x . (this/Customer <: receivedProduct) in x . o/inputs), (all x | # x . (this/Customer <: receivedProduct) = 1), (all x | no x . o/steps), (all x | x . o/inputs in x . (this/Customer <: receivedProduct)), (all x | no x . o/outputs), (all x | x . (this/TransferProduct <: customer) + x . (this/TransferProduct <: supplier) + x . (this/TransferProduct <: transferSupplierCustomer) in x . o/steps), (all x | # x . (this/TransferProduct <: supplier) = 1), (all x | # x . (this/TransferProduct <: customer) = 1), (all x | o/bijectionFiltered[o/sources, x . (this/TransferProduct <: transferSupplierCustomer), x . (this/TransferProduct <: supplier)]), (all x | o/bijectionFiltered[o/targets, x . (this/TransferProduct <: transferSupplierCustomer), x . (this/TransferProduct <: customer)]), (all x | o/subsettingItemRuleForSources[x . (this/TransferProduct <: transferSupplierCustomer)]), (all x | o/subsettingItemRuleForTargets[x . (this/TransferProduct <: transferSupplierCustomer)]), (all x | no x . o/inputs), (all x | no x . o/outputs), (all x | x . o/steps in x . (this/TransferProduct <: customer) + x . (this/TransferProduct <: supplier) + x . (this/TransferProduct <: transferSupplierCustomer)), (all t | AND[o/isAfterSource[t], o/isBeforeTarget[t]] <=> t in o/TransferBefore), (all t | # t . o/items > 0), r/acyclic[o/items, o/Transfer], r/acyclic[o/sources, o/Transfer], r/acyclic[o/targets, o/Transfer], (all t | (all itm | o/during[t, itm])), (all t,t' | AND[t . o/items = t' . o/items, t . o/sources = t' . o/sources, t . o/targets = t' . o/targets] => t = t'), (all t | (all src | (all tgt | OR[t in src . o/stepsAndSubsteps, t in tgt . o/stepsAndSubsteps => t !in o/TransferBefore]))), (all x,y | ! OR[o/before[x, y], o/before[y, x], o/during[x, y], o/during[y, x]] <=> o/overlap[x, y]), (all o | (all input | o/during[o, input])), (all o | (all output | o/during[o, output])), (all x,y | y in x . o/steps => ! x in y . o/steps), (all x,y,z | AND[y in x . o/steps, z in y . ^ o/steps] => z !in x . o/steps), (all x | # x . ~ o/steps <= 1), (all x | o/during[x, x]), (all x,y,z | AND[o/before[x, y], o/before[y, z]] => o/before[x, z]), (all x | ! o/before[x, x]), (all x,y,z | AND[o/during[x, y], o/during[y, z]] => o/during[x, z]), (all x,y | AND[o/before[x, y], o/before[y, x]] <=> AND[o/before[x, x], o/before[y, y], o/during[x, y], o/during[y, x]]), (all x,y,z | AND[o/before[x, y], o/during[z, y]] => o/before[x, z]), (all x,y,z | AND[o/before[y, x], o/during[z, y]] => o/before[z, x]), (all x,y | y in x . o/steps => o/during[y, x])]";


  // static String expr =
  // "AND[(all x | # x . (this/TransferProduct <: supplier) = 1), (all x | # x .
  // (this/TransferProduct <: customer) = 1), (all x | # x . (this/Supplier <: suppliedProduct) =
  // 1), (all x | # x . (this/Customer <: receivedProduct) = 1), (all x | x . (this/Supplier <:
  // suppliedProduct) in x . o/outputs), (all x | x . o/outputs in x . (this/Supplier <:
  // suppliedProduct)), (all x | x . (this/Customer <: receivedProduct) in x . o/inputs), (all x | x
  // . o/inputs in x . (this/Customer <: receivedProduct)), (all x | o/bijectionFiltered[o/sources,
  // x . (this/TransferProduct <: transferSupplierCustomer), x . (this/TransferProduct <:
  // supplier)]), (all x | o/bijectionFiltered[o/targets, x . (this/TransferProduct <:
  // transferSupplierCustomer), x . (this/TransferProduct <: customer)]), (all x |
  // o/subsettingItemRuleForSources[x . (this/TransferProduct <: transferSupplierCustomer)]), (all x
  // | o/subsettingItemRuleForTargets[x . (this/TransferProduct <: transferSupplierCustomer)]), (all
  // x | no x . o/steps), (all x | no x . o/steps), (all x | no x . o/steps), (all x | x .
  // (this/TransferProduct <: customer) + x . (this/TransferProduct <: supplier) + x .
  // (this/TransferProduct <: transferSupplierCustomer) in x . o/steps), (all x | x . o/steps in x .
  // (this/TransferProduct <: customer) + x . (this/TransferProduct <: supplier) + x .
  // (this/TransferProduct <: transferSupplierCustomer)), (all x | (no y | y in x . o/steps)), (all
  // x | no x . o/outputs), (all x | no x . o/inputs), (all x | no x . o/outputs), (all x | no x .
  // o/inputs), (all x | no x . o/inputs), (all x | no x . o/outputs), (all t |
  // AND[o/isAfterSource[t], o/isBeforeTarget[t]] <=> t in o/TransferBefore), (all t | # t . o/items
  // > 0), r/acyclic[o/items, o/Transfer], r/acyclic[o/sources, o/Transfer], r/acyclic[o/targets,
  // o/Transfer], (all t | (all itm | o/during[t, itm])), (all t,t' | AND[t . o/items = t' .
  // o/items, t . o/sources = t' . o/sources, t . o/targets = t' . o/targets] => t = t'), (all t |
  // (all src | (all tgt | OR[t in src . o/stepsAndSubsteps, t in tgt . o/stepsAndSubsteps => t !in
  // o/TransferBefore]))), (all x,y | ! OR[o/before[x, y], o/before[y, x], o/during[x, y],
  // o/during[y, x]] <=> o/overlap[x, y]), (all o | (all input | o/during[o, input])), (all o | (all
  // output | o/during[o, output])), (all x,y | y in x . o/steps => ! x in y . o/steps), (all x,y,z
  // | AND[y in x . o/steps, z in y . ^ o/steps] => z !in x . o/steps), (all x | # x . ~ o/steps <=
  // 1), (all x | o/during[x, x]), (all x,y,z | AND[o/before[x, y], o/before[y, z]] => o/before[x,
  // z]), (all x | ! o/before[x, x]), (all x,y,z | AND[o/during[x, y], o/during[y, z]] =>
  // o/during[x, z]), (all x,y | AND[o/before[x, y], o/before[y, x]] <=> AND[o/before[x, x],
  // o/before[y, y], o/during[x, y], o/during[y, x]]), (all x,y,z | AND[o/before[x, y], o/during[z,
  // y]] => o/before[x, z]), (all x,y,z | AND[o/before[y, x], o/during[z, y]] => o/before[z, x]),
  // (all x,y | y in x . o/steps => o/during[y, x])]";

  public static void main(String[] args) {
    String[] splits = expr.split("\\),");

    List<String> liststrings = new ArrayList<String>(Arrays.asList(splits));
    // Collections.sort(liststrings);
    for (String s : liststrings) {
      System.out.println(s);
    }

  }

}
