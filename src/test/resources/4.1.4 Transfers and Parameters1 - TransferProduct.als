//*****************************************************************
// Module: 		Transfer Product
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model calling behaviors
//				in which parameters are passed. 
//*****************************************************************
module ParticipantTransfer
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Product extends Occurrence{}

//*****************************************************************
/** 					Supplier */
//*****************************************************************
sig Supplier extends Occurrence{
	suppliedProduct: set Product
}{
//suppliedProduct in this.outputs and this.outputs in suppliedProduct and no this.inputs
}

fact {all x: Supplier | x.suppliedProduct in x.outputs}
fact {all x: Supplier | x.outputs in x.suppliedProduct}
fact {all x: Supplier | no x.inputs}
//*****************************************************************
/** 					Customer */
//*****************************************************************
sig Customer extends Occurrence{
	receivedProduct: set Product
}

fact {all x: Customer | x.receivedProduct in x.inputs}
fact {all x: Customer | x.inputs in x.receivedProduct}
fact {all x: Customer | no x.outputs}

//*****************************************************************
/** 					ParticipantTransfer */
//*****************************************************************
sig ParticipantTransfer extends Occurrence{
	supplier: set Supplier,
	customer: set Customer,
	transferSupplierCustomer: set Transfer
}
fact {all x: ParticipantTransfer | #(x.customer) = 1}
fact {all x: ParticipantTransfer | #(x.supplier) = 1}

fact {all x: Customer | #(x.receivedProduct) = 1}
fact {all x: Supplier | #(x.suppliedProduct) = 1}

fact {all x: ParticipantTransfer | bijectionFiltered[sources, x.transferSupplierCustomer, x.supplier]}
fact {all x: ParticipantTransfer | bijectionFiltered[targets, x.transferSupplierCustomer, x.customer]}
fact {all x: ParticipantTransfer | subsettingItemRuleForSources[x.transferSupplierCustomer]}
fact {all x: ParticipantTransfer | subsettingItemRuleForTargets[x.transferSupplierCustomer]}


fact {all x: ParticipantTransfer | x.customer + x.supplier + x.transferSupplierCustomer in x.steps}
fact {all x: ParticipantTransfer | x.steps in x.customer + x.supplier + x.transferSupplierCustomer}

//pred instancesDuringExample {Supplier in TransferProduct.supplier and Customer in TransferProduct.customer}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
//run transferProduct{instancesDuringExample and some TransferProduct} for 12


//mw modified
// comment out pred, run
//sig Customer extends Occurrence{
//	receivedProduct: one Product
//}
//is changed to 
//sig Customer extends Occurrence { receivedProduct: set Product }
//fact {all x: Customer | #(x.receivedProduct) = 1}
//Sig Real is removed

//steps order is changed
//fact {all x: ParticipantTransfer | x.supplier + x.customer + x.transferSupplierCustomer in x.steps}
//to
//fact {all x: ParticipantTransfer | x.customer + x.supplier + x.transferSupplierCustomer in x.steps}

//ParticipantTransfer's supplier and costomer one is changed to set and add facts below:
//fact {all x: ParticipantTransfer | #(x.customer) = 1}
//fact {all x: ParticipantTransfer | #(x.supplier) = 1}

/* note
fact {all x: Supplier | no x.inputs}
is considered as same as below in junit test
fact {all x: Supplier | no (x.inputs)}
*/
/* note
fact {all x: Customer | no x.outputs}
is considered as same as below in junit test
fact {all x: Customer | no (x.outputs)}
*/

//Sign ParticipantTransfer.supper and customer multiplecity is change from 1 to 0..*
