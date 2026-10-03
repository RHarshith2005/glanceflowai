package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.domain.model.GlanceItem
import com.example.domain.model.GlancePriority

class GlanceNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val CHANNEL_UNFINISHED = "glanceflow_unfinished_assignments"
        const val CHANNEL_UNFINISHED_NAME = "⚠️ Unfinished Assignments"

        const val CHANNEL_REMINDERS = "glanceflow_reminders"
        const val CHANNEL_REMINDERS_NAME = "GlanceFlow Deadlines & Reminders"

        const val SUMMARY_NOTIFICATION_ID = 8888
        const val ALARM_REQUEST_CODE = 7001
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // High priority channel for unfinished assignments with pending tasks
            val unfinishedChannel = NotificationChannel(
                CHANNEL_UNFINISHED,
                CHANNEL_UNFINISHED_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts for coursework & assignments with pending incomplete tasks"
                enableVibration(true)
                setShowBadge(true)
            }

            // General channel for reminders & scheduled alerts
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                CHANNEL_REMINDERS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders and milestones for actionable Glance cards"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(unfinishedChannel)
            notificationManager.createNotificationChannel(remindersChannel)
        }
    }

    /**
     * Dispatches an interactive, expandable notification specifically for an unfinished assignment.
     * Shows remaining pending tasks, priority level, and direct Action buttons (Mark Complete & Snooze).
     */
    fun notifyUnfinishedAssignment(item: GlanceItem) {
        val pendingTasks = item.tasks.filter { !it.isCompleted }
        val pendingCount = pendingTasks.size

        // Intent to open app
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("selected_glance_id", item.id)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            item.id.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: Direct "Mark Complete" broadcast action
        val completeIntent = Intent(context, AssignmentReminderReceiver::class.java).apply {
            action = AssignmentReminderReceiver.ACTION_MARK_COMPLETED
            putExtra(AssignmentReminderReceiver.EXTRA_ASSIGNMENT_ID, item.id)
            putExtra(AssignmentReminderReceiver.EXTRA_ASSIGNMENT_TITLE, item.title)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (item.id + 10000).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Direct "Snooze 1H" broadcast action
        val snoozeIntent = Intent(context, AssignmentReminderReceiver::class.java).apply {
            action = AssignmentReminderReceiver.ACTION_SNOOZE
            putExtra(AssignmentReminderReceiver.EXTRA_ASSIGNMENT_ID, item.id)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (item.id + 20000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Detailed BigTextStyle text showing pending checklist
        val bigText = buildString {
            if (!item.deadline.isNullOrBlank()) {
                append("🗓 DUE: ${item.deadline.uppercase()}\n")
            }
            append("⚡ PRIORITY: ${item.priority.label.uppercase()}\n\n")
            if (pendingTasks.isNotEmpty()) {
                append("PENDING TASKS ($pendingCount remaining):\n")
                pendingTasks.forEachIndexed { i, t ->
                    append("• [ ] ${t.title}\n")
                }
            } else {
                append("• Review final submission requirements.")
            }
        }

        val titlePrefix = if (item.priority == GlancePriority.HIGH) "🚨 CRITICAL ASSIGNMENT" else "⚠️ UNFINISHED ASSIGNMENT"

        val builder = NotificationCompat.Builder(context, CHANNEL_UNFINISHED)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("$titlePrefix: ${item.title}")
            .setContentText(
                if (pendingCount > 0) "$pendingCount tasks incomplete • Due ${item.deadline ?: "Soon"}"
                else "Assignment is incomplete. Tap to view."
            )
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSubText("$pendingCount PENDING")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "MARK DONE",
                completePendingIntent
            )
            .addAction(
                android.R.drawable.ic_popup_reminder,
                "SNOOZE 1H",
                snoozePendingIntent
            )

        notificationManager.notify(item.id.toInt().coerceAtLeast(1), builder.build())
    }

    /**
     * Dispatches an InboxStyle summary notification when multiple assignments are unfinished.
     */
    fun notifyUnfinishedAssignmentsSummary(items: List<GlanceItem>) {
        val totalPending = items.sumOf { it.tasks.count { t -> !t.isCompleted } }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            SUMMARY_NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("📚 ${items.size} Unfinished Assignments Pending")
            .setSummaryText("$totalPending total tasks remaining across coursework")

        items.take(5).forEach { item ->
            val count = item.tasks.count { !it.isCompleted }
            val deadlineStr = if (!item.deadline.isNullOrBlank()) " • ${item.deadline}" else ""
            inboxStyle.addLine("• ${item.title} ($count left$deadlineStr)")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_UNFINISHED)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📚 ${items.size} Unfinished Assignments")
            .setContentText("$totalPending total tasks remaining. Tap to open GlanceFlow.")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, builder.build())
    }

    /**
     * Backward-compatible simple reminder
     */
    fun showGlanceReminder(item: GlanceItem) {
        notifyUnfinishedAssignment(item)
    }

    /**
     * Cancels an active notification
     */
    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    /**
     * Schedules periodic background evaluation of unfinished assignments via AlarmManager.
     */
    fun schedulePeriodicAssignmentChecks(intervalHours: Int = 3) {
        val intent = Intent(context, AssignmentReminderReceiver::class.java).apply {
            action = AssignmentReminderReceiver.ACTION_CHECK_UNFINISHED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (intervalHours * 60 * 60 * 1000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Reschedules an unfinished assignment reminder after snooze
     */
    fun scheduleSnooze(assignmentId: Long, delayMillis: Long) {
        val intent = Intent(context, AssignmentReminderReceiver::class.java).apply {
            action = AssignmentReminderReceiver.ACTION_CHECK_UNFINISHED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (assignmentId + 30000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val triggerTime = System.currentTimeMillis() + delayMillis
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
