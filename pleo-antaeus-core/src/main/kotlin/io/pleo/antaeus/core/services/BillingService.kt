package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.BillingStatus
import io.pleo.antaeus.models.Invoice

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val conversionService: CurrencyConversionService,
    private val dal: AntaeusDal
) {
    /**
     * Process pending invoices
     * @param invoices: invoices that need to be charged
     *
     * @return list of invoice IDs that have been charged
     *
     */
    fun billInvoices(invoices: List<Invoice>): List<Int> {

        val paid = ArrayList<Int>()

        invoices.parallelStream().forEach {
            try {
                val invoiceWithConvertedCurrency = conversionService.checkAndConvert(it)
                val isSuccessful = paymentProvider.charge(invoiceWithConvertedCurrency)

                if (isSuccessful) {
                    recordBilling(it, BillingStatus.ACCEPTED)
                    paid.add(it.id)
                } else {
                    recordBilling(it, BillingStatus.DECLINED)
                }

                val status =
                    if (isSuccessful) BillingStatus.ACCEPTED
                    else BillingStatus.DECLINED

                recordBilling(it, status)
            } catch (e: CustomerNotFoundException) {
                recordBilling(it, BillingStatus.UNKNOWN_USER)
            } catch (e: CurrencyMismatchException) {
                recordBilling(it, BillingStatus.UNSUPPORTED_CURRENCY)
            } catch (e: NetworkException) {
                recordBilling(it, BillingStatus.COMMUNICATION_PROBLEM)
            } catch (e: Throwable) {
                recordBilling(it, BillingStatus.GENERAL_FAILURE)
            }
        }

        return paid
    }

    private fun recordBilling(invoice: Invoice, status: BillingStatus) {
        dal.createBilling(
            customerId = invoice.customerId,
            invoiceId = invoice.id,
            chargedAmount = invoice.amount,
            billingStatus = status
        )
    }

}

