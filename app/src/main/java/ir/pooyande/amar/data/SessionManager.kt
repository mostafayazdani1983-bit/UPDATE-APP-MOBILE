package ir.pooyande.amar.data

import android.content.Context
import android.content.SharedPreferences
import ir.pooyande.amar.util.Constants

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(Constants.KEY_TOKEN, null)
        set(value) = prefs.edit().putString(Constants.KEY_TOKEN, value).apply()

    var userId: Int
        get() = prefs.getInt(Constants.KEY_USER_ID, 0)
        set(value) = prefs.edit().putInt(Constants.KEY_USER_ID, value).apply()

    var fullName: String?
        get() = prefs.getString(Constants.KEY_FULL_NAME, null)
        set(value) = prefs.edit().putString(Constants.KEY_FULL_NAME, value).apply()

    var username: String?
        get() = prefs.getString(Constants.KEY_USERNAME, null)
        set(value) = prefs.edit().putString(Constants.KEY_USERNAME, value).apply()

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    fun saveLogin(data: LoginData) {
        token = data.token
        userId = data.userId
        fullName = data.fullName
        username = data.username
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
