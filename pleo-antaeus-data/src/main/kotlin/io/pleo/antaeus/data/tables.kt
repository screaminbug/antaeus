/*
    Defines database tables and their schemas.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import org.jetbrains.exposed.sql.CurrentDateTime
import org.jetbrains.exposed.sql.Table

object InvoiceTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val currency = varchar("currency", 3)
    val value = decimal("value", 1000, 2)
    val customerId = reference("customer_id", CustomerTable.id)
    val status = text("status")
}

object CustomerTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val currency = varchar("currency", 3)
}

object BillingTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val customerId = reference("customer_id", CustomerTable.id)
    val invoiceId = reference("invoice_id", InvoiceTable.id)
    val currency = varchar("currency", 3)
    val amount = decimal("value", 1000, 2)
    val billingStatus = text("status")
    val chargeAttempTimestamp = datetime("charge_attempt").defaultExpression(CurrentDateTime())
}
