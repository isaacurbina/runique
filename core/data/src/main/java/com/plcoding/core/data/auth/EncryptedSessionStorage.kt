package com.plcoding.core.data.auth

import android.content.SharedPreferences
import com.plcoding.core.domain.AuthInfo
import com.plcoding.core.domain.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EncryptedSessionStorage(
    private val sharedPreferences: SharedPreferences
) : SessionStorage {

    override suspend fun get(): AuthInfo? = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString(KEY_AUTH_INFO, null)
        json?.let {
            Json.decodeFromString<AuthInfoSerializable>(json)
                .toAuthInfo()
        }
    }

    override suspend fun set(info: AuthInfo) {
        withContext(Dispatchers.IO) {
            val json = Json.encodeToString(info.toAuthInfoSerializable())
            sharedPreferences.edit()
                .putString(KEY_AUTH_INFO, json)
                .apply()
        }
    }

    override suspend fun clear() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_INFO)
            .apply()
    }

    companion object {
        private const val KEY_AUTH_INFO = "KEY_AUTH_INFO"
    }
}
