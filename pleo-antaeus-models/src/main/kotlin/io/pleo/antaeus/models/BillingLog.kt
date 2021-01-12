package io.pleo.antaeus.models

data class BillingLog (
    val id: Int,
    val customerId: Int,
    val invoiceId: Int,
    val chargedAmount: Money,
    val billingStatus: BillingStatus
)