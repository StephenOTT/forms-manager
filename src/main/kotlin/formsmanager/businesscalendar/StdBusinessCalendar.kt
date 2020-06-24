package formsmanager.businesscalendar

import java.time.Duration
import java.time.ZonedDateTime
import java.util.*

data class StdBusinessCalendar(
        val rules: List<BusinessCalendarRule>,
        val maxCycles: Int = 2000, // Max attempts to find a next possible date...
        val description: String? = null
) {
    fun isAvailableDate(date: ZonedDateTime): Boolean {
        return rules.all {
            it.test(date)
        }
    }

    fun nextAvailableDateDayIncrement(startAt: ZonedDateTime, days: Int, startOnNextDay: Boolean = false): Optional<ZonedDateTime> {
        var newDate = startAt
        var remainingDays = days
        if (startOnNextDay) {
            // If startAt is also a holiday/blocked day then this does not make much change
            // StartOnNextDay is typically used when startAt is a regular day, but you count your business days starting on the next day
            newDate = newDate.plusDays(1)
        }
        while (remainingDays > 0){
            newDate = nextAvailableDate(newDate).get()
            // We only increment the days if its not the last iteration
            if (remainingDays > 1){
                newDate = newDate.plusDays(1)
            }
            remainingDays -= 1
        }
        return Optional.of(newDate)
    }

    fun nextAvailableDate(startAt: ZonedDateTime): Optional<ZonedDateTime> {
        var newDate = startAt
        var failure: Boolean? = null
        while (failure == null || failure == true) {
            val incrementers: MutableList<Duration> = mutableListOf()
            rules.forEach {
                if (!it.test(newDate)) {
                    // If the rule fails when we add the incrementer duration to the list
                    incrementers.add(it.incrementer(newDate))
                }
            }
            if (incrementers.isNotEmpty()){
             // If the list has incrementers (there were rules that returned false)
                // then we sort the durations to get the largest duration
                // We only need to increment by the largest duration as the smaller ones
                // dont matter if there is a larger duration to increment
                incrementers.sortByDescending {
                    it.seconds
                }
                newDate = newDate.plus(incrementers.first())
                failure = true
            } else {
                // If the incrementers list was empty then there were no failures
                failure = false
            }
        }
        if (failure == false) {
            // If there were no failures then return the new date
            return Optional.of(newDate)
            //@TODO future scenario where optional can return null when rules could not find a option, but no errors were thrown
        } else {
            // If we reach this point then something went wrong...
            throw IllegalStateException("Next available date loop failed.")
        }
    }
}