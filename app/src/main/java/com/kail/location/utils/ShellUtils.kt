package com.kail.location.utils

import java.io.BufferedReader
import java.io.InputStreamReader

object ShellUtils {
    private const val TAG = "ShellUtils"

    fun hasRoot(): Boolean {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("su")
            process.outputStream.write("id\n".toByteArray())
            process.outputStream.write("exit\n".toByteArray())
            process.outputStream.flush()
            process.outputStream.close()

            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            process.waitFor()
            val rooted = output.contains("uid=0") || process.exitValue() == 0
            KailLog.d(null, TAG, "hasRoot=$rooted")
            return rooted
        } catch (e: Exception) {
            KailLog.w(null, TAG, "hasRoot: su unavailable: ${e.message}")
            return false
        } finally {
            process?.destroy()
        }
    }

    fun executeCommand(command: String): String {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("su")
            process.outputStream.write("$command\n".toByteArray())
            process.outputStream.write("exit\n".toByteArray())
            process.outputStream.flush()
            process.outputStream.close()

            val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            process.waitFor()
            if (stderr.isNotBlank()) {
                KailLog.w(null, TAG, "executeCommand stderr [`$command`]: ${stderr.trim()}")
            } else {
                KailLog.d(null, TAG, "executeCommand [`$command`] ok (${stdout.length} bytes out)")
            }
            return if (stdout.isNotEmpty()) stdout else stderr
        } catch (e: Exception) {
            KailLog.e(null, TAG, "executeCommand failed [`$command`]", e)
            return ""
        } finally {
            process?.destroy()
        }
    }

    fun executeCommandToBytes(command: String): ByteArray {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("su")
            process.outputStream.write("$command\n".toByteArray())
            process.outputStream.write("exit\n".toByteArray())
            process.outputStream.flush()
            process.outputStream.close()

            val stdout = process.inputStream.use { it.readBytes() }
            val stderr = process.errorStream.use { it.readBytes() }
            process.waitFor()
            if (stdout.isEmpty() && stderr.isNotEmpty()) {
                KailLog.w(null, TAG, "executeCommandToBytes stderr [`$command`]: ${String(stderr).trim()}")
            }
            return if (stdout.isNotEmpty()) stdout else stderr
        } catch (e: Exception) {
            KailLog.e(null, TAG, "executeCommandToBytes failed [`$command`]", e)
            return ByteArray(0)
        } finally {
            process?.destroy()
        }
    }
}
