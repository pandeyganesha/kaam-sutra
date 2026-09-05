package com.pandeyganesha.kaamsutra.ui.components.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pandeyganesha.kaamsutra.data.Habit
import com.pandeyganesha.kaamsutra.data.HabitLog
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import java.time.DayOfWeek
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import java.time.LocalDate

private fun periodsForHeatMap(repeatType: RepeatType, today: LocalDate = LocalDate.now()): List<LocalDate> =
    when (repeatType) {
        RepeatType.DAILY -> {
            val start = today.minusDays(364)
            (0..364).map { start.plusDays(it.toLong()) }
        }
        RepeatType.WEEKLY -> {
            val thisMonday = today.with(DayOfWeek.MONDAY)
            val start = thisMonday.minusWeeks(52)
            generateSequence(start) { it.plusWeeks(1) }
                .takeWhile { !it.isAfter(thisMonday) }
                .toList()
        }
        RepeatType.MONTHLY -> {
            val thisMonth = today.withDayOfMonth(1)
            (0..11).map { thisMonth.minusMonths((11 - it).toLong()) }
        }
        RepeatType.YEARLY -> emptyList()
    }

@Composable
fun HeatMapBox(
    habit: Habit,
    habitLogs: List<HabitLog>,
    modifier: Modifier = Modifier
) {
    val completedDates = remember(habitLogs) {
        habitLogs.filter { it.completed }.map { it.habitDate }.toSet()
    }
    val periods = remember(habit.repeatType) { periodsForHeatMap(habit.repeatType) }

    when (habit.repeatType) {
        RepeatType.DAILY -> DailyHeatMapGrid(periods, completedDates, modifier)
        RepeatType.WEEKLY -> LinearHeatMapRow(periods, completedDates, pixelSize = 12.dp, modifier = modifier)
        RepeatType.MONTHLY -> LinearHeatMapRow(periods, completedDates, pixelSize = 18.dp, modifier = modifier)
        RepeatType.YEARLY -> Unit
    }
}

// GitHub-style: 7 rows (Sun..Sat), oldest column on the left, auto-scrolled to today
@Composable
private fun DailyHeatMapGrid(
    periods: List<LocalDate>,
    completedDates: Set<String>,
    modifier: Modifier = Modifier
) {
    val firstDate = periods.first()
    val leadingPad = firstDate.dayOfWeek.value % 7 // days since the preceding Sunday
    val cells: List<LocalDate?> = List(leadingPad) { null } + periods
    val weeks = cells.chunked(7)
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) { scrollState.scrollTo(scrollState.maxValue) }

    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        weeks.forEach { week ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                week.forEach { date ->
                    if (date == null) {
                        Spacer(modifier = Modifier.size(10.dp))
                    } else {
                        HeatMapPixel(colorIt = date.toString() in completedDates, size = 10.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinearHeatMapRow(
    periods: List<LocalDate>,
    completedDates: Set<String>,
    pixelSize: Dp,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) { scrollState.scrollTo(scrollState.maxValue) }

    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        periods.forEach { date ->
            HeatMapPixel(colorIt = date.toString() in completedDates, size = pixelSize)
        }
    }
}

@Composable
fun HeatMapPixel(colorIt: Boolean, size: Dp = 14.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(3.dp))
            .background(if (colorIt) Color(0xFF4CAF50) else Color(0xFF3A3A3A))
    )
}