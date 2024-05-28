package com.plcoding.wear.run.domain

import com.plcoding.core.domain.util.Error

enum class ExerciseError : Error {
    TRACKING_NOT_SUPPORTED,
    ALREADY_IN_USE,
    IN_USE_BY_OTHER_APP,
    EXERCISE_ALREADY_ENDED,
    UNKNOWN
}
