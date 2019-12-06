package io.honeycomb.workflows.asset

import co.paralleluniverse.fibers.Suspendable
import io.honeycomb.contracts.asset.AssetContract
import io.honeycomb.contracts.asset.AssetState
import io.honeycomb.contracts.asset.LockStatus
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.time.Instant

/*
 Simple Flow to issue tokens to yourself in order to bootstapper the nodes to a state where we can begin asset lock based trading
 */

@InitiatingFlow
@StartableByRPC
class IssueAssetFlow(val name : String,
                     val value: Long,
                     val timeLockInSeconds : Long,
                     val offset : Long) : FlowLogic<SignedTransaction>() {

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

        val command = AssetContract.Commands.Issue()

        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val assetState =  AssetState(
            name = name,
            owner = ourIdentity,
            newOwner = ourIdentity,
            counterparties = emptyList<Party>().toMutableList(),
            status = LockStatus.UNLOCKED,
            value = value,
            reference = UniqueIdentifier.fromString("ASSET1"),
            expiryDate = Instant.now().plusSeconds(timeLockInSeconds), // on issue this doesn't matter
            offset = offset,
            participants = listOf(ourIdentity))

        // Build transaction
        val txBuilder = with(TransactionBuilder(notary)) {
            addOutputState(assetState)
            addCommand(command, ourIdentity.owningKey)
        }

        // verify
        txBuilder.verify(serviceHub)

        // sign and gather signatures
        val pstx = serviceHub.signInitialTransaction(txBuilder)

        // Finalise
        return subFlow((FinalityFlow(pstx, emptyList())))
    }
}



