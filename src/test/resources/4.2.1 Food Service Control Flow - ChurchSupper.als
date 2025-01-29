//*****************************************************************
// Module: 		Food Service Control Flow - Church Supper
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence/Transfer module to show
//				control flow in various kinds of Food Service from Wyner (see NISTIR 8283).
//*****************************************************************
module ChurchSupperModule
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Eat extends Occurrence {}
fact {all x: Eat | no x.inputs}
fact {all x: Eat | no inputs.x}
fact {all x: Eat | no x.outputs}
fact {all x: Eat | no outputs.x}
fact {all x: Eat | no items.x}
fact {all x: Eat | no x.steps}

sig Order extends Occurrence {}
fact {all x: Order | no x.inputs}
fact {all x: Order | no inputs.x}
fact {all x: Order | no x.outputs}
fact {all x: Order | no outputs.x}
fact {all x: Order | no items.x}
fact {all x: Order | no x.steps}

sig Pay extends Occurrence {}
fact {all x: Pay | no x.inputs}
fact {all x: Pay | no inputs.x}
fact {all x: Pay | no x.outputs}
fact {all x: Pay | no outputs.x}
fact {all x: Pay | no items.x}
fact {all x: Pay | no x.steps}

sig Prepare extends Occurrence {}
fact {all x: Prepare | no x.inputs}
fact {all x: Prepare | no inputs.x}
fact {all x: Prepare | no x.outputs}
fact {all x: Prepare | no outputs.x}
fact {all x: Prepare | no items.x}
fact {all x: Prepare | no x.steps}

sig Serve extends Occurrence {}
fact {all x: Serve | no x.inputs}
fact {all x: Serve | no inputs.x}
fact {all x: Serve | no x.outputs}
fact {all x: Serve | no outputs.x}
fact {all x: Serve | no items.x}
fact {all x: Serve | no x.steps}
//*****************************************************************
/** 				Food Service */
//*****************************************************************
/**	Definition */
sig FoodService extends Occurrence {eat: set Eat, order: set Order, pay: set Pay, prepare: set Prepare, serve: set Serve}

fact {all x: FoodService | bijectionFiltered[happensBefore, x.order, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.prepare, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.serve, x.eat]}
fact {all x: FoodService | x.eat + x.order + x.pay + x.prepare + x.serve in x.steps}
//*****************************************************************
/** 				Single Food Service */
//*****************************************************************
/**	Definition */
sig SingleFoodService extends FoodService{}
fact {all x: SingleFoodService | x.eat in Eat}
fact {all x: SingleFoodService | x.order in Order}
fact {all x: SingleFoodService | x.pay in Pay}
fact {all x: SingleFoodService | x.prepare in Prepare}
fact {all x: SingleFoodService | x.serve in Serve}
fact {all x: SingleFoodService | #(x.order) = 1}
fact {all x: SingleFoodService | #(x.prepare) = 1}
fact {all x: SingleFoodService | #(x.pay) = 1}
fact {all x: SingleFoodService | #(x.eat) = 1}
fact {all x: SingleFoodService | #(x.serve) = 1}
//*****************************************************************
/** 				Church Supper */
//*****************************************************************
/**	Definition */
sig ChurchSupper extends SingleFoodService{}
fact {all x: ChurchSupper | bijectionFiltered[happensBefore, x.pay, x.prepare]}
fact {all x: ChurchSupper | bijectionFiltered[happensBefore, x.pay, x.order]}
/** Model closure */
fact {all x: ChurchSupper | x.steps in x.eat + x.order + x.pay + x.prepare + x.serve}
fact {all x: ChurchSupper | no x.inputs}
fact {all x: ChurchSupper | no inputs.x}
fact {all x: ChurchSupper | no x.outputs}
fact {all x: ChurchSupper | no outputs.x}
fact {all x: ChurchSupper | no items.x}
fact {all x: ChurchSupper | no steps.x}
fact {all x: ChurchSupper | no y: Transfer | y in x.steps}
//******************************************************************************************************
/** 			General Functions and Predicates */
//******************************************************************************************************
pred instancesDuringExample{all x: (Eat + Order + Pay + Prepare + Serve) | x in ChurchSupper.steps}
//******************************************************************************************************
/** 				Checks and Runs */
//	Note: Use the "Test Case Generator - Food Service.xlsx" file to generate cases for each FoodService subtype. Otherwise, the list of 
//		checks is too long for Alloy to support in its menu.
//******************************************************************************************************
run showChurchSupper{instancesDuringExample and some ChurchSupper and FoodService in ChurchSupper and no BinaryLink} for 10
