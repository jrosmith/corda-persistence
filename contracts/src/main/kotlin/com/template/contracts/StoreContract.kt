package com.template.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction
import java.lang.IllegalArgumentException

// ************
// * Contract *
// ************
class StoreContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        @JvmStatic
        val STORE_CONTRACT_ID = StoreContract::class.qualifiedName!!
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val cmd = tx.commands.requireSingleCommand<Commands>()

        when (cmd.value) {
            is Commands.Save -> true
            else -> throw IllegalArgumentException("Unrecognized command: ${cmd.value}")
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Save : TypeOnlyCommandData(), Commands
    }
}