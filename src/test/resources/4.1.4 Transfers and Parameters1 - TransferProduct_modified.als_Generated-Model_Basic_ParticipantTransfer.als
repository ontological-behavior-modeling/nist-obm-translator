// This file is created with code.

module ParticipantTransfer
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

// Signatures:
sig ParticipantTransfer extends Occurrence { customer: set Customer, supplier: set Supplier, transferSupplierCustomer: set Transfer }
sig Supplier extends Occurrence { suppliedProduct: set Product }
sig Product extends Occurrence {}
sig Customer extends Occurrence { receivedProduct: set Product }

// Facts:
fact {all x: ParticipantTransfer | #(x.supplier) = 1}
fact {all x: ParticipantTransfer | #(x.customer) = 1}
fact {all x: Supplier | #(x.suppliedProduct) = 1}
fact {all x: Customer | #(x.receivedProduct) = 1}
fact {all x: Supplier | x.suppliedProduct in x.outputs}
fact {all x: Supplier | x.outputs in x.suppliedProduct}
fact {all x: Customer | x.receivedProduct in x.inputs}
fact {all x: Customer | x.inputs in x.receivedProduct}
fact {all x: ParticipantTransfer | bijectionFiltered[sources, x.transferSupplierCustomer, x.supplier]}
fact {all x: ParticipantTransfer | bijectionFiltered[targets, x.transferSupplierCustomer, x.customer]}
fact {all x: ParticipantTransfer | subsettingItemRuleForSources[x.transferSupplierCustomer]}
fact {all x: ParticipantTransfer | subsettingItemRuleForTargets[x.transferSupplierCustomer]}
fact {all x: ParticipantTransfer | x.customer + x.supplier + x.transferSupplierCustomer in x.steps}
fact {all x: ParticipantTransfer | x.steps in x.customer + x.supplier + x.transferSupplierCustomer}
fact {all x: Customer | no (x.outputs)}
fact {all x: Supplier | no (x.inputs)}

