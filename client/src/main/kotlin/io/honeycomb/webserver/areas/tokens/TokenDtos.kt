package io.honeycomb.webserver.areas.tokens

data class IssueTokensInputDto(
    val amount: Long,
    val currency: String,
    val receiver: String
)

data class TokenTransactionOutputDto(
    val transactionId: String
)