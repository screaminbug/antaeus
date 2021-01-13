package io.pleo.antaeus.core.services

import io.mockk.*
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.util.getProcessingInvoices
import io.pleo.antaeus.models.BillingStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class BillingServiceTest {
    private val paymentProviderMock = mockk<PaymentProvider>()
    private val currencyConversionServiceMock = mockk<CurrencyConversionService>()
    private val billingLogServiceMock = mockk<BillingLogService>()

    @Test
    fun `given correct currency for customer and billing service charged, should return invoice ids` () {
        val range = (1..10)
        val invoices = getProcessingInvoices(range)
        val statusSlots = mutableListOf<BillingStatus>()

        invoices.forEach {
            every { currencyConversionServiceMock.checkAndConvert(it) } returns it
            every { billingLogServiceMock.recordBilling(it, capture(statusSlots)) } just Runs
        }
        every { paymentProviderMock.charge(any()) } returns true

        val paidIds = BillingService(
            paymentProviderMock,
            currencyConversionServiceMock,
            billingLogServiceMock
        ).billInvoices(invoices)

        assertEquals(range.toSet(), paidIds)
        assertEquals(Collections.singleton(BillingStatus.ACCEPTED), statusSlots.toSet())

    }

    @Test
    fun `given correct currency for customer, should return invoice ids only for charged invoices` () {
        val rangeCharged = (1..10)
        val rangeDeclined = (11..20)
        val invoicesToCharge = getProcessingInvoices(rangeCharged)
        val invoicesToDecline = getProcessingInvoices(rangeDeclined)
        val invoices = listOf(invoicesToCharge, invoicesToDecline).flatten()

        val statusSlots = mutableListOf<BillingStatus>()

        invoices.forEach {
            every { currencyConversionServiceMock.checkAndConvert(it) } returns it
            every { billingLogServiceMock.recordBilling(it, capture(statusSlots)) } just Runs
        }
        invoicesToCharge.forEach {
            every { paymentProviderMock.charge(it) } returns true
        }
        invoicesToDecline.forEach {
            every { paymentProviderMock.charge(it) } returns false
        }

        val paidIds = BillingService(
            paymentProviderMock,
            currencyConversionServiceMock,
            billingLogServiceMock
        ).billInvoices(invoices)

        assertEquals(rangeCharged.toSet(), paidIds)
        assertEquals(setOf(BillingStatus.ACCEPTED, BillingStatus.DECLINED), statusSlots.toSet())

        verify (exactly = 20) {
            billingLogServiceMock.recordBilling(any(), capture(statusSlots))
        }

    }

    @Test
    fun `given service throws exception, should handle and still return all charged invoices` () {
        val range = (1..20)
        val invoices = getProcessingInvoices(range)

        val statusSlots = mutableListOf<BillingStatus>()

        invoices.forEach {
            if (it.id == 5) {
                every { currencyConversionServiceMock.checkAndConvert(it) } throws CustomerNotFoundException(it.customerId)
            } else {
                every { currencyConversionServiceMock.checkAndConvert(it) } returns it
            }
            when (it.id) {
                9 -> every { paymentProviderMock.charge(it) } throws CustomerNotFoundException(it.customerId)
                13 -> every { paymentProviderMock.charge(it) } throws CurrencyMismatchException(it.id, it.customerId)
                17 -> every { paymentProviderMock.charge(it) } throws NetworkException()
                19 -> every { paymentProviderMock.charge(it) } throws Exception()
                else -> every { paymentProviderMock.charge(it) } returns true
            }
            every { billingLogServiceMock.recordBilling(it, capture(statusSlots)) } just Runs
        }

        val paidIds = BillingService(
            paymentProviderMock,
            currencyConversionServiceMock,
            billingLogServiceMock
        ).billInvoices(invoices)

        assertEquals(
            range.filter { !setOf(5, 9, 13, 17, 19).contains(it) }.toSet()
          , paidIds
        )

        assertEquals(
            setOf(
                BillingStatus.ACCEPTED,
                BillingStatus.UNKNOWN_USER,
                BillingStatus.UNSUPPORTED_CURRENCY,
                BillingStatus.COMMUNICATION_PROBLEM,
                BillingStatus.GENERAL_FAILURE
            )
          , statusSlots.toSet()
        )
    }

}