package com.plcoding.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.plcoding.core.database.entity.DeleteRunSyncEntity
import com.plcoding.core.database.entity.RunPendingSyncEntity

@Dao
interface RunPendingSyncDao {

    // region created runs
    @Query("SELECT * FROM RunPendingSyncEntity WHERE userId=:userId")
    suspend fun getAllRunPendingSyncEntities(userId: String): List<RunPendingSyncEntity>

    @Query("SELECT * FROM RunPendingSyncEntity WHERE runId=:runId")
    suspend fun getRunPendingSyncEntity(runId: String): RunPendingSyncEntity?

    @Upsert
    suspend fun upsertRunPendingSyncEntity(entity: RunPendingSyncEntity)

    @Query("DELETE FROM RunPendingSyncEntity WHERE runId=:runId")
    suspend fun deleteRunPendingSyncEntity(runId: String)
    // endregion

    // region deleted runs
    @Query("SELECT * FROM  DeleteRunSyncEntity WHERE userId=:userId")
    suspend fun getAllDeletedRunSyncEntities(userId: String): List<DeleteRunSyncEntity>

    @Upsert
    suspend fun upsertDeletedRunSyncEntity(entity: DeleteRunSyncEntity)

    @Query("DELETE FROM DeleteRunSyncEntity WHERE runId=:runId")
    suspend fun deleteDeleteRunSyncEntity(runId: String)
    // endregion
}
