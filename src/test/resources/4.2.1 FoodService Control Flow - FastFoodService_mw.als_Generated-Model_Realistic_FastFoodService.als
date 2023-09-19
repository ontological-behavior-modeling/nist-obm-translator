// This file is created with code.

module FastFoodService
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
sig SingleFoodService extends FoodService {}
sig FastFoodService extends SingleFoodService {}

// Facts:
fact {all x: SingleFoodService | #(x.order) = 1}
fact {all x: SingleFoodService | #(x.prepare) = 1}
fact {all x: SingleFoodService | #(x.pay) = 1}
fact {all x: SingleFoodService | #(x.serve) = 1}
fact {all x: SingleFoodService | #(x.eat) = 1}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.prepare, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.order, x.serve]}
fact {all x: FoodService | bijectionFiltered[happensBefore, x.serve, x.eat]}
fact {all x: FastFoodService | bijectionFiltered[happensBefore, x.pay, x.eat]}
fact {all x: FastFoodService | bijectionFiltered[happensBefore, x.order, x.pay]}
fact {all x: FoodService | x.eat + x.order + x.pay + x.prepare + x.serve in x.steps}
fact {all x: FoodService | x.steps in x.eat + x.order + x.pay + x.prepare + x.serve}

