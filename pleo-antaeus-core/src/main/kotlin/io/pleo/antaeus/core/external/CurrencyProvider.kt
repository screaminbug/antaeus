package io.pleo.antaeus.core.external

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Money

interface CurrencyProvider {
    fun convert(toCurrency: Currency, amount: Money): Money
}