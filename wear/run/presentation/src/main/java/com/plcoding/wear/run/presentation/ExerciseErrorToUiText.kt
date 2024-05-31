package com.plcoding.wear.run.presentation

import com.plcoding.auth.presentation.UiText
import com.plcoding.wear.run.domain.ExerciseError

fun ExerciseError.toUiText(): UiText? {
    return when (this) {
        ExerciseError.TRACKING_NOT_SUPPORTED -> null
        ExerciseError.ALREADY_IN_USE ->
            UiText.StringResource(R.string.error_ongoing_exercise)

        ExerciseError.IN_USE_BY_OTHER_APP ->
            UiText.StringResource(R.string.error_ongoing_exercise_other_app)

        ExerciseError.EXERCISE_ALREADY_ENDED ->
            UiText.StringResource(
                R.string.error_exercise_already_ended
            )

        ExerciseError.UNKNOWN ->
            UiText.StringResource(com.plcoding.core.presentation.ui.R.string.error_unknown)
    }
}
