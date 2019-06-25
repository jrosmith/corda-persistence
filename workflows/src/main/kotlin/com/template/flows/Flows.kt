package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.StoreContract
import com.template.states.StoreState
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import org.apache.commons.lang.mutable.Mutable

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class Initiator(
        private val storeName: String,
        private val productNames: MutableList<String>,
        private val recipients: List<Party>
) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        println("Creating store")

        val flowSessionsSet = recipients.map { initiateFlow(it) }.toSet()

        val txn = TransactionBuilder(notary())
                .addOutputState(
                        StoreState(
                                storeName = storeName,
                                productNames = productNames,
                                participants = recipients.plus(myIdentity())
                        )
                )
                .addCommand(
                        StoreContract.Commands.Save(),
                        myIdentity().owningKey
                )

        val stx = serviceHub.signInitialTransaction(txn)

        subFlow(FinalityFlow(stx, flowSessionsSet))
        println("Store created")
    }

    private fun notary() = serviceHub.networkMapCache.notaryIdentities.first()

    private fun myIdentity() = serviceHub.myInfo.legalIdentities.first()
}

@InitiatedBy(Initiator::class)
class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        println("Saving store")
        subFlow(ReceiveFinalityFlow(counterpartySession, statesToRecord = StatesToRecord.ALL_VISIBLE))
        println("Store saved")
    }
}
