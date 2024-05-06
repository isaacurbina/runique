package com.plcoding.auth.data

import android.util.Patterns
import com.plcoding.auth.domain.PatternValidator

class EmailPatternValidator : PatternValidator {

    override fun matches(value: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(value).matches()
}
