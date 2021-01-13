package io.pleo.antaeus.core.job

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Invoice

class BillingJob(
    private val invoiceService: InvoiceService,
    private val billingService: BillingService
): Runnable {

    override fun run() {
        val unpaid = invoiceService.fetchForProcessing()
        val paidIds = billingService.billInvoices(unpaid)
        invoiceService.markInvoicesAsPaid(paidIds)
        invoiceService.markInvoicesAsPending(
            unpaid.filter { !paidIds.contains(it.id) }
                .map(Invoice::id)
        )
    }

}
