// This file is created with code.

module FoodServiceObjectControlFlowIFSingleFoodService_ExplicitFact
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig Order extends Occurrence {}
sig Prepare extends Occurrence {}
sig Serve extends Occurrence {}
sig Eat extends Occurrence {}
sig Pay extends Occurrence {}
sig FoodItem extends Occurrence {}
sig Location extends Occurrence {}
sig Real extends Occurrence {}
sig OFStart extends Occurrence {}
sig OFEnd extends Occurrence {}
sig OFOrder extends Order { orderedFoodItem: one FoodItem }
sig OFCustomOrder extends OFOrder { orderDestination: one Location orderAmount: one Real }
sig OFPrepare extends Prepare { preparedFoodItem: one FoodItem }
sig OFCustomPrepare extends OFPrepare { prepareDestination: one Location }
sig OFServe extends Serve { servedFoodItem: one FoodItem }
sig OFCustomServe extends OFServe { serviceDestination: one Location }
sig OFEat extends Eat { eatenItem: one FoodItem }
sig OFPay extends Pay { paidAmount: one Real paidFoodItem: one FoodItem }
sig FoodService extends Occurrence { order: set Order pay: set Pay eat: set Eat serve: set Serve prepare: set Prepare }
sig OFFoodService extends FoodService { disj transferPrepareServe, transferOrderServe, transferServeEat: set TransferBefore }
sig OFSingleFoodService extends OFFoodService { disj transferOrderPrepare, transferOrderPay, transferPayEat: set TransferBefore }
sig OFLoopFoodService extends OFFoodService { end: one OFEnd start: one OFStart disj transferOrderPrepare, transferOrderPay, transferPayEat: set TransferBefore }
sig OFParallelFoodService extends OFFoodService {}

