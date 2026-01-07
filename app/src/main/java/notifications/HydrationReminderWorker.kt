package com.example.projekuas.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class HydrationReminderWorker(
    private val ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {

    override fun doWork(): Result {
        NotificationHelper.show(
            ctx,
            "Hydrology",
            "Waktunya minum air 💧 (pengingat 2 jam)"
        )
        return Result.success()
    }
}
