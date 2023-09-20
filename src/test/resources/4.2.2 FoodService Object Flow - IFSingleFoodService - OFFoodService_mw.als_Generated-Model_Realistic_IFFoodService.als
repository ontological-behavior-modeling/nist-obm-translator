// This file is created with code.

module IFFoodService
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig FoodService extends Occurrence { eat: set Eat, order: set Order, pay: set Pay, prepare: set Prepare, serve: set Serve }
sig Prepare extends Occurrence {}
sig Order extends Occurrence {}
sig Serve extends Occurrence {}
sig Eat extends Occurrence {}
sig Pay extends Occurrence {}
sig IFFoodService extends FoodService { disj transferbeforePrepareServe, transferbeforeServeEat, transferbeforeOrderServe: set TransferBefore }
sig IFPrepare extends Prepare { preparedFoodItem: set FoodItem }
sig FoodItem extends Occurrence {}
sig IFServe extends Serve { servedFoodItem: set FoodItem }
sig IFEat extends Eat { eatenItem: set FoodItem }
sig IFPay extends Pay { paidFoodItem: set FoodItem, paidAmount: set Real }
sig Real extends Occurrence {}
sig IFOrder extends Order { orderedFoodItem: set FoodItem }

// Facts:
fact {all x: IFPrepare | #(x.preparedFoodItem) = 1}
fact {all x: IFServe | #(x.servedFoodItem) = 1}
fact {all x: IFOrder | #(x.orderedFoodItem) = 1}
fact {all x: IFPay | #(x.paidAmount) = 1}
fact {all x: IFPay | #(x.paidFoodItem) = 1}
fact {all x: IFEat | #(x.eatenItem) = 1}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.prepare, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.order, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.serve, x.eat]}
fact {all x: IFFoodService | bijectionFiltered[happensBefore, x.prepare, x.serve]}
fact {all x: IFFoodService | bijectionFiltered[happensBefore, x.order, x.serve]}
fact {all x: IFFoodService | bijectionFiltered[happensBefore, x.serve, x.eat]}
fact {all x: IFFoodService | bijectionFiltered[sources, x.transferbeforePrepareServe, x.prepare]}
fact {all x: IFFoodService | bijectionFiltered[targets, x.transferbeforePrepareServe, x.serve]}
fact {all x: IFFoodService | subsettingItemRuleForSources[x.transferbeforePrepareServe]}
fact {all x: IFFoodService | subsettingItemRuleForTargets[x.transferbeforePrepareServe]}
fact {all x: IFFoodService | bijectionFiltered[sources, x.transferbeforeServeEat, x.serve]}
fact {all x: IFFoodService | bijectionFiltered[targets, x.transferbeforeServeEat, x.eat]}
fact {all x: IFFoodService | subsettingItemRuleForSources[x.transferbeforeServeEat]}
fact {all x: IFFoodService | subsettingItemRuleForTargets[x.transferbeforeServeEat]}
fact {all x: IFFoodService | bijectionFiltered[sources, x.transferbeforeOrderServe, x.order]}
fact {all x: IFFoodService | bijectionFiltered[targets, x.transferbeforeOrderServe, x.serve]}
fact {all x: IFFoodService | subsettingItemRuleForSources[x.transferbeforeOrderServe]}
fact {all x: IFFoodService | subsettingItemRuleForTargets[x.transferbeforeOrderServe]}
fact {all x: FoodService | x.eat + x.order + x.pay + x.prepare + x.serve in x.steps}
fact {all x: FoodService | x.steps in x.eat + x.order + x.pay + x.prepare + x.serve}
fact {all x: IFPay | x.paidAmount + x.paidFoodItem in x.steps}
fact {all x: IFPay | x.steps in x.paidAmount + x.paidFoodItem}
fact {all x: IFFoodService | x.transferbeforeOrderServe + x.transferbeforePrepareServe + x.transferbeforeServeEat in x.steps}
fact {all x: IFFoodService | x.steps in x.transferbeforeOrderServe + x.transferbeforePrepareServe + x.transferbeforeServeEat}

