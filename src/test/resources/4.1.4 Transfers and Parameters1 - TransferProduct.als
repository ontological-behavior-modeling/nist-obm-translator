//*****************************************************************
// Module: 		Transfer Product
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model calling behaviors
//				in which parameters are passed. 
//*****************************************************************
module TransferProduct
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Real, Product extends Occurrence{}

//*****************************************************************
/** 					Supplier */
//*****************************************************************
sig Supplier extends Occurrence{suppliedProduct: set Product}

fact {all x: Supplier | #(x.suppliedProduct) = 1}
fact {all x: Supplier | x.suppliedProduct in x.outputs}
fact {all x: Supplier | x.outputs in x.suppliedProduct}
fact {all x: Supplier | no x.inputs}
//*****************************************************************
/** 					Customer */
//*****************************************************************
sig Customer extends Occurrence{receivedProduct: set Product}

fact {all x: Customer | #(x.receivedProduct) = 1}
fact {all x: Customer | x.receivedProduct in x.inputs}
fact {all x: Customer | x.inputs in x.receivedProduct}
fact {all x: Customer | no x.outputs}

//*****************************************************************
/** 					TransferProduct */
//*****************************************************************
sig TransferProduct extends Occurrence{
	supplier: set Supplier,
	customer: set Customer,
	transferSupplierCustomer: set Transfer
}

fact {all x: TransferProduct | #(x.supplier) = 1}
fact {all x: TransferProduct | #(x.customer) = 1}
fact {all x: TransferProduct | bijectionFiltered[sources, x.transferSupplierCustomer, x.supplier]}
fact {all x: TransferProduct | bijectionFiltered[targets, x.transferSupplierCustomer, x.customer]}
fact {all x: TransferProduct | subsettingItemRuleForSources[x.transferSupplierCustomer]}
fact {all x: TransferProduct | subsettingItemRuleForTargets[x.transferSupplierCustomer]}
fact {all x: TransferProduct | x.steps in x.customer + x.supplier + x.transferSupplierCustomer}
fact {all x: TransferProduct | x.customer + x.supplier + x.transferSupplierCustomer in x.steps}


pred instancesDuringExample {Supplier in TransferProduct.supplier and Customer in TransferProduct.customer}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run transferProduct{instancesDuringExample and no HappensBefore and some TransferProduct} for 12
