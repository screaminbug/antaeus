package io.pleo.antaeus.core.scheduled

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import java.util.Calendar.MILLISECOND
import java.util.Calendar.getInstance
import java.util.stream.Collectors

internal class BillingMonthlySchedulerTest {
    private val invoiceServiceMock = mockk<InvoiceService>()
    private val billingServiceMock = mockk<BillingService>()
    private val scheduleProviderMock = mockk<ScheduleProvider>()
    private val dummyAmount = Money(BigDecimal.ONE, Currency.DKK)

    @Test
    fun `test scheduler`() {
        val allPendingIds = (1..10).map {
            Invoice(
                id = it,
                customerId = it+100,
                amount = dummyAmount,
                status = InvoiceStatus.PENDING)
        }.stream().collect(Collectors.toList())

        val expectedPaidIds = listOf(1, 2, 3, 4, 9, 10)
        val expectedUnpaidIds = listOf(5, 6, 7, 8)

        val pendingIdsSlot = slot<List<Int>>()
        val paidIdsSlot = slot<List<Int>>()

        every { invoiceServiceMock.fetchForProcessing() } returns allPendingIds
        every { invoiceServiceMock.markInvoicesAsPending(capture(pendingIdsSlot)) } returns Unit
        every { invoiceServiceMock.markInvoicesAsPaid(capture(paidIdsSlot)) } returns Unit
        every { billingServiceMock.billInvoices(any()) } returns listOf(1, 2, 3, 4)

        val now = getInstance()

        every { scheduleProviderMock.getForNow(any()) } returns
                delay(now, amount = 1) andThen delay(now, amount = 2) andThen delay(now, amount = 3)   andThen null

        BillingMonthlyScheduler(
            billingService = billingServiceMock,
            invoiceService = invoiceServiceMock,
            scheduleProvider = scheduleProviderMock
        ).schedule(1)

        // TODO: can't test it like this, schedule() returns immediately

        assertEquals(expectedPaidIds, paidIdsSlot.captured)
        assertEquals(expectedUnpaidIds, paidIdsSlot.captured)
    }

    private fun delay(start: Calendar, amount: Int): Date {
        start.add(MILLISECOND, amount * 1000)
        return start.time
    }


}

