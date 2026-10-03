package ir.pooyande.amar.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import ir.pooyande.amar.R
import ir.pooyande.amar.data.ApiClient
import ir.pooyande.amar.data.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progress: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        session = SessionManager(this)
        ApiClient.setTokenProvider { session.token }

        if (session.isLoggedIn) {
            goToMain()
            return
        }

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progress = findViewById(R.id.progress)

        btnLogin.setOnClickListener { doLogin() }
    }

    private fun doLogin() {
        val user = etUsername.text?.toString()?.trim().orEmpty()
        val pass = etPassword.text?.toString()?.trim().orEmpty()
        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "نام کاربری و رمز عبور را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val res = ApiClient.api.login(mapOf("username" to user, "password" to pass))
                if (res.success && res.data != null) {
                    session.saveLogin(res.data)
                    Toast.makeText(this@LoginActivity, "خوش آمدید ${res.data.fullName}", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    Toast.makeText(this@LoginActivity, res.message ?: "خطا در ورود", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "خطای ارتباط: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
