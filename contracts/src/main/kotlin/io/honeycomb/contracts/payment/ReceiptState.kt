package io.honeycomb.contracts.payment

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(ReceiptContract::class)
data class ReceiptState(val creator: Party,
                        val receiver: Party,
                        val amount: Long,
                        val reference: UniqueIdentifier,
                        override val participants : List<AbstractParty> = listOf(creator, receiver)

) : ContractState