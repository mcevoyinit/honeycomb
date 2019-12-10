package io.honeycomb.webserver.areas.assets

data class IssueAssetInputDto(
    val name: String,
    val value: Long,
    val timeLockInSeconds: Long,
    val offset : Long,
    val reference: String
)

data class AssetOutputDto(
    val name: String,
    val owner: String,
    val newOwner: String,
    val status: String,
    val value: Long,
    val reference: String,
    val expiryDate: Long,
    val offset : Long
)

data class AssetTransactionOutputDto(
    val transactionId: String
)

data class LockAssetInputDto(
    val name: String,
    val newOwner: String,
    val expiryTime: Long,
    val offset : Long,
    val reference: String
)

data class LockTransactionOutputDto(
    val transactionId: String
)

data class UnlockAssetInputDto(
    val name: String,
    val newOwner: String,
    val reference: String
)

data class UnlockTransactionOutputDto(
    val transactionId: String
)
