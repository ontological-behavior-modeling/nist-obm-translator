//*****************************************************************
// Module: 		Food Service
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using the Occurrence/Transfer module to show
//				control flow in various kinds of Food Service from Wyner (see NISTIR 8283).
//*****************************************************************
module FoodService
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
	bijectionFiltered[happensBefore, order, serve]
	bijectionFiltered[happensBefore, prepare, serve]
	bijectionFiltered[happensBefore, serve, eat]

	order + prepare + pay + eat + serve in this.steps and this.steps in order + prepare + pay + eat + serve
}
//*****************************************************************
/** 				Generic Single Food Service */
//*****************************************************************
/**	Definition */
sig SingleFoodService extends FoodService{}{
	#order = 1
	#prepare = 1
	#pay = 1
	#eat = 1
	#serve = 1
}
//*****************************************************************
/** 				Buffet Service */
//*****************************************************************
/**	Definition */
sig BuffetService extends SingleFoodService{}{
	bijectionFiltered[happensBefore, prepare, order]
	bijectionFiltered[happensBefore, eat, pay]
}
//*****************************************************************
/** 				Church Supper Service */
//*****************************************************************
/**	Definition */
sig ChurchSupperService extends SingleFoodService{}{
	bijectionFiltered[happensBefore, pay, prepare]
	bijectionFiltered[happensBefore, pay, order]
}
//*****************************************************************
/** 				Fast Food Service */
//*****************************************************************
/**	Definition */
sig FastFoodService extends SingleFoodService{}{
	bijectionFiltered[happensBefore, order, pay]
	bijectionFiltered[happensBefore, pay, eat]
}

//*****************************************************************
/** 				Restaurant Service */
//*****************************************************************
/**	Definition */
sig RestaurantService extends SingleFoodService{}{
	bijectionFiltered[happensBefore, eat, pay]
}
//*****************************************************************
/** 				Usatisfiable Food Service */
//*****************************************************************
/**	Definition */
sig UsatisfiableFoodService extends SingleFoodService{}{
	bijectionFiltered[happensBefore, eat, pay]
	bijectionFiltered[happensBefore, pay, prepare]
//	r/asymmetric[^happensBefore]		// Comment this line to produce instance
}
//******************************************************************************************************
/** 			General Functions and Predicates */
//******************************************************************************************************
pred suppressTransfers {no Transfer}
pred suppressIO {no inputs and no outputs}
pred instancesDuringExample{Order in FoodService.order and Prepare in FoodService.prepare
		and Serve in FoodService.serve and Eat in FoodService.eat and Pay in FoodService.pay}
pred onlyFoodService {#FoodService = 1 and no SingleFoodService and noChildFoodService}
pred onlySingleFoodService {FoodService in SingleFoodService and noChildFoodService}
pred onlyBuffetService {#BuffetService = 1 and all g: FoodService | g in BuffetService}
pred onlyChurchSupperService {#ChurchSupperService = 1 and all g: FoodService | g in ChurchSupperService}
pred onlyFastFoodService {#FastFoodService =1 and all g: FoodService | g in FastFoodService}
pred onlyRestaurantService {#RestaurantService = 1 and all g: FoodService | g in RestaurantService}
pred onlyUsatisfiableFoodService {#UsatisfiableFoodService = 1 and all g: FoodService | g in UsatisfiableFoodService}
pred noChildFoodService {no BuffetService && no ChurchSupperService && no FastFoodService && no RestaurantService && no UsatisfiableFoodService}
//******************************************************************************************************
/** 				Checks and Runs */
//	Note: Use the "Test Case Generator - Food Service.xlsx" file to generate cases for each FoodService subtype. Otherwise, the list of 
//		checks is too long for Alloy to support in its menu.
//******************************************************************************************************
run showFoodService{nonZeroDurationOnly && instancesDuringExample && onlyFoodService and suppressTransfers and suppressIO} for 10
run showSingleFoodService{nonZeroDurationOnly && instancesDuringExample && onlySingleFoodService and suppressTransfers and suppressIO} for 10 but exactly 1 SingleFoodService
run showBuffetService{nonZeroDurationOnly && instancesDuringExample && onlyBuffetService and suppressTransfers and suppressIO} for 10
run showChurchSupperService{nonZeroDurationOnly && instancesDuringExample && onlyChurchSupperService and suppressTransfers and suppressIO} for 10
run showFastFoodService{nonZeroDurationOnly && instancesDuringExample && onlyFastFoodService and suppressTransfers and suppressIO} for 10
run showRestaurantService{nonZeroDurationOnly && instancesDuringExample && onlyRestaurantService and suppressTransfers and suppressIO} for 10
run showUsatisfiableFoodService{instancesDuringExample && onlyUsatisfiableFoodService and suppressTransfers and suppressIO} for 15
