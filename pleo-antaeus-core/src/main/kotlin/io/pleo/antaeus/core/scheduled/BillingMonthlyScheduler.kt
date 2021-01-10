package io.pleo.antaeus.core.scheduled

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Invoice
import java.util.*
import kotlin.concurrent.schedule

class BillingMonthlyScheduler (
    private val billingService: BillingService,
    private val invoiceService: InvoiceService,
    private val scheduleProvider: ScheduleProvider
) : Scheduler {

    override fun schedule(startDay: Int) {

        val scheduledDate = scheduleProvider.getForNow(startDay)

        if (scheduledDate != null) {
            Timer("MonthlyPayment", true)
                .schedule(scheduledDate) {
                    process()
                    Thread.sleep(1000)
                    schedule(startDay)
                }
        }
    }

    private fun process() {
        val unpaid = invoiceService.fetchForProcessing()
        val paidIds = billingService.billInvoices(unpaid)
        invoiceService.markInvoicesAsPaid(paidIds)
        invoiceService.markInvoicesAsPending(
            unpaid.filter { !paidIds.contains(it.id) }
                .map(Invoice::id)
        )
    }

}

