package dev.drewett.vista.data.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.concurrent.TimeUnit

/**
 * "Jump back in" comes from real usage history. We walk actual foreground events (most reliable
 * across devices/intervals) and order packages by when they were last brought to the foreground.
 * Requires PACKAGE_USAGE_STATS (GET_USAGE_STATS appop), which we grant over adb.
 */
class UsageRepository(context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun recentPackages(lookbackDays: Long = 14): List<String> {
        val end = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
        val start = end - TimeUnit.DAYS.toMillis(lookbackDays)

        // Last foreground timestamp per package, from the raw event stream.
        val lastForeground = LinkedHashMap<String, Long>()
        runCatching {
            val events = usageStatsManager.queryEvents(start, end)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                ) {
                    lastForeground[event.packageName] = event.timeStamp
                }
            }
        }

        val byEvents = lastForeground.entries.sortedByDescending { it.value }.map { it.key }
        if (byEvents.isNotEmpty()) return byEvents

        // Fallback: aggregated stats (handles devices where events are sparse).
        return runCatching {
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, end)
        }.getOrNull().orEmpty()
            .filter { it.lastTimeUsed > 0 }
            .sortedByDescending { it.lastTimeUsed }
            .map { it.packageName }
            .distinct()
    }
}
