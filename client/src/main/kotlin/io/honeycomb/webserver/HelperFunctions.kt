package io.honeycomb.webserver

import io.honeycomb.contracts.asset.AssetState
import io.honeycomb.contracts.payment.ReceiptState
import io.honeycomb.webserver.areas.assets.AssetOutputDto
import io.honeycomb.webserver.areas.payment.PaymentOutputDto

fun AssetState.toDto() = AssetOutputDto(
    name = name,
    owner = owner.name.toString(),
    newOwner = newOwner.name.toString(),
    status = status.name.toString(),
    value = value,
    reference = reference.toString(),
    expiryDate = expiryDate.epochSecond,
    offset = offset
)

fun ReceiptState.toDto() = PaymentOutputDto(
    creator = creator.name.toString() ,
    receiver= receiver.name.toString(),
    amount=amount,
    reference = reference.toString()
)
