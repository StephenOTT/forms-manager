# Business Calendar

Kotlin / Java library for definition of Business Calendars and calculations of next available date and next available date after N a duration (such as 10 business days).

## What does it do?

Business Calendar provides robust support for the definition of a business calendars and 
the calculation of: 

   1. Next available date
   1. Next available date after `N` days business days
   1. Is the provided date an available date in the business calendar


```kotlin
val bc = StdBusinessCalendar(
                listOf(
                        Holiday(listOf("2020-06-24", "2020-06-25", "2020-06-26")), // Cannot be any of these dates
                        Is(weekday()), // Must be a weekday (no weekends)
                        Is(weekend().negate()), // example of saying "not a weekend"
                        IfIs(weekday(), betweenHours("09:00", "17:30")) {
                            // Standard workweek:
                            if (it.toLocalTime().isBefore(LocalTime.of(17, 30))) {
                                Duration.ofMinutes(1)
                            } else {
                                Duration.ofDays(1)
                            }
                        },
                        IfIs(date("06-29"), betweenHours("10:00", "12:30")) {
                            // modification to a standard work week with a partial day
                            if (it.toLocalTime().isBefore(LocalTime.of(12, 30))) {
                                // If it's before 12:30 then increment by 1min
                                // Of course you could add more complex logic to detect current time and jump to best starting point.
                                Duration.ofMinutes(1)
                            } else {
                                // If it is already past 12:30 then go to next day
                                Duration.ofDays(1)
                            }
                        },
                        Holiday(listOf("2020-06-23"))
                )
        )
```


## Logic and Conditions

1. All Rules must pass
1. If multiple rules fail, then the rule with the largest Incrementer Duration will have its duration used for incrementing.
1. Use the `Is(everyday())` pattern as a catch all rule if you want to allow all days except for the days you provide (such as holidays). 


Incrementer Optimization
BusinessCalendarRule


## Built in Rules

1. `Is(..)` : Checks if the predicate is true
1. `IfIs(..)` : If the first predicate is true then second predicate must be true.
1. `Holiday(listOf("06-22"))` : Must not be any of the dates (because they are holidays). (Holiday is an example of a custom rule rather than writing a more complex `Is(..)`) 

## BusinessCalendar Helpers:

Helpers are all based on Predicates API

   1. `everyday()` : always true.  Used as a catch-all
   1. "Must be a weekday" : `weekday()` (M-F Workweek) / custom workweek:`weekday(arrayOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY))`
   1. "Must be a weekend" : `weekend()` (Sat-Sun) / custom weekend: `weekend(arrayOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY))`
   1. "Must be between A and B hours" : `betweenHours(time, time)` : supports String and LocalTime
   1. "Must be this date without a year" : `date(date)` : supports String and MonthDay
   1. "Must be this date with year" : `specificDate(date)` : supports LocalDate
   1. "Must be this day of week" : `dayOfWeek(DayOfWeek)` : 
   1. "Must be this month" : `month(Month)`
   
You can chain predicates such as: `weekday().and(betweenHours("09:00", "10:00"))` or `weekday().or(date("06-27"))` which means "weekday or the 27th of June (a weekend)"

You can negate/reverse using the Predicates API `.negate()`: `weekend().negate()` which means "not a weekend".

## Next Available Date Calculations


### Next Available Date

Calculates the next available date based on the business calendar.

Returns a date.

`bc.nextAvailableDate(now).get()`

### Is Date Available

Checks if the date is available in the business calendar.

Returns a boolean

`bc.isAvailableDate(now)`

### Next Available Date after N Business Days

Calculates the next available Date After a specific number of business days.

Example: Date is June 1, 2020, calculate next available business date after 10 business days.

Returns a date 

`bc.nextAvailableDateDayIncrement(date, Int-of-BusinessDays, StartOnNextDayBoolean).get()`

`StartOnNextDayBoolean` boolean indicates if the current day is an available business day, should the current day get skipped. 

`bc.nextAvailableDateDayIncrement(now, 2, true).get()`

`bc.nextAvailableDateDayIncrement(now, 2, false).get()`

`bc.nextAvailableDateDayIncrement(now, 2).get()` (false is the default)

## Business Calendar Examples:
1. Holiday
1. Working Days Per week with holidays
1. Working Days plus a single day with partial hours restriction


Holidays CSV
Holiday Rule
Is Rule
IfIs Rule

Default Duration increments


## Building a Custom BusinessCalendarRule

Why
How
Usage of Predicate


## Best practices

1. Starting Date should be a rounded date such as 9am rather than 9:03:02
1. Always consider add custom incrementer logic when you need large time jumps: such as entire week's holiday should have incrementer logic to jump 7 days.
1. Incrementer duration logic ensures less loops are required to determine next available day