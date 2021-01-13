package io.pleo.antaeus.models

import org.joda.time.DateTime

data class BillingLog (
    val id: Int,
    val customerId: Int,
    val invoiceId: Int,
    val chargedAmount: Money,
    val billingStatus: BillingStatus,
    val billingDate: DateTime
)