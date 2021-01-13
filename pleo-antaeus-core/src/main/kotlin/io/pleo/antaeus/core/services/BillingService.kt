package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.BillingStatus
import io.pleo.antaeus.models.Invoice

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val conversionService: CurrencyConversionService,
    private val billingLogService: BillingLogService
) {
    /**
     * Process pending invoices
     * @param invoices: invoices that need to be charged
     *
     * @return set of invoice IDs that have been charged
     *
     */
    fun billInvoices(invoices: List<Invoice>): Set<Int> {

        val paid = HashSet<Int>()

        invoices.parallelStream().forEach {
            try {
                val invoiceWithConvertedCurrency = conversionService.checkAndConvert(it)
                val isSuccessful = paymentProvider.charge(invoiceWithConvertedCurrency)

                if (isSuccessful) {
                    billingLogService.recordBilling(it, BillingStatus.ACCEPTED)
                    paid.add(it.id)
                } else {
                    billingLogService.recordBilling(it, BillingStatus.DECLINED)
                }

            } catch (e: CustomerNotFoundException) {
                billingLogService.recordBilling(it, BillingStatus.UNKNOWN_USER)
            } catch (e: CurrencyMismatchException) {
                billingLogService.recordBilling(it, BillingStatus.UNSUPPORTED_CURRENCY)
            } catch (e: NetworkException) {
                billingLogService.recordBilling(it, BillingStatus.COMMUNICATION_PROBLEM)
            } catch (e: Throwable) {
                billingLogService.recordBilling(it, BillingStatus.GENERAL_FAILURE)
            }
        }

        return paid
    }



}

