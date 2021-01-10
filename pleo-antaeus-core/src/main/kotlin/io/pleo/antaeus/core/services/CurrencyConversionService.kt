package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.CurrencyProvider
import io.pleo.antaeus.models.Invoice

class CurrencyConversionService(
    private val customerService: CustomerService,
    private val currencyProvider: CurrencyProvider
) {

    fun checkAndConvert(invoice: Invoice): Invoice {
        val customer = customerService.fetch(invoice.customerId)
        if (customer.currency == invoice.amount.currency) { return invoice }

        val convertedAmount = currencyProvider.convert(
            toCurrency = customer.currency,
            amount = invoice.amount
        )

        return Invoice(
            id = invoice.id,
            amount = convertedAmount,
            customerId = customer.id,
            status = invoice.status
        )
    }
}