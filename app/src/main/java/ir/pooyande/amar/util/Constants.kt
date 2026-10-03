package ir.pooyande.amar.util

object Constants {
    /**
     * آدرس سرور:
     * - آنلاین:  "https://amar.pooyande.ir/"
     * - محلی (همان وای‌فای PC): "http://192.168.1.10/amar/"  ← IP کامپیوتر خود را بگذارید
     * - امولاتور اندروید استودیو به localhost PC: "http://10.0.2.2/amar/"
     */
    const val BASE_URL = "http://192.168.1.10/amar/"
    const val API_ENDPOINT = "mobile_api.php"

    const val PREFS_NAME = "amar_session"
    const val KEY_TOKEN = "api_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_FULL_NAME = "full_name"
    const val KEY_USERNAME = "username"
}
