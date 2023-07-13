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
sig BuffetService extends SingleFoodService {}
sig ChurchSupperService extends SingleFoodService {}
sig FastFoodService extends SingleFoodService {}
sig RestaurantService extends SingleFoodService {}
sig UnsatisfiableFoodService extends SingleFoodService {}

// Facts:
fact f1 {all fs: FoodService | bijectionFiltered[happensBefore, fs.order, fs.serve]}
fact f2 {all fs: FoodService | bijectionFiltered[happensBefore, fs.prepare, fs.serve]}
fact f3 {all fs: FoodService | bijectionFiltered[happensBefore, fs.serve, fs.eat]}
fact f4 {all fs: FoodService | fs.order + fs.prepare + fs.pay + fs.eat + fs.serve in fs.steps}
fact f5 {all fs: FoodService | fs.steps in fs.order + fs.prepare + fs.pay + fs.eat + fs.serve}
fact f6 {all sfs: SingleFoodService | #(sfs.order) = 1}
fact f7 {all sfs: SingleFoodService | #(sfs.prepare) = 1}
fact f8 {all sfs: SingleFoodService | #(sfs.pay) = 1}
fact f9 {all sfs: SingleFoodService | #(sfs.eat) = 1}
fact f10 {all sfs: SingleFoodService | #(sfs.serve) = 1}
fact f11 {all bs: BuffetService | bijectionFiltered[happensBefore, bs.prepare, bs.order]}
fact f12 {all bs: BuffetService | bijectionFiltered[happensBefore, bs.eat, bs.pay]}
fact f13 {all css: ChurchSupperService | bijectionFiltered[happensBefore, css.pay, css.prepare]}
fact f14 {all css: ChurchSupperService | bijectionFiltered[happensBefore, css.pay, css.order]}
fact f15 {all ffs: FastFoodService | bijectionFiltered[happensBefore, ffs.order, ffs.pay]}
fact f16 {all ffs: FastFoodService | bijectionFiltered[happensBefore, ffs.pay, ffs.eat]}
fact f17 {all rs: RestaurantService | bijectionFiltered[happensBefore, rs.eat, rs.pay]}
fact f18 {all ufs: UnsatisfiableFoodService | bijectionFiltered[happensBefore, ufs.eat, ufs.pay]}
fact f19 {all ufs: UnsatisfiableFoodService | bijectionFiltered[happensBefore, ufs.pay, ufs.prepare]}

// Functions and predicates:
pred instancesDuringExample{Order in FoodService.order and Prepare in FoodService.prepare and Serve in FoodService.serve and Eat in FoodService.eat and Pay in FoodService.pay}
pred onlyFoodService{#FoodService = 1 and no SingleFoodService and noChildFoodService}
pred suppressTransfers{no Transfer}
pred suppressIO{no inputs and no outputs}
pred noChildFoodService{no BuffetService and no ChurchSupperService and no FastFoodService and no RestaurantService and no UnsatisfiableFoodService}
pred onlySingleFoodService{FoodService in SingleFoodService and noChildFoodService}
pred onlyBuffetService{#BuffetService = 1 and all g: FoodService | g in BuffetService}
pred onlyChurchSupperService{#ChurchSupperService = 1 and all g: ChurchSupperService | g in ChurchSupperService}
pred onlyFastFoodService{#FoodService = 1 and all g: FastFoodService | g in FastFoodService}
pred onlyRestaurantService{#RestaurantService = 1 and all g: RestaurantService | g in RestaurantService}
pred onlyUnsatisfiableFoodService{#UnsatisfiableFoodService = 1 and all g: UnsatisfiableFoodService | g in UnsatisfiableFoodService}

// Commands:
run showFoodService{nonZeroDurationOnly and instancesDuringExample and onlyFoodService and suppressTransfers and suppressIO} for 10
run showSingleFoodService{nonZeroDurationOnly and instancesDuringExample and onlySingleFoodService and suppressTransfers and suppressIO} for 10 but exactly 1 SingleFoodService
run showBuffetService{nonZeroDurationOnly and instancesDuringExample and onlyBuffetService and suppressTransfers and suppressIO} for 10
run showChurchSupperService{nonZeroDurationOnly and instancesDuringExample and onlyChurchSupperService and suppressTransfers and suppressIO} for 10
run showFastFoodService{nonZeroDurationOnly and instancesDuringExample and onlyFastFoodService and suppressTransfers and suppressIO} for 10
run showRestaurantService{nonZeroDurationOnly and instancesDuringExample and onlyRestaurantService and suppressTransfers and suppressIO} for 10
run showUnsatisfiableFoodService{instancesDuringExample and onlyUnsatisfiableFoodService and suppressTransfers and suppressIO} for 15

