package io.pleo.antaeus.core.scheduled

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Invoice
import java.util.*
import kotlin.concurrent.schedule

class BillingMonthlyScheduler (
    private val billingService: BillingService,
    private val invoiceService: InvoiceService
) : Scheduler {
    override fun schedule(startDay: Int) {

        val time = calculateNextDayInMonth(startDay)
        val period = calculateNextPeriod(time)

        Timer("MonthlyPayment", true)
            .schedule(time, period) {
                val unpaid = invoiceService.fetchForProcessing()
                val paidIds = billingService.billInvoices(unpaid, 3, 10)
                invoiceService.markInvoicesAsPaid(paidIds)
                invoiceService.markInvoicesAsPending(
                    unpaid.filter { !paidIds.contains(it.id) }
                          .map(Invoice::id)
                )
            }
    }

    private fun calculateNextDayInMonth(startDay: Int): Date {
        TODO("Not yet implemented")
    }

    private fun calculateNextPeriod(time: Date): Long {
        TODO("Not yet implemented")
    }

}