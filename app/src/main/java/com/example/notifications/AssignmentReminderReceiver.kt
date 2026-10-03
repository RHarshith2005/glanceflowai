package com.example.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.data.local.GlanceDatabase
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.domain.model.GlanceCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver responsible for:
 * 1. Periodic background checks of unfinished assignments
 * 2. Direct "Mark Completed" actions directly from notification shade
 * 3. Snoozing assignment alerts
 * 4. Re-registering alarms upon device reboot
 */
class AssignmentReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CHECK_UNFINISHED = "com.example.glanceflow.CHECK_UNFINISHED_ASSIGNMENTS"
        const val ACTION_MARK_COMPLETED = "com.example.glanceflow.MARK_ASSIGNMENT_COMPLETED"
        const val ACTION_SNOOZE = "com.example.glanceflow.SNOOZE_ASSIGNMENT"
        const val EXTRA_ASSIGNMENT_ID = "extra_assignment_id"
        const val EXTRA_ASSIGNMENT_TITLE = "extra_assignment_title"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val notificationManager = GlanceNotificationManager(context)

        when (intent.action) {
            ACTION_CHECK_UNFINISHED, Intent.ACTION_BOOT_COMPLETED -> {
                // Background asynchronous check of Room Database for unfinished assignments
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val database = GlanceDatabase.getDatabase(context)
                        val activeGlances = database.glanceDao().getActiveGlancesSnapshot().map { it.toDomain() }
                        
                        // Filter for unfinished assignments and glances with pending tasks
                        val unfinishedAssignments = activeGlances.filter { glance ->
                            !glance.completed && (
                                glance.category == GlanceCategory.ASSIGNMENT ||
                                glance.category == GlanceCategory.EXAM ||
                                glance.tasks.any { !it.isCompleted }
                            )
                        }

                        if (unfinishedAssignments.isNotEmpty()) {
                            // Notify each critical assignment individually
                            unfinishedAssignments.take(3).forEach { assignment ->
                                notificationManager.notifyUnfinishedAssignment(assignment)
                            }

                            // If there are multiple, also post a summary digest
                            if (unfinishedAssignments.size > 1) {
                                notificationManager.notifyUnfinishedAssignmentsSummary(unfinishedAssignments)
                            }
                        }

                        // Schedule the next check
                        notificationManager.schedulePeriodicAssignmentChecks()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            ACTION_MARK_COMPLETED -> {
                val assignmentId = intent.getLongExtra(EXTRA_ASSIGNMENT_ID, -1L)
                val title = intent.getStringExtra(EXTRA_ASSIGNMENT_TITLE) ?: "Assignment"

                if (assignmentId > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val database = GlanceDatabase.getDatabase(context)
                            val entity = database.glanceDao().getGlanceById(assignmentId)
                            if (entity != null) {
                                val glance = entity.toDomain()
                                // Mark glance and all its subtasks complete
                                val updatedTasks = glance.tasks.map { it.copy(isCompleted = true) }
                                val completedGlance = glance.copy(
                                    completed = true,
                                    tasks = updatedTasks
                                )
                                database.glanceDao().updateGlance(completedGlance.toEntity())

                                // Cancel the active notification
                                notificationManager.cancelNotification(assignmentId.toInt())

                                // Show celebratory feedback notification
                                postCompletionFeedback(context, title)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            ACTION_SNOOZE -> {
                val assignmentId = intent.getLongExtra(EXTRA_ASSIGNMENT_ID, -1L)
                if (assignmentId > 0) {
                    notificationManager.cancelNotification(assignmentId.toInt())
                    // Snooze for 1 hour
                    notificationManager.scheduleSnooze(assignmentId, 60 * 60 * 1000L)
                    Toast.makeText(context, "Assignment snoozed for 1 hour", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun postCompletionFeedback(context: Context, title: String) {
        val sysNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, GlanceNotificationManager.CHANNEL_UNFINISHED)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 Assignment Finished!")
            .setContentText("'$title' has been resolved and marked complete.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setTimeoutAfter(8000)

        sysNotificationManager.notify(9999, builder.build())
    }
}
