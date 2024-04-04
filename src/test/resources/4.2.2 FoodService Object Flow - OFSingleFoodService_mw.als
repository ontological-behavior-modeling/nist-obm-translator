// This file is created with code.

module OFSingleFoodServiceModule
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:

sig Eat extends Occurrence {}

sig FoodItem extends Occurrence {}
fact {all x: FoodItem | no (x.steps)}
fact {all x: FoodItem | no (x.inputs)}
fact {all x: FoodItem | no (x.outputs)}
fact {all x: FoodItem | no (steps.x)}

sig FoodService extends Occurrence {
 eat: set Eat,
 order: set Order,
 pay: set Pay,
 prepare: set Prepare,
 serve: set Serve
}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.prepare, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.order, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.serve, x.eat]}
fact {all x: FoodService | x.eat + x.order + x.pay + x.prepare + x.serve in x.steps}

sig Location extends Occurrence {}
fact {all x: Location | no (x.steps)}
fact {all x: Location | no (x.inputs)}
fact {all x: Location | no (x.outputs)}
fact {all x: Location | no (steps.x)}

sig OFCustomOrder extends OFOrder {
 orderDestination: set Location,
 orderAmount: set Real
}
fact {all x: OFCustomOrder | #(x.orderAmount) = 1}
fact {all x: OFCustomOrder | #(x.orderDestination) = 1}
fact {all x: OFCustomOrder | no (x.steps)}
fact {all x: OFCustomOrder | no (items.x)}
fact {all x: OFCustomOrder | no (inputs.x)}
fact {all x: OFCustomOrder | no (x.inputs)}
fact {all x: OFCustomOrder | x.orderAmount + x.orderDestination + x.orderedFoodItem in x.outputs}
fact {all x: OFCustomOrder | x.outputs in x.orderAmount + x.orderDestination + x.orderedFoodItem}
fact {all x: OFCustomOrder | no (outputs.x)}

sig OFCustomPrepare extends OFPrepare {
 prepareDestination: set Location
}
fact {all x: OFCustomPrepare | #(x.prepareDestination) = 1}
fact {all x: OFCustomPrepare | no (x.steps)}
fact {all x: OFCustomPrepare | no (items.x)}
fact {all x: OFCustomPrepare | x.prepareDestination + x.preparedFoodItem in x.inputs}
fact {all x: OFCustomPrepare | x.inputs in x.prepareDestination + x.preparedFoodItem}
fact {all x: OFCustomPrepare | no (inputs.x)}
fact {all x: OFCustomPrepare | x.prepareDestination + x.preparedFoodItem in x.outputs}
fact {all x: OFCustomPrepare | x.outputs in x.prepareDestination + x.preparedFoodItem}
fact {all x: OFCustomPrepare | no (outputs.x)}

sig OFCustomServe extends OFServe {
 serviceDestination: set Location
}
fact {all x: OFCustomServe | #(x.serviceDestination) = 1}
fact {all x: OFCustomServe | no (x.steps)}
fact {all x: OFCustomServe | no (items.x)}
fact {all x: OFCustomServe | x.servedFoodItem + x.serviceDestination in x.inputs}
fact {all x: OFCustomServe | x.inputs in x.servedFoodItem + x.serviceDestination}
fact {all x: OFCustomServe | no (inputs.x)}
fact {all x: OFCustomServe | x.servedFoodItem in x.outputs}
fact {all x: OFCustomServe | x.outputs in x.servedFoodItem}
fact {all x: OFCustomServe | no (outputs.x)}

sig OFEat extends Eat {
 eatenItem: set FoodItem
}
fact {all x: OFEat | #(x.eatenItem) = 1}
fact {all x: OFEat | no (x.steps)}
fact {all x: OFEat | no (items.x)}
fact {all x: OFEat | x.eatenItem in x.inputs}
fact {all x: OFEat | x.inputs in x.eatenItem}
fact {all x: OFEat | no (inputs.x)}
fact {all x: OFEat | no (x.outputs)}
fact {all x: OFEat | no (outputs.x)}

sig OFFoodService extends FoodService {
 transferOrderServe: set Transfer
}
fact {all x: OFFoodService | x.prepare in OFPrepare}
fact {all x: OFFoodService | x.serve in OFServe}
fact {all x: OFFoodService | x.order in OFOrder}
fact {all x: OFFoodService | x.eat in OFEat}
fact {all x: OFFoodService | x.pay in OFPay}
fact {all x: OFFoodService | bijectionFiltered[sources, x.transferOrderServe, x.order]}
fact {all x: OFFoodService | bijectionFiltered[targets, x.transferOrderServe, x.serve]}
fact {all x: OFFoodService | subsettingItemRuleForSources[x.transferOrderServe]}
fact {all x: OFFoodService | subsettingItemRuleForTargets[x.transferOrderServe]}
fact {all x: OFFoodService | isAfterSource[x.transferOrderServe]}
fact {all x: OFFoodService | isBeforeTarget[x.transferOrderServe]}
fact {all x: OFFoodService | x.eat + x.order + x.pay + x.prepare + x.serve + x.transferOrderServe in x.steps}

sig OFOrder extends Order {
 orderedFoodItem: set FoodItem
}
fact {all x: OFOrder | #(x.orderedFoodItem) = 1}

sig OFPay extends Pay {
 paidFoodItem: set FoodItem,
 paidAmount: set Real
}
fact {all x: OFPay | #(x.paidFoodItem) = 1}
fact {all x: OFPay | #(x.paidAmount) = 1}
fact {all x: OFPay | no (x.steps)}
fact {all x: OFPay | no (items.x)}
fact {all x: OFPay | x.paidAmount + x.paidFoodItem in x.inputs}
fact {all x: OFPay | x.inputs in x.paidAmount + x.paidFoodItem}
fact {all x: OFPay | no (inputs.x)}
fact {all x: OFPay | x.paidFoodItem in x.outputs}
fact {all x: OFPay | x.outputs in x.paidFoodItem}
fact {all x: OFPay | no (outputs.x)}

sig OFPrepare extends Prepare {
 preparedFoodItem: set FoodItem
}
fact {all x: OFPrepare | #(x.preparedFoodItem) = 1}

sig OFServe extends Serve {
 servedFoodItem: set FoodItem
}
fact {all x: OFServe | #(x.servedFoodItem) = 1}

sig OFSingleFoodService extends OFFoodService {
 disj transferOrderPrepare, transferPrepareServe, transferOrderPay, transferPayEat, transferServeEat: set Transfer
}
fact {all x: OFSingleFoodService | #(x.order) = 1}
fact {all x: OFSingleFoodService | x.prepare in OFCustomPrepare}
fact {all x: OFSingleFoodService | x.order in OFCustomOrder}
fact {all x: OFSingleFoodService | x.serve in OFCustomServe}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.order, x.order.orderedFoodItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.order, x.order.orderDestination]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.order, x.order.orderAmount]}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.prepare, x.prepare.preparedFoodItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.prepare, x.prepare.prepareDestination]}
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferOrderPrepare, x.order]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferOrderPrepare, x.prepare]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferOrderPrepare]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferOrderPrepare]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferOrderPrepare]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferOrderPrepare]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.prepare, x.prepare.preparedFoodItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.prepare, x.prepare.prepareDestination]}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.serve, x.serve.servedFoodItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.serve, x.serve.serviceDestination]}
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferPrepareServe, x.prepare]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferPrepareServe, x.serve]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferPrepareServe]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferPrepareServe]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferPrepareServe]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferPrepareServe]}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.pay, x.pay.paidFoodItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.pay, x.pay.paidAmount]}
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferOrderPay, x.order]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferOrderPay, x.pay]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferOrderPay]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferOrderPay]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferOrderPay]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferOrderPay]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.pay, x.pay.paidFoodItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[inputs, x.eat, x.eat.eatenItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferPayEat, x.pay]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferPayEat, x.eat]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferPayEat]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferPayEat]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferPayEat]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferPayEat]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.serve, x.serve.servedFoodItem]}
fact {all x: OFSingleFoodService | bijectionFiltered[outputs, x.serve, x.serve.serviceDestination]}
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferServeEat, x.serve]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferServeEat, x.eat]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferServeEat]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferServeEat]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferServeEat]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferServeEat]}
fact {all x: OFSingleFoodService | bijectionFiltered[sources, x.transferOrderServe, x.order]}
fact {all x: OFSingleFoodService | bijectionFiltered[targets, x.transferOrderServe, x.serve]}
fact {all x: OFSingleFoodService | subsettingItemRuleForTargets[x.transferOrderServe]}
fact {all x: OFSingleFoodService | isBeforeTarget[x.transferOrderServe]}
fact {all x: OFSingleFoodService | isAfterSource[x.transferOrderServe]}
fact {all x: OFSingleFoodService | subsettingItemRuleForSources[x.transferOrderServe]}
fact {all x: OFSingleFoodService | x.eat + x.order + x.pay + x.prepare + x.serve + x.transferOrderPay + x.transferOrderPrepare + x.transferOrderServe + x.transferPayEat + x.transferPrepareServe + x.transferServeEat in x.steps}
fact {all x: OFSingleFoodService | x.steps in x.eat + x.order + x.pay + x.prepare + x.serve + x.transferOrderPay + x.transferOrderPrepare + x.transferOrderServe + x.transferPayEat + x.transferPrepareServe + x.transferServeEat}
fact {all x: OFSingleFoodService | no (items.x)}
fact {all x: OFSingleFoodService | no (inputs.x)}
fact {all x: OFSingleFoodService | no (x.inputs)}
fact {all x: OFSingleFoodService | no (x.outputs)}
fact {all x: OFSingleFoodService | no (outputs.x)}

sig Order extends Occurrence {}

sig Pay extends Occurrence {}

sig Prepare extends Occurrence {}

sig Real extends Occurrence {}
fact {all x: Real | no (x.steps)}
fact {all x: Real | no (x.inputs)}
fact {all x: Real | no (x.outputs)}
fact {all x: Real | no (steps.x)}

sig Serve extends Occurrence {}

