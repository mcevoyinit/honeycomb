package io.honeycomb.webserver.areas.tokens

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import io.honeycomb.workflows.tokens.IssueTokensFlow

fun Route.tokenRoutes(rpc: CordaRPCOps) = route("/token") {

    post("/issue") {
        try {
            val dto = call.receive<IssueTokensInputDto>()
            val receiver = rpc.wellKnownPartyFromX500Name(CordaX500Name.parse(dto.receiver!!))!!
            val message = rpc.startFlow(::IssueTokensFlow,
                dto.amount,dto.currency,receiver).returnValue.getOrThrow()
            call.respond(HttpStatusCode.Created, message)
        } catch (ex: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("errorMessage" to ex.message))
        }
    }

}