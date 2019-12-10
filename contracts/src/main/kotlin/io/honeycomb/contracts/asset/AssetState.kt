package io.honeycomb.contracts.asset

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.Instant

@BelongsToContract(AssetContract::class)
data class AssetState(val name : String,
                      val owner: Party,
                      val newOwner : Party,
                      val status : LockStatus,
                      val value: Long, // amount of currency i.e 50 $
                      val reference: UniqueIdentifier,
                      val expiryDate : Instant,
                      val offset : Long,
                      override val participants: List<AbstractParty> = listOf(newOwner, owner)) : ContractState

@CordaSerializable
enum class LockStatus { LOCKED, UNLOCKED }