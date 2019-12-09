package io.honeycomb.webserver

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.honeycomb.webserver.areas.admin.adminRoutes
import io.honeycomb.webserver.areas.assets.assetRoutes
import io.honeycomb.webserver.areas.payment.paymentRoutes
import io.honeycomb.webserver.areas.attachments.attachmentRoutes
import io.honeycomb.webserver.areas.nodes.nodeRoutes
import io.honeycomb.webserver.areas.tokens.tokenRoutes
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val configuration = Configuration()
    val rpcProxy = ConnectionProvider(configuration).proxy

    embeddedServer(Netty, configuration.serverPort) {
        install(DefaultHeaders)
        install(Compression)
        install(CallLogging)
        install(ContentNegotiation) {
            jackson {
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true)
                configure(SerializationFeature.INDENT_OUTPUT, true)
                setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultIndenter("  ", "\n"))
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                })
                registerModule(JavaTimeModule())
            }
        }

        routing {
            adminRoutes(rpcProxy)
            nodeRoutes(rpcProxy)
            attachmentRoutes(rpcProxy)
            assetRoutes(rpcProxy)
            paymentRoutes(rpcProxy)
            tokenRoutes(rpcProxy)
        }
    }.start(wait = true)
}