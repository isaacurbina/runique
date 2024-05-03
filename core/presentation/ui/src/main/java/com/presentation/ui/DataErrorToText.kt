package com.presentation.ui

import com.plcoding.core.domain.util.DataError
import com.plcoding.core.presentation.ui.R

fun DataError.asUiText(): UiText {
    val stringId = when (this) {
        DataError.Local.DISK_FULL -> R.string.error_disk_full
        DataError.Network.REQUEST_TIMEOUT -> R.string.error_request_timeout
        DataError.Network.TOO_MANY_REQUESTS -> R.string.error_too_many_requests
        DataError.Network.NO_INTERNET -> R.string.error_no_internet
        DataError.Network.PAYLOAD_TOO_LARGE -> R.string.error_payload_too_large
        DataError.Network.SERVER_ERROR -> R.string.error_server_error
        DataError.Network.SERIALIZATION -> R.string.error_serialization
        else -> R.string.error_unknown
    }
    return UiText.StringResource(stringId)
}
