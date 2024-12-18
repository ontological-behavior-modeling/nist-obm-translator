//*****************************************************************
// Module: 		Transfer Product
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model calling behaviors
//				in which parameters are passed. 
//*****************************************************************
module TransferProductModule
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Product extends Occurrence{}

/** Model closure */
fact {all x: Product | no x.inputs}
fact {all x: Product | no x.outputs}
fact {all x: Product | no x.steps}
fact {all x: Product | no steps.x}
//*****************************************************************
/** 					Supplier */
//*****************************************************************
sig Supplier extends Occurrence{suppliedProduct: set Product}

fact {all x: Supplier | x.suppliedProduct in x.outputs}
fact {all x: Supplier | x.outputs in x.suppliedProduct}
fact {all x: Supplier | #(x.suppliedProduct) = 1}
/** Model closure */
fact {all x: Supplier | no x.steps}
fact {all x: Supplier | no x.inputs}
fact {all x: Supplier | no inputs.x}
fact {all x: Supplier | no outputs.x}
fact {all x: Supplier | no items.x}
//*****************************************************************
/** 					Customer */
//*****************************************************************
sig Customer extends Occurrence{receivedProduct: set Product}

fact {all x: Customer | x.receivedProduct in x.inputs}
fact {all x: Customer | x.inputs in x.receivedProduct}
fact {all x: Customer | #(x.receivedProduct) = 1}
/** Model closure */
fact {all x: Customer | no x.steps}
fact {all x: Customer | no x.outputs}
fact {all x: Customer | no inputs.x}
fact {all x: Customer | no outputs.x}
fact {all x: Customer | no items.x}
//*****************************************************************
/** 					TransferProduct */
//*****************************************************************
sig TransferProduct extends Occurrence{customer: set Customer, supplier: set Supplier, transferSupplierCustomer: set Transfer}

fact {all x: TransferProduct | x.customer + x.supplier + x.transferSupplierCustomer in x.steps}
fact {all x: TransferProduct | x.steps in x.customer + x.supplier + x.transferSupplierCustomer}
fact {all x: TransferProduct | no x.inputs}
fact {all x: TransferProduct | no x.outputs}
fact {all x: TransferProduct | no inputs.x}
fact {all x: TransferProduct | no outputs.x}
fact {all x: TransferProduct | no items.x}
/** Supplier */
fact {all x: TransferProduct | #(x.supplier) = 1}
fact {all x: TransferProduct | bijectionFiltered[outputs, x.supplier, x.supplier.suppliedProduct]}
fact {all x: TransferProduct | x.supplier.outputs in x.supplier.suppliedProduct}
/** Customer */
fact {all x: TransferProduct | #(x.customer) = 1}
fact {all x: TransferProduct | bijectionFiltered[inputs, x.customer, x.customer.receivedProduct]}
fact {all x: TransferProduct | x.customer.inputs in x.customer.receivedProduct}
/** Transfers */
fact {all x: TransferProduct | bijectionFiltered[sources, x.transferSupplierCustomer, x.supplier]}
fact {all x: TransferProduct | bijectionFiltered[targets, x.transferSupplierCustomer, x.customer]}
fact {all x: TransferProduct | subsettingItemRuleForSources[x.transferSupplierCustomer]}
fact {all x: TransferProduct | subsettingItemRuleForTargets[x.transferSupplierCustomer]}
fact {all x: TransferProduct | x.transferSupplierCustomer.sources.suppliedProduct in x.transferSupplierCustomer.items}
fact {all x: TransferProduct | x.transferSupplierCustomer.items in x.transferSupplierCustomer.sources.suppliedProduct}
fact {all x: TransferProduct | x.transferSupplierCustomer.targets.receivedProduct in x.transferSupplierCustomer.items}
fact {all x: TransferProduct | x.transferSupplierCustomer.items in x.transferSupplierCustomer.targets.receivedProduct}
//*****************************************************************
/** 					General Functions and Predicates */
//*****************************************************************
pred instancesDuringExample {Supplier in TransferProduct.supplier and Customer in TransferProduct.customer}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run transferProduct{instancesDuringExample and no HappensBefore and some TransferProduct} for 12
