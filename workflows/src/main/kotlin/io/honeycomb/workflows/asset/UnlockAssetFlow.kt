package io.honeycomb.workflows.asset

import co.paralleluniverse.fibers.Suspendable
import io.honeycomb.contracts.asset.AssetContract
import io.honeycomb.contracts.asset.AssetState
import io.honeycomb.contracts.asset.LockStatus
import io.honeycomb.contracts.payment.ReceiptContract
import io.honeycomb.contracts.payment.ReceiptState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.internal.FlowIORequest
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.Instant

@InitiatingFlow
@StartableByRPC
class UnlockAssetFlow(val name : String,
                      val newOwner : Party,
                      val paymentReference : UniqueIdentifier) : FlowLogic<SignedTransaction>() {

    @Suppress("ClassName")
    companion object {
        object GETTING_IDENTITIES : ProgressTracker.Step("Getting ours and recipients identity")
        object PARSING_CURRENCY : ProgressTracker.Step("Parsing targetCurrency to issue")
        object ISSUING_TOKENS : ProgressTracker.Step("Issuing tokens to recipient")
    }

    override val progressTracker = ProgressTracker(
        GETTING_IDENTITIES,
        PARSING_CURRENCY,
        ISSUING_TOKENS
    )

    @Suspendable
    override fun call(): SignedTransaction {
        val inputAssetState = serviceHub.vaultService.queryBy<AssetState>().states.first { it.state.data.name == name }
        val receiptState = serviceHub.vaultService.queryBy<ReceiptState>().states.first { it.state.data.reference == paymentReference }
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val lockCommand = AssetContract.Commands.Lock()
        val receiptCommand = ReceiptContract.Commands.Claim()

        val outputAssetState =  inputAssetState.state.data.copy(
            owner = ourIdentity,
            counterparties = listOf(newOwner).toMutableList(),
            status = LockStatus.UNLOCKED,
            newOwner = ourIdentity,
            expiryDate = Instant.MAX,
            offset = 0L,
            reference = paymentReference
        )

        // Build transaction
        val txBuilder = with(TransactionBuilder(notary)) {
            addInputState(receiptState)
            addInputState(inputAssetState)
            addOutputState(outputAssetState)
            addCommand(lockCommand, ourIdentity.owningKey)
            addCommand(receiptCommand, ourIdentity.owningKey)
        }
        // verify
        txBuilder.verify(serviceHub)

        // sign and gather signatures
        val pstx = serviceHub.signInitialTransaction(txBuilder)

        // Finalise
        return subFlow((FinalityFlow(pstx, emptyList())))
    }
}
@InitiatedBy(UnlockAssetFlow::class)
open class UnlockAssetFlowResponder(private val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        return subFlow(ReceiveFinalityFlow(otherPartySession))
    }
}
