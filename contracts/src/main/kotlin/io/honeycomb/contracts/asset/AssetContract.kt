package io.honeycomb.contracts.asset

import io.honeycomb.contracts.payment.ReceiptState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.time.Instant

open class AssetContract: Contract {
    open class Commands : CommandData, TypeOnlyCommandData() {
        class Issue : Commands()
        class Lock : Commands()
        class Unlock : Commands()
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {
            is Commands.Issue -> verifyIssue(tx, command)
            is Commands.Lock -> verifyLock(tx, command)
            is Commands.Unlock -> verifyUnlock(tx, command)
            else         -> throw IllegalArgumentException("Unsupported command ${command.value}")
        }
    }
    open fun verifyIssue(tx: LedgerTransaction, command: CommandWithParties<Commands>) = requireThat {
        val assetState = tx.outputsOfType<AssetState>().single()

        "Asset must have a value greater then zero" using ( assetState.value > 0L)

    }

    open fun verifyLock(tx: LedgerTransaction, command: CommandWithParties<Commands>) = requireThat {
        val outputAssetState = tx.outputsOfType<AssetState>().single()
        val inputAssetState = tx.inputsOfType<AssetState>().single()

        "status changed to LOCKED" using (outputAssetState.status == LockStatus.LOCKED && inputAssetState.status == LockStatus.UNLOCKED)

        "owner and prospective new owner are not the same network party" using (outputAssetState.newOwner != inputAssetState.owner)

    }

    open fun verifyUnlock(tx: LedgerTransaction, command: CommandWithParties<Commands>) = requireThat {
        val assetState = tx.outputsOfType<AssetState>().single()
        val receiptState = tx.inputsOfType<ReceiptState>().single()

        "Status must be changed to UNLOCKED" using ( assetState.status == LockStatus.UNLOCKED )

        "Receipt reference must match that of the asset" using ( assetState.reference == receiptState.reference)

        "Amount paid for asset per receipt must match the assets specified value" using ( assetState.value == receiptState.amount )

        "Asset cannot be claimed by buyer as the expiry date has passed. Offset considered" using (assetState.expiryDate.plusSeconds(assetState.offset).isAfter(Instant.now()))

    }
}