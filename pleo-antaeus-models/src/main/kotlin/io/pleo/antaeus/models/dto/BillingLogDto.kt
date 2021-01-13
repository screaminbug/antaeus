package io.pleo.antaeus.models.dto

import io.pleo.antaeus.models.Money

class BillingLogDto (
    val customerId: Int,
    val invoiceId: Int,
    val amount: String,
    val currency: String,
    val billingStatus: String,
    val billingDate: String
)
