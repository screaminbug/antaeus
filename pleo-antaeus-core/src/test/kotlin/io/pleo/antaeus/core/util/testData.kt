package io.pleo.antaeus.core.util

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import java.math.BigDecimal
import java.util.stream.Collectors

internal fun getProcessingInvoices(range: IntRange) : List<Invoice> {
    return range.map(::getProcessingInvoice).stream().collect(Collectors.toList())
}

internal fun getProcessingInvoice(id: Int) : Invoice {
    return Invoice(
        id = id,
        customerId = id+100,
        amount = Money(BigDecimal.ONE, Currency.EUR),
        status = InvoiceStatus.PROCESSING
    )
}

