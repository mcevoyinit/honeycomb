package io.honeycomb.webserver.areas.payment

import io.honeycomb.contracts.asset.AssetState
import io.honeycomb.contracts.payment.ReceiptState
import io.honeycomb.webserver.areas.assets.LockTransactionOutputDto
import io.honeycomb.webserver.toDto
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import net.corda.core.messaging.CordaRPCOps
import io.honeycomb.workflows.payment.PaymentFlow
import io.ktor.routing.get
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import java.util.*

/**
 * Defines the payment routes for the web server.
 */

fun Route.paymentRoutes(rpc: CordaRPCOps) = route("/payment") {

    get ("/receipt"){
        val id = call.parameters["id"]

        val criteria = if (id != null) {
            val linearId = UniqueIdentifier.fromString(id)
            QueryCriteria.LinearStateQueryCriteria(
                linearId = listOf(linearId),
                status = Vault.StateStatus.ALL
            )
        } else {
            QueryCriteria.VaultQueryCriteria()
        }

        val receipts = rpc
            .vaultQueryByCriteria(criteria, ReceiptState::class.java)
            .states
            .map { it.state.data.toDto() }

        call.respond(mapOf("receipts" to receipts))
    }

    post("/perform") {
        try {
            val dto = call.receive<PaymentInputDto>()
            val receiver = rpc.wellKnownPartyFromX500Name(CordaX500Name.parse(dto.receiver!!))!!
            val transaction = rpc.startFlow(::PaymentFlow,
                dto.amount,dto.currency,receiver,UniqueIdentifier(null, UUID.fromString(dto.reference))).returnValue.getOrThrow()
            call.respond(PaymentTransactionOutputDto(transaction.id.toString()))
        } catch (ex: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("errorMessage" to ex.message))
        }
    }
}