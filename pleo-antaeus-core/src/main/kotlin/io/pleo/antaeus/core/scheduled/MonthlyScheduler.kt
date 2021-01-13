package io.pleo.antaeus.core.scheduled

import io.pleo.antaeus.core.job.BillingJob
import java.util.*
import kotlin.concurrent.schedule

class MonthlyScheduler (
    private val billingJob: BillingJob,
    private val scheduleProvider: ScheduleProvider,
    private val schedulerName: String
) : Scheduler {

    override fun schedule(startDay: Int) {

        val scheduledDate = scheduleProvider.getNextDateFromNow(startDay)

        if (scheduledDate != null) {
            Timer(schedulerName, true)
                .schedule(scheduledDate) {
                    billingJob.run()
                    Thread.sleep(1000)
                    schedule(startDay)
                }
        }
    }

}