// Facts:
fact f1 {all food_item: FoodItem | no (food_item.happensBefore) and no (happensBefore.food_item) and no (food_item.steps) and no (food_item.inputs) and no (food_item.outputs)}
fact f2 {all location: Location | no (location.happensBefore) and no (happensBefore.location) and no (location.steps) and no (location.inputs) and no (location.outputs)}
fact f3 {all real: Real | no (real.happensBefore) and no (happensBefore.real) and no (real.steps) and no (real.inputs) and no (real.outputs)}
fact f4 {all of_start: OFStart | no (inputs.of_start) and no (outputs.of_start) and no (items.of_start)}
fact f5 {all of_end: OFEnd | no (inputs.of_end) and no (outputs.of_end) and no (items.of_end)}
fact f6 {all of_order: OFOrder | no (of_order.inputs)}
fact f7 {all of_order: OFOrder | of_order.orderedFoodItem in of_order.outputs}
fact f8 {all of_custom_order: OFCustomOrder | of_custom_order.orderAmount in of_custom_order.outputs}
fact f9 {all of_custom_order: OFCustomOrder | of_custom_order.orderDestination in of_custom_order.outputs}
fact f10 {all of_prepare: OFPrepare | of_prepare.preparedFoodItem in of_prepare.inputs}
fact f11 {all of_prepare: OFPrepare | of_prepare.preparedFoodItem in of_prepare.outputs}
fact f12 {all of_custom_prepare: OFCustomPrepare | of_custom_prepare.prepareDestination in of_custom_prepare.inputs}
fact f13 {all of_custom_prepare: OFCustomPrepare | of_custom_prepare.prepareDestination in of_custom_prepare.outputs}
fact f14 {all of_serve: OFServe | of_serve.servedFoodItem in of_serve.inputs}
fact f15 {all of_serve: OFServe | of_serve.servedFoodItem in of_serve.outputs}
fact f16 {all of_custom_serve: OFCustomServe | of_custom_serve.serviceDestination in of_custom_serve.inputs}
fact f17 {all of_eat: OFEat | of_eat.eatenItem in of_eat.inputs}
fact f18 {all of_eat: OFEat | no (of_eat.outputs)}
fact f19 {all of_pay: OFPay | of_pay.paidAmount in of_pay.inputs}
fact f20 {all of_pay: OFPay | of_pay.paidFoodItem in of_pay.inputs}
fact f21 {all of_pay: OFPay | of_pay.paidFoodItem in of_pay.outputs}
fact f22 {all food_service: FoodService | bijectionFiltered[happensBefore, food_service.order, food_service.serve]}
fact f23 {all food_service: FoodService | bijectionFiltered[happensBefore, food_service.prepare, food_service.serve]}
fact f24 {all food_service: FoodService | bijectionFiltered[happensBefore, food_service.serve, food_service.eat]}
fact f25 {all food_service: FoodService | food_service.order + food_service.prepare + food_service.pay + food_service.eat + food_service.serve in food_service.steps}
fact f26 {all of_food_service: OFFoodService | no (of_food_service.inputs) and no (inputs.of_food_service)}
fact f27 {all of_food_service: OFFoodService | no (of_food_service.outputs) and no (outputs.of_food_service)}
fact f28 {all of_food_service: OFFoodService | of_food_service.order in OFOrder}
fact f29 {all of_food_service: OFFoodService | of_food_service.prepare in OFPrepare}
fact f30 {all of_food_service: OFFoodService | of_food_service.pay in OFPay}
fact f31 {all of_food_service: OFFoodService | of_food_service.eat in OFEat}
fact f32 {all of_food_service: OFFoodService | of_food_service.serve in OFServe}
fact f33 {all of_food_service: OFFoodService | of_food_service.transferPrepareServe + of_food_service.transferOrderServe + of_food_service.transferServeEat in of_food_service.steps}
fact f34 {all of_food_service: OFFoodService | bijectionFiltered[sources, of_food_service.transferPrepareServe, of_food_service.prepare]}
fact f35 {all of_food_service: OFFoodService | bijectionFiltered[targets, of_food_service.transferPrepareServe, of_food_service.serve]}
fact f36 {all of_food_service: OFFoodService | subsettingItemRuleForSources[of_food_service.transferPrepareServe]}
fact f37 {all of_food_service: OFFoodService | subsettingItemRuleForTargets[of_food_service.transferPrepareServe]}
fact f38 {all of_food_service: OFFoodService | bijectionFiltered[sources, of_food_service.transferServeEat, of_food_service.serve]}
fact f39 {all of_food_service: OFFoodService | bijectionFiltered[targets, of_food_service.transferServeEat, of_food_service.eat]}
fact f40 {all of_food_service: OFFoodService | subsettingItemRuleForSources[of_food_service.transferServeEat]}
fact f41 {all of_food_service: OFFoodService | subsettingItemRuleForTargets[of_food_service.transferServeEat]}
fact f42 {all of_food_service: OFFoodService | bijectionFiltered[sources, of_food_service.transferOrderServe, of_food_service.order]}
fact f43 {all of_food_service: OFFoodService | bijectionFiltered[targets, of_food_service.transferOrderServe, of_food_service.serve]}
fact f44 {all of_food_service: OFFoodService | subsettingItemRuleForSources[of_food_service.transferOrderServe]}
fact f45 {all of_food_service: OFFoodService | subsettingItemRuleForTargets[of_food_service.transferOrderServe]}
fact f46 {all of_single_food_service: OFSingleFoodService | of_single_food_service.order in OFCustomOrder}
fact f47 {all of_single_food_service: OFSingleFoodService | of_single_food_service.prepare in OFCustomPrepare}
fact f48 {all of_single_food_service: OFSingleFoodService | of_single_food_service.serve in OFCustomServe}
fact f49 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderPrepare + of_single_food_service.transferOrderPay + of_single_food_service.transferPayEat in of_single_food_service.steps}
fact f50 {all of_single_food_service: OFSingleFoodService | of_single_food_service.steps in of_single_food_service.order + of_single_food_service.prepare + of_single_food_service.pay + of_single_food_service.serve + of_single_food_service.eat + of_single_food_service.transferPrepareServe + of_single_food_service.transferOrderServe + of_single_food_service.transferServeEat + of_single_food_service.transferOrderPrepare + of_single_food_service.transferOrderPay + of_single_food_service.transferPayEat}
fact f51 {all of_single_food_service: OFSingleFoodService | #(of_single_food_service.order) = 1}
fact f52 {all of_single_food_service: OFSingleFoodService | of_single_food_service.order.outputs in of_single_food_service.order.orderedFoodItem + of_single_food_service.order.orderAmount + of_single_food_service.order.orderDestination}
fact f53 {all of_single_food_service: OFSingleFoodService | of_single_food_service.pay.inputs + of_single_food_service.pay.paidAmount in of_single_food_service.pay.paidFoodItem}
fact f54 {all of_single_food_service: OFSingleFoodService | of_single_food_service.pay.outputs in of_single_food_service.pay.paidFoodItem}
fact f55 {all of_single_food_service: OFSingleFoodService | of_single_food_service.prepare.inputs in of_single_food_service.prepare.preparedFoodItem + of_single_food_service.prepare.prepareDestination}
fact f56 {all of_single_food_service: OFSingleFoodService | of_single_food_service.prepare.outputs in of_single_food_service.prepare.preparedFoodItem + of_single_food_service.prepare.prepareDestination}
fact f57 {all of_single_food_service: OFSingleFoodService | of_single_food_service.serve.inputs in of_single_food_service.serve.servedFoodItem + of_single_food_service.serve.serviceDestination}
fact f58 {all of_single_food_service: OFSingleFoodService | of_single_food_service.serve.outputs in of_single_food_service.serve.servedFoodItem + of_single_food_service.serve.serviceDestination}
fact f59 {all of_single_food_service: OFSingleFoodService | of_single_food_service.eat.inputs in of_single_food_service.eat.eatenItem}
fact f60 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[sources, of_single_food_service.transferOrderPay, of_single_food_service.order]}
fact f61 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[targets, of_single_food_service.transferOrderPay, of_single_food_service.pay]}
fact f62 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForSources[of_single_food_service.transferOrderPay]}
fact f63 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForTargets[of_single_food_service.transferOrderPay]}
fact f64 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderPay.items in of_single_food_service.transferOrderPay.sources.orderedFoodItem + of_single_food_service.transferOrderPay.sources.orderAmount}
fact f65 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderPay.sources.orderedFoodItem + of_single_food_service.transferOrderPay.sources.orderAmount in of_single_food_service.transferOrderPay.items}
fact f66 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderPay.items in of_single_food_service.transferOrderPay.targets.paidFoodItem + of_single_food_service.transferOrderPay.targets.paidAmount}
fact f67 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderPay.targets.paidFoodItem + of_single_food_service.transferOrderPay.targets.paidAmount in of_single_food_service.transferOrderPay.items}
fact f68 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[sources, of_single_food_service.transferPayEat, of_single_food_service.pay]}
fact f69 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[targets, of_single_food_service.transferPayEat, of_single_food_service.eat]}
fact f70 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForSources[of_single_food_service.transferPayEat]}
fact f71 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForTargets[of_single_food_service.transferPayEat]}
fact f72 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferPayEat.items in of_single_food_service.transferPayEat.sources.paidFoodItem}
fact f73 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferPayEat.items in of_single_food_service.transferPayEat.targets.eatenItem}
fact f74 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferPayEat.sources.paidFoodItem in of_single_food_service.transferPayEat.items}
fact f75 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferPayEat.targets.eatenItem in of_single_food_service.transferPayEat.items}
fact f76 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderServe.items in of_single_food_service.transferOrderServe.sources.orderedFoodItem}
fact f77 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderServe.items in of_single_food_service.transferOrderServe.targets.servedFoodItem}
fact f78 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderServe.sources.orderedFoodItem in of_single_food_service.transferOrderServe.items}
fact f79 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderServe.targets.servedFoodItem in of_single_food_service.transferOrderServe.items}
fact f80 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[sources, of_single_food_service.transferOrderPrepare, of_single_food_service.order]}
fact f81 {all of_single_food_service: OFSingleFoodService | bijectionFiltered[targets, of_single_food_service.transferOrderPrepare, of_single_food_service.prepare]}
fact f82 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForSources[of_single_food_service.transferOrderPrepare]}
fact f83 {all of_single_food_service: OFSingleFoodService | subsettingItemRuleForTargets[of_single_food_service.transferOrderPrepare]}
fact f84 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderPrepare.items in of_single_food_service.transferOrderPrepare.sources.orderedFoodItem + of_single_food_service.transferOrderPrepare.sources.orderDestination}
fact f85 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderPrepare.items in of_single_food_service.transferOrderPrepare.targets.preparedFoodItem + of_single_food_service.transferOrderPrepare.targets.prepareDestination}
fact f86 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderPrepare.sources.orderedFoodItem + of_single_food_service.transferOrderPrepare.sources.orderDestination in of_single_food_service.transferOrderPrepare.items}
fact f87 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferOrderPrepare.targets.preparedFoodItem + of_single_food_service.transferOrderPrepare.targets.prepareDestination in of_single_food_service.transferOrderPrepare.items}
fact f88 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferPrepareServe.items in of_single_food_service.transferPrepareServe.sources.preparedFoodItem + of_single_food_service.transferPrepareServe.sources.prepareDestination}
fact f89 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferPrepareServe.sources.preparedFoodItem + of_single_food_service.transferPrepareServe.sources.prepareDestination in of_single_food_service.transferPrepareServe.items}
fact f90 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferPrepareServe.items in of_single_food_service.transferPrepareServe.targets.servedFoodItem + of_single_food_service.transferPrepareServe.targets.serviceDestination}
fact f91 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferPrepareServe.targets.servedFoodItem + of_single_food_service.transferPrepareServe.targets.serviceDestination in of_single_food_service.transferPrepareServe.items}
fact f92 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferServeEat.items in of_single_food_service.transferServeEat.sources.servedFoodItem}
fact f93 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferServeEat.items in of_single_food_service.transferServeEat.targets.eatenItem}
fact f94 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferServeEat.sources.servedFoodItem in of_single_food_service.transferServeEat.items}
fact f95 {all of_single_food_service: OFSingleFoodService | of_single_food_service.transferServeEat.targets.eatenItem in of_single_food_service.transferServeEat.items}
fact f96 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.order in OFCustomOrder}
fact f97 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.prepare in OFCustomPrepare}
fact f98 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.serve in OFCustomServe}
fact f99 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.start + of_loop_food_service.end + of_loop_food_service.transferOrderPrepare + of_loop_food_service.transferOrderPay + of_loop_food_service.transferPayEat in of_loop_food_service.steps}
fact f100 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.steps in of_loop_food_service.order + of_loop_food_service.prepare + of_loop_food_service.pay + of_loop_food_service.serve + of_loop_food_service.eat + of_loop_food_service.start + of_loop_food_service.end + of_loop_food_service.transferPrepareServe + of_loop_food_service.transferOrderServe + of_loop_food_service.transferServeEat + of_loop_food_service.transferOrderPrepare + of_loop_food_service.transferOrderPay + of_loop_food_service.transferPayEat}
fact f101 {all of_loop_food_service: OFLoopFoodService | #(of_loop_food_service.start) = 1}
fact f102 {all of_loop_food_service: OFLoopFoodService | functionFiltered[happensBefore, of_loop_food_service.start, of_loop_food_service.order]}
fact f103 {all of_loop_food_service: OFLoopFoodService | #(of_loop_food_service.order) = 2}
fact f104 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.order.outputs in of_loop_food_service.order.orderedFoodItem + of_loop_food_service.order.orderAmount + of_loop_food_service.order.orderDestination}
fact f105 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, of_loop_food_service.order, FoodItem]}
fact f106 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, of_loop_food_service.order, Real]}
fact f107 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, of_loop_food_service.order, Location]}
fact f108 {all of_loop_food_service: OFLoopFoodService | inverseFunctionFiltered[happensBefore, of_loop_food_service.start + of_loop_food_service.eat, of_loop_food_service.order]}
fact f109 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.pay.inputs in of_loop_food_service.pay.paidAmount + of_loop_food_service.pay.paidFoodItem}
fact f110 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, of_loop_food_service.pay, Real]}
fact f111 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, of_loop_food_service.pay, FoodItem]}
fact f112 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.pay.outputs in of_loop_food_service.pay.paidFoodItem}
fact f113 {all of_loop_food_service: OFLoopFoodService | functionFiltered[outputs, of_loop_food_service.pay, FoodItem]}
fact f114 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.prepare.inputs in of_loop_food_service.prepare.preparedFoodItem + of_loop_food_service.prepare.prepareDestination}
fact f115 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, of_loop_food_service.prepare, FoodItem]}
fact f116 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, of_loop_food_service.prepare, Location]}
fact f117 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.prepare.outputs in of_loop_food_service.prepare.preparedFoodItem + of_loop_food_service.prepare.prepareDestination}
fact f118 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, of_loop_food_service.prepare, FoodItem]}
fact f119 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, of_loop_food_service.prepare, Location]}
fact f120 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.serve.inputs in of_loop_food_service.serve.servedFoodItem + of_loop_food_service.serve.serviceDestination}
fact f121 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, of_loop_food_service.serve, FoodItem]}
fact f122 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, of_loop_food_service.serve, Location]}
fact f123 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.serve.outputs in of_loop_food_service.serve.servedFoodItem + of_loop_food_service.serve.serviceDestination}
fact f124 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, of_loop_food_service.serve, Location]}
fact f125 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[outputs, of_loop_food_service.serve, FoodItem]}
fact f126 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.eat.inputs in of_loop_food_service.eat.eatenItem}
fact f127 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[inputs, of_loop_food_service.eat, FoodItem]}
fact f128 {all of_loop_food_service: OFLoopFoodService | functionFiltered[happensBefore, of_loop_food_service.eat, of_loop_food_service.end + of_loop_food_service.order]}
fact f129 {all of_loop_food_service: OFLoopFoodService | #(of_loop_food_service.end) = 1}
fact f130 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[sources, of_loop_food_service.transferOrderPay, of_loop_food_service.order]}
fact f131 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[targets, of_loop_food_service.transferOrderPay, of_loop_food_service.pay]}
fact f132 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForSources[of_loop_food_service.transferOrderPay]}
fact f133 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForTargets[of_loop_food_service.transferOrderPay]}
fact f134 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderPay.items in of_loop_food_service.transferOrderPay.sources.orderedFoodItem + of_loop_food_service.transferOrderPay.sources.orderAmount}
fact f135 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderPay.sources.orderedFoodItem + of_loop_food_service.transferOrderPay.sources.orderAmount in of_loop_food_service.transferOrderPay.items}
fact f136 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderPay.items in of_loop_food_service.transferOrderPay.targets.paidFoodItem + of_loop_food_service.transferOrderPay.targets.paidAmount}
fact f137 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderPay.targets.paidFoodItem + of_loop_food_service.transferOrderPay.targets.paidAmount in of_loop_food_service.transferOrderPay.items}
fact f138 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[sources, of_loop_food_service.transferPayEat, of_loop_food_service.pay]}
fact f139 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[targets, of_loop_food_service.transferPayEat, of_loop_food_service.eat]}
fact f140 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForSources[of_loop_food_service.transferPayEat]}
fact f141 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForTargets[of_loop_food_service.transferPayEat]}
fact f142 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferPayEat.items in of_loop_food_service.transferPayEat.sources.paidFoodItem}
fact f143 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferPayEat.items in of_loop_food_service.transferPayEat.targets.eatenItem}
fact f144 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferPayEat.sources.paidFoodItem in of_loop_food_service.transferPayEat.items}
fact f145 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferPayEat.targets.eatenItem in of_loop_food_service.transferPayEat.items}
fact f146 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderServe.items in of_loop_food_service.transferOrderServe.sources.orderedFoodItem}
fact f147 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderServe.items in of_loop_food_service.transferOrderServe.targets.servedFoodItem}
fact f148 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderServe.sources.orderedFoodItem in of_loop_food_service.transferOrderServe.items}
fact f149 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderServe.targets.servedFoodItem in of_loop_food_service.transferOrderServe.items}
fact f150 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[sources, of_loop_food_service.transferOrderPrepare, of_loop_food_service.order]}
fact f151 {all of_loop_food_service: OFLoopFoodService | bijectionFiltered[targets, of_loop_food_service.transferOrderPrepare, of_loop_food_service.prepare]}
fact f152 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForSources[of_loop_food_service.transferOrderPrepare]}
fact f153 {all of_loop_food_service: OFLoopFoodService | subsettingItemRuleForTargets[of_loop_food_service.transferOrderPrepare]}
fact f154 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderPrepare.items in of_loop_food_service.transferOrderPrepare.sources.orderedFoodItem + of_loop_food_service.transferOrderPrepare.sources.orderDestination}
fact f155 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderPrepare.items in of_loop_food_service.transferOrderPrepare.targets.preparedFoodItem + of_loop_food_service.transferOrderPrepare.targets.prepareDestination}
fact f156 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderPrepare.sources.orderedFoodItem + of_loop_food_service.transferOrderPrepare.sources.orderDestination in of_loop_food_service.transferOrderPrepare.items}
fact f157 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferOrderPrepare.targets.preparedFoodItem + of_loop_food_service.transferOrderPrepare.targets.prepareDestination in of_loop_food_service.transferOrderPrepare.items}
fact f158 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferPrepareServe.items in of_loop_food_service.transferPrepareServe.sources.preparedFoodItem + of_loop_food_service.transferPrepareServe.sources.prepareDestination}
fact f159 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferPrepareServe.sources.preparedFoodItem + of_loop_food_service.transferPrepareServe.sources.prepareDestination in of_loop_food_service.transferPrepareServe.items}
fact f160 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferPrepareServe.items in of_loop_food_service.transferPrepareServe.targets.servedFoodItem + of_loop_food_service.transferPrepareServe.targets.serviceDestination}
fact f161 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferPrepareServe.targets.servedFoodItem + of_loop_food_service.transferPrepareServe.targets.serviceDestination in of_loop_food_service.transferPrepareServe.items}
fact f162 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferServeEat.items in of_loop_food_service.transferServeEat.sources.servedFoodItem}
fact f163 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferServeEat.items in of_loop_food_service.transferServeEat.targets.eatenItem}
fact f164 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferServeEat.sources.servedFoodItem in of_loop_food_service.transferServeEat.items}
fact f165 {all of_loop_food_service: OFLoopFoodService | of_loop_food_service.transferServeEat.targets.eatenItem in of_loop_food_service.transferServeEat.items}
fact f166 {all of_parallel_food_service: OFParallelFoodService | bijectionFiltered[happensBefore, of_parallel_food_service.pay, of_parallel_food_service.prepare]}
fact f167 {all of_parallel_food_service: OFParallelFoodService | bijectionFiltered[happensBefore, of_parallel_food_service.pay, of_parallel_food_service.order]}

