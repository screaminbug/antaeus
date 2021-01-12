package io.pleo.antaeus.core.services

import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.BillingStatus
import io.pleo.antaeus.models.Invoice

class BillingLogService (
    private val dal: AntaeusDal
) {
    fun recordBilling(invoice: Invoice, status: BillingStatus) {
        dal.createBillingLog(
            customerId = invoice.customerId,
            invoiceId = invoice.id,
            chargedAmount = invoice.amount,
            billingStatus = status
        )
    }
}