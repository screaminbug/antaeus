package io.pleo.antaeus.core.job

import io.mockk.*
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.util.getProcessingInvoices
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BillingJobTest {
    private val invoiceServiceMock = mockk<InvoiceService>()
    private val billingServiceMock = mockk<BillingService>()

    @Test
    fun `billing job test`() {
        val allPendingInvoices = getProcessingInvoices((1..10))

        val expectedPaidIds = setOf(1, 2, 3, 4, 9, 10)
        val expectedUnpaidIds = listOf(5, 6, 7, 8)

        val pendingIdsSlot = slot<List<Int>>()
        val chargedIdsSlot = slot<Set<Int>>()

        every { invoiceServiceMock.fetchForProcessing(100) } returns allPendingInvoices
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

