package io.honeycomb.workflows.asset

import co.paralleluniverse.fibers.Suspendable
import io.honeycomb.contracts.asset.AssetContract
import io.honeycomb.contracts.asset.AssetState
import io.honeycomb.contracts.asset.LockStatus
import io.honeycomb.contracts.payment.ReceiptContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.Instant

@InitiatingFlow
@StartableByRPC
class LockAssetFlow(val name : String,
                    val newOwner : Party,
                    val expiryTime : Long,
                    val offset : Long,
                    val reference : UniqueIdentifier) : FlowLogic<SignedTransaction>() {

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
        val input = serviceHub.vaultService.queryBy<AssetState>().states.first { it.state.data.name == name }
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val lockCommand = AssetContract.Commands.Lock()
        val receiptCommand = ReceiptContract.Commands.Claim()

        val output =  input.state.data.copy(
            owner = ourIdentity,
            status = LockStatus.LOCKED,
            newOwner = newOwner,
            expiryDate = Instant.now().plusSeconds(expiryTime),
            offset = offset,
            reference = reference,
            participants = listOf(ourIdentity, newOwner)
        )

        // Build transaction
        val txBuilder = with(TransactionBuilder(notary)) {
            addInputState(input)
            addOutputState(output)
            addCommand(lockCommand, ourIdentity.owningKey)
            addCommand(receiptCommand, ourIdentity.owningKey)
        }
        // verify
        txBuilder.verify(serviceHub)

        val session = initiateFlow(newOwner)

        // sign and gather signatures
        val pstx = serviceHub.signInitialTransaction(txBuilder)

        // Finalise
        return subFlow((FinalityFlow(pstx, listOf(session))))
    }

}
@InitiatedBy(LockAssetFlow::class)
open class LockAssetFlowResponder(private val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        return subFlow(ReceiveFinalityFlow(otherPartySession))
    }
}
