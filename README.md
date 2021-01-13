## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Brainstorming

### Currency
Each Customer has his/her own currency, which is probably the currency of their bank account, and payment processor may or may
not accept charging in foreign currency. With that and Since there is no constraint that invoice currency must match the customer currency,
a currency conversion provider must be made available to the billing service for currency conversion purposes before sending
it to the Payment Provider.

### Scheduling
Since requirement is to charge invoices on the first of the month, a some kind of scheduler will be needed.
A scheduled task will need to get all PENDING invoices and then call the Billing Service method pay with them.

### Possible pitfalls
Double charging. System must be made aware that invoice is being changed to prevent charging the customers multiple times.
This could happen if, for example, payment will be spread out over the course of day the day and is processed by multiple schedulers

Proposed solution. Add a new status of the invoice, for example PROCESSING which will prevent that from happening, since
only the PENDING statuses will be considered for sending to the billing service.

### Failure in billing service
A care needs to be taken so that charged invoices are marked as paid. If something goes wrong in the billing service
after the invoice has been successfully charged, there is a potential for invoices to get stuck in PROCESSING status.
Possible mitigations
1. Billing service should handle all exceptions and log the results for later reference

## Classes

### BillingService

This service is responsible for sending invoices to be charged by Payment Service. It also logs successful
and unsuccessful charging attempts. 
It consists of a single function ```billInvoices()``` which receives a list of invoices and returns the ids
of only changed invoices. Failures and successful processing are recorded in BillingLog

### BillingLog
This entity is mapped to Billing Log Table which is used to record information about payments. Each payment attempt is
logged, along with customer id, invoice id, currency, amount and time of the billing attempt.

### BillingJob
Runnable which is responsible for sending invoices that need to be charged to the BillingService and for updating invoice statuses

### ScheduleProvider
An interface for Schedule Providers. The job of the Schedule Provider is to provide the date when the next billing 
process will be occurring. It allows scheduling on specific day.

### MonthlyScheduleProvider
A specific ScheduleProvider which calculates next billing date based on the current date. If current day of month is after
desired day in month when the billing should occur, then it will provide date with desired day in the next month. Otherwise
it will provide the date in the same month. If the desired day is greater than maximal number of days in any given month
it will provide the last day of the month, based on rules methioned above.

### MonthlyScheduler
This class is responsible for spawning Billing Jobs. It takes next job date provided by ScheduleProvider. It will run
multiple times in a day of month that is provided as an argument to the call of schedule() function. It will not run on
other days

### CurrencyConversionService
Service which uses external CurrencyProvider to convert between currencies. If the currency od the invoice is not
the same as customer currency it will call the provider to make the conversion. 

### CurrencyProvider
An external service for currency conversion


## Operation

On application start the scheduler is started, which will kick off the billing job on the desired day of month. It will
use the InvoiceService to fetch all pending invoices. InvoiceService will fetch all PENDING invoices and in the same transaction
update them to PROCESSING status. This is to help prevent possibility for charging the same invoice twice. For example if 
some other thread calls function for fetching pending invoices while some of them are in the billing process (PaymentService might be slow).
After the eligible invoices are fetched the billing job will send them to the BillingService which will take care of
sending them to the payment processor and logging the result.

After the BillingService is done with all invoices it will return to the billing job the ids of charged invoices. Billing
job will then call the invoice service to mark invoices with those ids as PAID, and others will be returned to PENDING status.
This is so that it can be attempted to charge them again (if the failure was recoverable). Possible improvement with
this process is to mark recoverable and unrecoverable errors and then mark invoices which failed with unrecoverable error
with the appropriate status so that they won't be charged again. However, this implementation will retry charging  all failed invoices.

Two additional REST endpoints are added, mainly for testing purposes: GET /rest/v1/triggerbilling which will trigger the billing
process immediately and return empty 200 response after it's done and GET /rest/v1/billing_log which will return all billing log records


### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine
* [joda-time](https://www.joda.org/joda-time/) -  Provides replacement for the Java date and time classes, used by Exposed

Happy hacking 😁!
