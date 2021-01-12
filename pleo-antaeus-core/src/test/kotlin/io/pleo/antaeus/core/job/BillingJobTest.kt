package io.pleo.antaeus.core.job

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.stream.Collectors

internal class BillingJobTest {
    private val invoiceServiceMock = mockk<InvoiceService>()
    private val billingServiceMock = mockk<BillingService>()
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
        val chargedIdsSlot = slot<List<Int>>()

        every { invoiceServiceMock.fetchForProcessing(100) } returns allPendingIds
        every { invoiceServiceMock.markInvoicesAsPending(capture(pendingIdsSlot)) } returns Unit
        every { invoiceServiceMock.markInvoicesAsPaid(capture(chargedIdsSlot)) } returns Unit
        every { billingServiceMock.billInvoices(any()) } returns expectedPaidIds

        BillingJob(
            billingService = billingServiceMock,
            invoiceService = invoiceServiceMock,
            batchSize = 100
        ).run()

        assertEquals(expectedPaidIds, chargedIdsSlot.captured)
        assertEquals(expectedUnpaidIds, pendingIdsSlot.captured)
    }

}

