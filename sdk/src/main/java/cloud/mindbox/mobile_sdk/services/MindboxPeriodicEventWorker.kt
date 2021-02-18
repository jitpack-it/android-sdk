package cloud.mindbox.mobile_sdk.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import cloud.mindbox.mobile_sdk.managers.logEndWork
import cloud.mindbox.mobile_sdk.managers.sendEventsWithResult

internal class MindboxPeriodicEventWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return sendEventsWithResult(
            context = applicationContext,
            parent = this,
            workerType = WorkerType.PERIODIC_WORKER
        )
    }

    override fun onStopped() {
        super.onStopped()
        logEndWork(this)
    }
}