// This file is created with code.

module FoodServiceControlFlow_ExplicitFast
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig Order extends Occurrence {}
sig Prepare extends Occurrence {}
sig Serve extends Occurrence {}
sig Eat extends Occurrence {}
sig Pay extends Occurrence {}
sig FoodService extends Occurrence { order: set Order, prepare: set Prepare, pay: set Pay, eat: set Eat, serve: set Serve }
sig SingleFoodService extends FoodService {}
sig BuffetService extends FoodService {}
sig ChurchSupperService extends FoodService {}
sig FastFoodService extends FoodService {}
sig RestaurantService extends FoodService {}
sig UnsatisfiableFoodService extends Occurrence {}

// Facts:
fact f1 {all s: FoodService | bijectionFiltered[happensBefore, s.order, s.serve]}
fact f2 {all s: FoodService | bijectionFiltered[happensBefore, s.prepare, s.serve]}
fact f3 {all s: FoodService | bijectionFiltered[happensBefore, s.serve, s.eat]}
fact f4 {all s: FoodService | s.prepare + s.pay + s.eat + s.serve + s.order in s.steps}
fact f5 {all s: FoodService | s.steps in s.prepare + s.pay + s.eat + s.serve + s.order}
fact f6 {all s: SingleFoodService | #(s.order) = 1}
fact f7 {all s: SingleFoodService | #(s.prepare) = 1}
fact f8 {all s: SingleFoodService | #(s.pay) = 1}
fact f9 {all s: SingleFoodService | #(s.eat) = 1}
fact f10 {all s: SingleFoodService | #(s.serve) = 1}
fact f11 {all s: BuffetService | bijectionFiltered[happensBefore, s.prepare, s.order]}
fact f12 {all s: BuffetService | bijectionFiltered[happensBefore, s.eat, s.pay]}
fact f13 {all s: ChurchSupperService | bijectionFiltered[happensBefore, s.pay, s.prepare]}
fact f14 {all s: ChurchSupperService | bijectionFiltered[happensBefore, s.pay, s.order]}
fact f15 {all s: FastFoodService | bijectionFiltered[happensBefore, s.order, s.pay]}
fact f16 {all s: FastFoodService | bijectionFiltered[happensBefore, s.pay, s.eat]}
fact f17 {all s: RestaurantService | bijectionFiltered[happensBefore, s.eat, s.pay]}
fact f18 {all s: UnsatisfiableFoodService | bijectionFiltered[happensBefore, s.eat, s.pay]}
fact f19 {all s: UnsatisfiableFoodService | bijectionFiltered[happensBefore, s.pay, s.prepare]}

// Functions and predicates:
pred suppressTransfers{no Transfer}
pred suppressIO{no inputs and no outputs}
pred instancesDuringExample{Order in FoodService.order and Prepare in FoodService.prepare and Serve in FoodService.serve and Eat in FoodService.eat and Pay in FoodService.pay}
pred onlyRestaurantService{#RestaurantService = 1 and all g: RestaurantService | g in RestaurantService}

// Commands:
run showFoodService{nonZeroDurationOnly and Order in FoodService.order and Prepare in FoodService.prepare and Serve in FoodService.serve and Eat in FoodService.eat and Pay in FoodService.pay and #FoodService = 1 and no SingleFoodService and no BuffetService and no ChurchSupperService and no FastFoodService and no RestaurantService and no UnsatisfiableFoodService and suppressTransfers and suppressIO} for 10
run showSingleFoodService{nonZeroDurationOnly and Order in FoodService.order and Prepare in FoodService.prepare and Serve in FoodService.serve and Eat in FoodService.eat and Pay in FoodService.pay and FoodService in SingleFoodService and no BuffetService and no ChurchSupperService and no FastFoodService and no RestaurantService and no UnsatisfiableFoodService and suppressTransfers and suppressIO} for 10
run showBuffetService{nonZeroDurationOnly and Order in FoodService.order and Prepare in FoodService.prepare and Serve in FoodService.serve and Eat in FoodService.eat and Pay in FoodService.pay and FoodService in SingleFoodService and no BuffetService and no ChurchSupperService and no FastFoodService and no RestaurantService and no UnsatisfiableFoodService and suppressTransfers and suppressIO} for 10
run showChurchSupperService{nonZeroDurationOnly and Order in FoodService.order and Prepare in FoodService.prepare and Serve in FoodService.serve and Eat in FoodService.eat and Pay in FoodService.pay and FoodService in SingleFoodService and no BuffetService and no ChurchSupperService and no FastFoodService and no RestaurantService and no UnsatisfiableFoodService and suppressTransfers and suppressIO} for 10
run showFastFoodService{nonZeroDurationOnly and Order in FoodService.order and Prepare in FoodService.prepare and Serve in FoodService.serve and Eat in FoodService.eat and Pay in FoodService.pay and FoodService in SingleFoodService and no BuffetService and no ChurchSupperService and no FastFoodService and no RestaurantService and no UnsatisfiableFoodService and suppressTransfers and suppressIO} for 10
run showRestaurantService{nonZeroDurationOnly and suppressTransfers and suppressIO and instancesDuringExample and onlyRestaurantService} for 10
run showUnsatisfiableFoodService{Order in FoodService.order and Prepare in FoodService.prepare and Serve in FoodService.serve and Eat in FoodService.eat and Pay in FoodService.pay and #UnsatisfiableFoodService = 1 and all g: UnsatisfiableFoodService | g in UnsatisfiableFoodService and suppressTransfers and suppressIO} for 15

