package io.honeycomb.webserver.areas.payment

data class PaymentInputDto(
    val amount: Long,
    val currency: String,
    val receiver: String,
    val reference: String
)

data class PaymentOutputDto(
    val creator: String,
    val receiver: String,
    val amount: Long,
    val reference: String
    )

data class PaymentTransactionOutputDto(
    val transactionId: String
)