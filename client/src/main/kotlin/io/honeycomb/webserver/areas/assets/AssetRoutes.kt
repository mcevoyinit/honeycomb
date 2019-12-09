package io.honeycomb.webserver.areas.assets

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import net.corda.core.messaging.CordaRPCOps
import io.honeycomb.workflows.asset.IssueAssetFlow
import io.honeycomb.workflows.asset.LockAssetFlow
import io.honeycomb.workflows.asset.UnlockAssetFlow
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import net.corda.core.contracts.UniqueIdentifier
import io.honeycomb.contracts.asset.AssetState
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import net.corda.core.contracts.Amount
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import java.util.*

/**
 * Defines the routes routes for the web server.
 */

fun Route.assetRoutes(rpc: CordaRPCOps) = route("/assets") {

    get {
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

        //val assets = rpc
        //    .vaultQueryByCriteria(criteria, AssetState::class.java)
        //    .states
           // .map { it.state.data.toDto() }

        //call.respond(mapOf("assets" to assets))
    }

    post("/issue") {
        try {
            val dto = call.receive<IsuueAssetInputDto>()
            val message = rpc.startFlow(::IssueAssetFlow,
                dto.name,dto.value,dto.timeLockInSeconds,dto.offset,UniqueIdentifier(null, UUID.fromString(dto.reference))).returnValue.getOrThrow()
            call.respond(HttpStatusCode.Created, message)
        } catch (ex: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("errorMessage" to ex.message))
        }
    }

    post("/lock") {
        try {
            val dto = call.receive<LockAssetInputDto>()
            val newOwner = rpc.wellKnownPartyFromX500Name(CordaX500Name.parse(dto.newOwner!!))!!
            val message = rpc.startFlow(::LockAssetFlow,
                dto.name,newOwner,dto.expiryTime,dto.offset,UniqueIdentifier(null, UUID.fromString(dto.reference))).returnValue.getOrThrow()
            call.respond(HttpStatusCode.Created, message)
        } catch (ex: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("errorMessage" to ex.message))
        }
    }

    post("/unlock") {
        try {
            val dto = call.receive<UnlockAssetInputDto>()
            val newOwner = rpc.wellKnownPartyFromX500Name(CordaX500Name.parse(dto.newOwner!!))!!
            val message = rpc.startFlow(::UnlockAssetFlow,
                dto.name,newOwner,UniqueIdentifier(null, UUID.fromString(dto.reference))).returnValue.getOrThrow()
            call.respond(HttpStatusCode.Created, message)
        } catch (ex: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("errorMessage" to ex.message))
        }
    }
}