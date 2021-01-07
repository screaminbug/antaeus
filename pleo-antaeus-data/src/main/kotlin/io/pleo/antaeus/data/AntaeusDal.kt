/*
    Implements the data access layer (DAL).
    The data access layer generates and executes requests to the database.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class AntaeusDal(private val db: Database) {
    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            // Returns the first invoice with matching id.
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    fun fetchInvoicesForProcessing(): List<Invoice> {
        return transaction(db) {

            InvoiceTable
                .select(where = InvoiceTable.status.eq(InvoiceStatus.PROCESSING.name))
                .map {
                    val invoice = it.toInvoice()
                    invoice.status = InvoiceStatus.PROCESSING
                    invoice
                }

        }
    }

    fun createBilling(customerId: Int, invoiceId: Int, chargedAmount: Money, billingStatus: BillingStatus): Billing? {
        val id = transaction(db) {
            BillingTable
                .insert {
                    it[this.customerId] = customerId
                    it[this.invoiceId] = invoiceId
                    it[this.currency] = chargedAmount.currency.name
                    it[this.amount] = chargedAmount.value
                    it[this.billingStatus] = billingStatus.name
                } get BillingTable.id
        }
        return fetchBilling(id)
    }

    fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus = InvoiceStatus.PENDING): Invoice? {
        val id = transaction(db) {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                } get InvoiceTable.id
        }

        return fetchInvoice(id)
    }


    fun updateInvoices(invoiceIds: List<Int>, paid: InvoiceStatus) {
        TODO("Not yet implemented")
    }

    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    private fun fetchBilling(id: Int): Billing? {
        return transaction(db) {
            BillingTable
                .select{ BillingTable.id.eq(id) }
                .firstOrNull()
                ?.toBilling()
        }
    }

    fun createCustomer(currency: Currency): Customer? {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id)
    }
}
