package ir.pooyande.amar.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import ir.pooyande.amar.R
import ir.pooyande.amar.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var spinnerRole: Spinner
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvCases: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvUser: TextView
    private lateinit var progress: View
    private lateinit var btnLogout: ImageButton

    private var roles: List<Role> = emptyList()
    private var selectedRole: Role? = null
    private var searchJob: Job? = null
    private lateinit var adapter: CaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        session = SessionManager(this)
        ApiClient.setTokenProvider { session.token }

        if (!session.isLoggedIn) {
            goLogin()
            return
        }

        bindViews()
        setupRecycler()
        loadRoles()
        setupSearch()
    }

    private fun bindViews() {
        spinnerRole = findViewById(R.id.spinnerRole)
        etSearch = findViewById(R.id.etSearch)
        rvCases = findViewById(R.id.rvCases)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvUser = findViewById(R.id.tvUser)
        progress = findViewById(R.id.progress)
        btnLogout = findViewById(R.id.btnLogout)

        tvUser.text = session.fullName ?: session.username ?: "کاربر"
        btnLogout.setOnClickListener { confirmLogout() }
    }

    private fun setupRecycler() {
        adapter = CaseAdapter { caseItem, pages ->
            claimCase(caseItem, pages)
        }
        rvCases.layoutManager = LinearLayoutManager(this)
        rvCases.adapter = adapter
    }

    private fun loadRoles() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val res = ApiClient.api.myRoles()
                if (res.success && res.data != null) {
                    roles = res.data
                    if (roles.isEmpty()) {
                        Toast.makeText(this@MainActivity, "هیچ نقشی برای شما تعریف نشده است", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    val names = roles.map { it.toString() }
                    val spinnerAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, names)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerRole.adapter = spinnerAdapter
                    spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            selectedRole = roles[position]
                            adapter.setRequiresPages(selectedRole?.needsPages == true)
                            val q = etSearch.text?.toString()?.trim().orEmpty()
                            if (q.isNotEmpty()) doSearch(q)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                    selectedRole = roles.firstOrNull()
                    adapter.setRequiresPages(selectedRole?.needsPages == true)
                } else {
                    if (res.message?.contains("توکن") == true) {
                        session.clear()
                        goLogin()
                    } else {
                        Toast.makeText(this@MainActivity, res.message ?: "خطا در دریافت نقش‌ها", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "خطای ارتباط: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim().orEmpty()
                searchJob?.cancel()
                if (q.isEmpty()) {
                    adapter.submitList(emptyList())
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "شماره کلاسه را وارد کنید"
                    return
                }
                searchJob = lifecycleScope.launch {
                    delay(350) // debounce
                    doSearch(q)
                }
            }
        })
    }

    private fun doSearch(query: String) {
        val role = selectedRole ?: return
        setLoading(true)
        lifecycleScope.launch {
            try {
                val res = ApiClient.api.search(query, role.id)
                if (res.success) {
                    val list = res.data ?: emptyList()
                    adapter.submitList(list)
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    tvEmpty.text = if (list.isEmpty()) "پرونده‌ای در شورت‌لیست این نقش یافت نشد" else ""
                } else {
                    if (res.message?.contains("توکن") == true) {
                        session.clear()
                        goLogin()
                    } else {
                        Toast.makeText(this@MainActivity, res.message ?: "خطا در جستجو", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "خطای ارتباط: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun claimCase(item: CaseItem, pages: Int?) {
        val role = selectedRole ?: return
        if (role.needsPages && (pages == null || pages <= 0)) {
            Toast.makeText(this, "تعداد اوراق را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("تأیید تخصیص")
            .setMessage("آیا پرونده «${item.caseNumber}» به نقش «${role.name}» تخصیص داده شود؟")
            .setPositiveButton("بله") { _, _ ->
                performClaim(item, role, pages)
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    private fun performClaim(item: CaseItem, role: Role, pages: Int?) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val body = ClaimRequest(
                    caseId = item.id,
                    roleId = role.id,
                    submittedPages = if (role.needsPages) pages else null
                )
                val res = ApiClient.api.claimCase(body)
                if (res.success) {
                    Toast.makeText(this@MainActivity, res.message ?: "ثبت شد", Toast.LENGTH_LONG).show()
                    // حذف از لیست
                    val newList = adapter.currentList.toMutableList().apply { removeAll { it.id == item.id } }
                    adapter.submitList(newList)
                    if (newList.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = "پرونده‌ای در شورت‌لیست این نقش یافت نشد"
                    }
                } else {
                    Toast.makeText(this@MainActivity, res.message ?: "خطا در ثبت", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "خطای ارتباط: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("خروج")
            .setMessage("آیا می‌خواهید از حساب کاربری خارج شوید؟")
            .setPositiveButton("خروج") { _, _ ->
                lifecycleScope.launch {
                    try { ApiClient.api.logout() } catch (_: Exception) {}
                    session.clear()
                    goLogin()
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun goLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
