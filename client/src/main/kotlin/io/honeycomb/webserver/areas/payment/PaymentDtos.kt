package io.honeycomb.webserver.areas.payment

data class PaymentInputDto(
    val amount: Long,
    val currency: String,
    val receiver: String,
    val reference: String
)