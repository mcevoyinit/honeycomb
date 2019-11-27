package io.honeycomb.contracts.payment

import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.CommandWithParties
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

open class ReceiptContract : Contract {

    open class Commands : CommandData, TypeOnlyCommandData() {
        class Payment : Commands()
        class Claim : Commands()
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {
            is Commands.Payment -> verifyPayment(tx, command)
            is Commands.Claim -> verifyClaim(tx, command)
            else         -> throw IllegalArgumentException("Unsupported command ${command.value}")
        }
    }

    open fun verifyPayment(tx: LedgerTransaction, command: CommandWithParties<Commands>) = requireThat {

        // Tokens
        val inputTokens = tx.inputsOfType<FungibleToken>()
        val outputTokens = tx.outputsOfType<FungibleToken>()
        "There should only be less than or equal to 2  input fungible tokens" using (inputTokens.size <= 2)

        // Receipt accurately reflects what is being transferred
        val receiptState = tx.outputsOfType<ReceiptState>().single()
        "Some output fungible token must have a the same holder as the the Receipt State as well as the same amount" using (outputTokens.any { it.holder == receiptState.receiver && it.amount.quantity == receiptState.amount })
    }

    open fun verifyClaim(tx: LedgerTransaction, command: CommandWithParties<Commands>) = requireThat {

    }
}
