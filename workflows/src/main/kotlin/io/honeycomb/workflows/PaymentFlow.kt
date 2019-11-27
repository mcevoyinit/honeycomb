package io.honeycomb.workflows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveFungibleTokens
import io.honeycomb.contracts.payment.ReceiptContract
import io.honeycomb.contracts.payment.ReceiptState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// ************************************
// * Payment Flow
// * Transaction 2 as per payment design
// ************************************

@InitiatingFlow
@StartableByRPC
@SchedulableFlow
class PaymentFlow(val amount: Long,
                  val currency: String,
                  val receiver: Party,
                  val reference: UniqueIdentifier
) : FlowLogic<SignedTransaction>() {

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        val ourIdentity = serviceHub.myInfo.legalIdentities.first()

        // Create the transaction ReceiptState
        val receiptState = ReceiptState(creator = ourIdentity,
            receiver = receiver,
            amount = amount,
            reference = reference)

        // Receipt Create Command
        val receiptCommand = ReceiptContract.Commands.Payment()

        // Build transaction
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val txBuilder = with(TransactionBuilder(notary)) {
            addOutputState(receiptState)
            addCommand(receiptCommand, ourIdentity.owningKey, receiver.owningKey)
        }

        // Create the amount of token type to be transferred
        val tokenType = FiatCurrency.getInstance(currency)
        val amountOfToken: Amount<TokenType> = Amount(amount, tokenType)

        // adding fungible tokens to the builder
        addMoveFungibleTokens(transactionBuilder = txBuilder,
            serviceHub = serviceHub,
            amount = amountOfToken,
            holder = receiver,
            changeHolder = ourIdentity)

        // verify
        txBuilder.verify(serviceHub)

        // sign and gather signatures
        val pstx = serviceHub.signInitialTransaction(txBuilder)
        val session = initiateFlow(receiver)
        val stx = subFlow(CollectSignaturesFlow(pstx, listOf(session)))

        // Finalise
        return subFlow((FinalityFlow(stx, session)))
    }
}

@InitiatedBy(PaymentFlow::class)
class PaymentResponderFlow(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
            override fun checkTransaction(stx: SignedTransaction) {
                val output = stx.tx.outputStates.filterIsInstance(FungibleToken::class.java)
                val myIdentity = serviceHub.myInfo.legalIdentities.first()
                requireThat {
                    "$myIdentity is the new holder of tokens" using (output.any {
                        it.holder == myIdentity
                    })
                }
            }
        }
        val txId = subFlow(signTransactionFlow).id
        return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
    }
}