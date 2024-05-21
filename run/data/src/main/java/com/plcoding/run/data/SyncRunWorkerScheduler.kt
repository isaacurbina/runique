package com.plcoding.run.data

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import com.plcoding.core.database.dao.RunPendingSyncDao
import com.plcoding.core.database.entity.DeleteRunSyncEntity
import com.plcoding.core.database.entity.RunPendingSyncEntity
import com.plcoding.core.database.mapper.toRunEntity
import com.plcoding.core.domain.SessionStorage
import com.plcoding.core.domain.run.Run
import com.plcoding.core.domain.run.RunId
import com.plcoding.core.domain.run.SyncRunScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class SyncRunWorkerScheduler(
    private val context: Context,
    private val pendingSyncDao: RunPendingSyncDao,
    private val sessionStorage: SessionStorage,
    private val applicationScope: CoroutineScope
) : SyncRunScheduler {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun scheduleSync(type: SyncRunScheduler.SyncType) {
        when (type) {
            is SyncRunScheduler.SyncType.CreateRun -> scheduleCreateRunWorker(
                type.run,
                type.mapPictureBytes
            )

            is SyncRunScheduler.SyncType.DeleteRun -> scheduleDeleteRunWorker(type.runId)
            is SyncRunScheduler.SyncType.FetchRuns -> scheduleFetchRunsWorker(type.interval)
        }
    }

    override suspend fun cancelAllSyncs() {
        workManager
            .cancelAllWork()
            .await()
    }

    private suspend fun scheduleFetchRunsWorker(interval: Duration) {
        val isSyncScheduled = withContext(Dispatchers.IO) {
            workManager
                .getWorkInfosByTag(SYNC_WORK_TAG)
                .get()
                .isNotEmpty()
        }
        if (isSyncScheduled) {
            return
        }
        val workRequest = PeriodicWorkRequestBuilder<FetchRunsWorker>(
            repeatInterval = interval.toJavaDuration()
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2L,
                timeUnit = TimeUnit.SECONDS
            )
            .setInitialDelay(
                duration = 30L,
                timeUnit = TimeUnit.MINUTES
            )
            .addTag(SYNC_WORK_TAG)
            .build()

        workManager.enqueue(workRequest).await()
    }

    private suspend fun scheduleCreateRunWorker(
        run: Run,
        mapPictureBytes: ByteArray
    ) {
        val userId = sessionStorage.get()?.userId ?: return
        val pendingRun = RunPendingSyncEntity(
            run = run.toRunEntity(),
            mapPictureBytes = mapPictureBytes,
            userId = userId
        )
        pendingSyncDao.upsertRunPendingSyncEntity(pendingRun)

        val workRequest = OneTimeWorkRequestBuilder<CreateRunWorker>()
            .addTag(CREATE_WORK_TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2L,
                timeUnit = TimeUnit.SECONDS
            )
            .setInputData(
                Data.Builder()
                    .putString(CreateRunWorker.RUN_ID, pendingRun.runId)
                    .build()
            )
            .build()

        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    private suspend fun scheduleDeleteRunWorker(runId: RunId) {
        val userId = sessionStorage.get()?.userId ?: return
        val entity = DeleteRunSyncEntity(
            runId = runId,
            userId = userId
        )
        pendingSyncDao.upsertDeletedRunSyncEntity(entity)

        val workRequest = OneTimeWorkRequestBuilder<DeleteRunWorker>()
            .addTag(DELETE_WORK_TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2L,
                timeUnit = TimeUnit.SECONDS
            )
            .setInputData(
                Data.Builder()
                    .putString(DeleteRunWorker.RUN_ID, entity.runId)
                    .build()
            )
            .build()

        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    companion object {
        private const val CREATE_WORK_TAG = "runique:create_tag"
        private const val DELETE_WORK_TAG = "runique:delete_tag"
        private const val SYNC_WORK_TAG = "runique:sync_tag"
    }
}
