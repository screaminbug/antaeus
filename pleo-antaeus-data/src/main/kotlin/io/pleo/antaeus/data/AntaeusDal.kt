/*
    Implements the data access layer (DAL).
    The data access layer generates and executes requests to the database.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

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

            InvoiceTable.update({InvoiceTable.status.eq(InvoiceStatus.PENDING.name)}) {
                it[this.status] = InvoiceStatus.PROCESSING.toString()
            }

            InvoiceTable
                .select(InvoiceTable.status.eq(InvoiceStatus.PROCESSING.name))
                .map {
                    it.toInvoice()
                }

        }
    }

    fun createBillingLog(customerId: Int, invoiceId: Int, chargedAmount: Money, billingStatus: BillingStatus): BillingLog? {
        val id = transaction(db) {
            BillingLogTable
                .insert {
                    it[this.customerId] = customerId
                    it[this.invoiceId] = invoiceId
                    it[this.currency] = chargedAmount.currency.name
                    it[this.amount] = chargedAmount.value
                    it[this.billingStatus] = billingStatus.name
                } get BillingLogTable.id
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


    fun updateInvoices(invoiceIds: Iterable<Int>, invoiceStatus: InvoiceStatus) {
        return transaction(db) {
            InvoiceTable
                .update({ InvoiceTable.id.inList(invoiceIds) }) {
                    it[this.status] = invoiceStatus.toString()
                }
        }
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

    private fun fetchBilling(id: Int): BillingLog? {
        return transaction(db) {
            BillingLogTable
                .select{ BillingLogTable.id.eq(id) }
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

    fun fetchBillingLog(): List<BillingLog> {
        return transaction(db) {
            BillingLogTable
                .selectAll()
                .map { it.toBilling() }
        }
    }
}
