package com.kail.location.auth

import android.content.Context
import com.kail.location.network.RuoYiClient
import com.kail.location.utils.KailLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UsageManager {
    private const val TAG = "UsageManager"

    fun init(context: Context) {}

    /**
     * Check if user can start simulation (does NOT consume a count)
     */
    suspend fun canStartSimulation(context: Context): Boolean {
        if (!AuthManager.isLoggedIn) {
            KailLog.i(context, TAG, "canStartSimulation=false: not logged in")
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "请先登录后再使用模拟功能", android.widget.Toast.LENGTH_SHORT).show()
            }
            return false
        }

        if (AuthManager.isSubscribed) {
            KailLog.i(context, TAG, "canStartSimulation=true: subscribed")
            return true
        }

        val token = AuthManager.token ?: return false
        val result = withContext(Dispatchers.IO) {
            RuoYiClient.checkSimulation(token)
        }

        return if (result.isSuccess) {
            val remaining = result.getOrThrow()
            KailLog.i(context, TAG, "canStartSimulation: remaining free count=$remaining")
            if (remaining <= 0) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "今日免费模拟次数已用完，订阅后可无限使用", android.widget.Toast.LENGTH_SHORT).show()
                }
                false
            } else {
                true
            }
        } else {
            KailLog.w(context, TAG, "canStartSimulation: check failed: ${result.exceptionOrNull()?.message}")
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "今日免费模拟次数已用完，订阅后可无限使用", android.widget.Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    /**
     * Consume one simulation count. Call this when user actually starts simulating.
     */
    suspend fun consumeSimulation(context: Context): Boolean {
        if (!AuthManager.isLoggedIn) return false
        if (AuthManager.isSubscribed) return true

        val token = AuthManager.token ?: return false
        val result = withContext(Dispatchers.IO) {
            RuoYiClient.useSimulation(token)
        }

        return if (result.isSuccess) {
            KailLog.i(context, TAG, "consumeSimulation: consumed one count")
            true
        } else {
            KailLog.w(context, TAG, "consumeSimulation failed: ${result.exceptionOrNull()?.message}")
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "模拟次数已用完", android.widget.Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
}
