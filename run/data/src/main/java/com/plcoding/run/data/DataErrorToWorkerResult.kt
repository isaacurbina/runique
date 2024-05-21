package com.plcoding.run.data

import androidx.work.ListenableWorker
import com.plcoding.core.domain.util.DataError

fun DataError.toWorkerResult(): ListenableWorker.Result {
    return when (this) {
        DataError.Network.REQUEST_TIMEOUT,
        DataError.Network.UNAUTHORIZED,
        DataError.Network.CONFLICT,
        DataError.Network.TOO_MANY_REQUESTS,
        DataError.Network.NO_INTERNET,
        DataError.Network.SERVER_ERROR -> ListenableWorker.Result.retry()

        DataError.Local.DISK_FULL,
        DataError.Network.PAYLOAD_TOO_LARGE,
        DataError.Network.SERIALIZATION,
        DataError.Network.UNKNOWN -> ListenableWorker.Result.failure()
    }
}
