package io.honeycomb.contracts.asset

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.Instant

@BelongsToContract(AssetContract::class)
data class AssetState(val owner: Party,
                      val counterparties: MutableList<Party>,
                      val status : LockStatus,
                      val amount: Long,
                      val reference: UniqueIdentifier,
                      val expiryDate : Instant,
                      val offset : Instant,
                      override val participants: List<AbstractParty> = counterparties + owner) : ContractState

@CordaSerializable
enum class LockStatus { LOCKED, UNLOCKED }