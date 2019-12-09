package io.honeycomb.webserver.areas.assets

data class IsuueAssetInputDto(
    val name: String,
    val value: Long,
    val timeLockInSeconds: Long,
    val offset : Long,
    val reference: String
)

data class LockAssetInputDto(
    val name: String,
    val newOwner: String,
    val expiryTime: Long,
    val offset : Long,
    val reference: String
)

data class UnlockAssetInputDto(
    val name: String,
    val newOwner: String,
    val reference: String
)