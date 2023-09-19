//*****************************************************************
// Module: 		Food Service 
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence/Transfer module to show
//				control flow in various kinds of Food Service from Wyner (see NISTIR 8283).
//*****************************************************************

//MW -

module RestaurantService
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Order, Prepare, Serve, Eat, Pay extends Occurrence {}
//*****************************************************************
/** 				Generic Food Service */
//*****************************************************************
/**	Definition */
sig FoodService extends Occurrence {
	order: set Order,
	prepare: set Prepare,
	pay: set Pay,
	eat: set Eat,
	serve: set Serve
}{
	/*bijectionFiltered[happensBefore, order, serve]
	bijectionFiltered[happensBefore, prepare, serve]
	bijectionFiltered[happensBefore, serve, eat]

	order + prepare + pay + eat + serve in this.steps and this.steps in order + prepare + pay + eat + serve
	*/
}	
fact {all x: FoodService | bijectionFiltered[happensBefore, x.order, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.prepare, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.serve, x.eat]}

fact {all x: FoodService | x.eat + x.order + x.pay + x.prepare + x.serve in x.steps}
fact {all x: FoodService | x.steps in x.eat + x.order + x.pay + x.prepare + x.serve}

//*****************************************************************
/** 				Generic Single Food Service */
//*****************************************************************
/**	Definition */
sig SingleFoodService extends FoodService{}{
	/*#order = 1
	#prepare = 1
	#pay = 1
	#eat = 1
	#serve = 1
	*/
}
fact {all x: SingleFoodService | #(x.order) = 1}
fact {all x: SingleFoodService | #(x.prepare) = 1}
fact {all x: SingleFoodService | #(x.pay) = 1}
fact {all x: SingleFoodService | #(x.eat) = 1}
fact {all x: SingleFoodService | #(x.serve) = 1}

//*****************************************************************
/** 				Restaurant Supper Service */
//*****************************************************************
/**	Definition */
sig RestaurantService extends SingleFoodService{}{
	/*bijectionFiltered[happensBefore, eat, pay]*/
}
fact {all x: RestaurantService | bijectionFiltered[happensBefore, x.eat, x.pay]}


