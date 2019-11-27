package io.honeycomb.contracts.asset

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

open class AssetContract: Contract {
    open class Commands : CommandData, TypeOnlyCommandData() {
        class Lock : Commands()
        class Unlock : Commands()
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {
            is Commands.Lock -> verifyLock(tx, command)
            is Commands.Unlock -> verifyUnlock(tx, command)
            else         -> throw IllegalArgumentException("Unsupported command ${command.value}")
        }
    }

    open fun verifyLock(tx: LedgerTransaction, command: CommandWithParties<Commands>) = requireThat {


    }

    open fun verifyUnlock(tx: LedgerTransaction, command: CommandWithParties<Commands>) = requireThat {


    }
}