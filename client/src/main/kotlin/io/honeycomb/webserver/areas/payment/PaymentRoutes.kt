package io.honeycomb.webserver.areas.payment

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import net.corda.core.messaging.CordaRPCOps
import io.honeycomb.workflows.payment.PaymentFlow
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import net.corda.core.contracts.UniqueIdentifier
import java.util.*

/**
 * Defines the payment routes for the web server.
 */

fun Route.paymentRoutes(rpc: CordaRPCOps) = route("/payment") {

    post("/perform") {
        try {
            val dto = call.receive<PaymentInputDto>()
            val receiver = rpc.wellKnownPartyFromX500Name(CordaX500Name.parse(dto.receiver!!))!!
            val message = rpc.startFlow(::PaymentFlow,
                dto.amount,dto.currency,receiver,UniqueIdentifier(null, UUID.fromString(dto.reference))).returnValue.getOrThrow()
            call.respond(HttpStatusCode.Created, message)
        } catch (ex: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("errorMessage" to ex.message))
        }
    }

}