// Functions and predicates:
pred instancesDuringExample{Order in OFFoodService.order and Prepare in OFFoodService.prepare and Serve in OFFoodService.serve and Eat in OFFoodService.eat and Pay in OFFoodService.pay}
pred onlyOFFoodService{FoodService in OFFoodService and noChildFoodService and #OFFoodService = 1 and noCustomFoodService}
pred noChildFoodService{no OFSingleFoodService and no OFLoopFoodService and no OFParallelFoodService}
pred noCustomFoodService{no OFCustomOrder and no OFCustomPrepare and no OFCustomServe}
pred onlyOFSingleFoodService{FoodService in OFSingleFoodService}
pred onlyOFLoopFoodService{FoodService in OFLoopFoodService}
pred onlyOFParallelFoodService{FoodService in OFParallelFoodService}

// Commands:
run showOFFoodService{nonZeroDurationOnly and instancesDuringExample and onlyOFFoodService and #(OFFoodService.order) = 1} for 12
run showOFSingleFoodService{nonZeroDurationOnly and instancesDuringExample and onlyOFSingleFoodService} for 15 but exactly 1 OFSingleFoodService
run showOFLoopFoodService{nonZeroDurationOnly and instancesDuringExample and onlyOFLoopFoodService} for 30 but exactly 1 OFLoopFoodService
run showOFParallelFoodService{nonZeroDurationOnly and instancesDuringExample and onlyOFParallelFoodService} for 10

