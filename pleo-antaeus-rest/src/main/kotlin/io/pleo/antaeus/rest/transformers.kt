package io.pleo.antaeus.rest

import io.pleo.antaeus.models.BillingLog
import io.pleo.antaeus.models.dto.BillingLogDto
import org.joda.time.format.DateTimeFormat

fun BillingLog.toBillingLogDto() = BillingLogDto (
    customerId = customerId,
    invoiceId = invoiceId,
    amount = chargedAmount.value.toString(),
    currency = chargedAmount.currency.toString(),
    billingStatus = billingStatus.toString(),
    billingDate = DateTimeFormat
        .forPattern("YYYY-MM-dd HH:mm:ss")
        .print(billingDate)
)

