package com.plcoding.wear.run.domain

import com.plcoding.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface ExerciseTracker {
    val heartRate: Flow<Int>
    suspend fun isHeartRateTrackingSupported(): Boolean
    suspend fun prepareExercise(): EmptyResult<ExerciseError>
    suspend fun startExercise(): EmptyResult<ExerciseError>
    suspend fun resumeExercise(): EmptyResult<ExerciseError>
    suspend fun pauseExercise(): EmptyResult<ExerciseError>
    suspend fun stopExercise(): EmptyResult<ExerciseError>
}
