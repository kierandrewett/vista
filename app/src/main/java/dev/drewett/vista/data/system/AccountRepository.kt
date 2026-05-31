package dev.drewett.vista.data.system

import android.accounts.AccountManager
import android.content.Context

/** Best-effort read of the signed-in Google account for the avatar. */
class AccountRepository(private val context: Context) {

    /** First letter of the Google account name/email, or null if not visible to us. */
    fun googleInitial(): String? = runCatching {
        val manager = AccountManager.get(context)
        manager.getAccountsByType("com.google")
            .firstOrNull()
            ?.name
            ?.trim()
            ?.firstOrNull { it.isLetterOrDigit() }
            ?.uppercaseChar()
            ?.toString()
    }.getOrNull()
}
