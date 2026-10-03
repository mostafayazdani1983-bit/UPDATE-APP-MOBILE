package ir.pooyande.amar.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.pooyande.amar.R
import ir.pooyande.amar.data.CaseItem

class CaseAdapter(
    private val onClaim: (CaseItem, Int?) -> Unit
) : ListAdapter<CaseItem, CaseAdapter.VH>(DIFF) {

    private var requiresPages: Boolean = false

    fun setRequiresPages(value: Boolean) {
        requiresPages = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_case, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCaseNumber: TextView = itemView.findViewById(R.id.tvCaseNumber)
        private val tvContracts: TextView = itemView.findViewById(R.id.tvContracts)
        private val layoutPages: LinearLayout = itemView.findViewById(R.id.layoutPages)
        private val etPages: EditText = itemView.findViewById(R.id.etPages)
        private val tvPrevPages: TextView = itemView.findViewById(R.id.tvPrevPages)
        private val btnClaim: Button = itemView.findViewById(R.id.btnClaim)

        fun bind(item: CaseItem) {
            tvCaseNumber.text = "کلاسه: ${item.caseNumber}"
            tvContracts.text = item.contractNums?.takeIf { it.isNotBlank() } ?: "بدون اطلاعات قرارداد"

            val showPages = requiresPages || item.needsPageInput
            layoutPages.visibility = if (showPages) View.VISIBLE else View.GONE

            if (showPages) {
                val prev = item.displayPages
                etPages.setText(if (prev > 0) prev.toString() else "")
                // جلوگیری از متن سفید روی بعضی گوشی‌ها (تم تیره / OEM)
                etPages.setTextColor(Color.parseColor("#212529"))
                etPages.setHintTextColor(Color.parseColor("#6c757d"))
                etPages.setBackgroundResource(R.drawable.bg_edit_pages)
                tvPrevPages.text = if (prev > 0) "(ثبت‌شده قبلی: $prev برگ)" else ""
            }

            btnClaim.setOnClickListener {
                val pages = if (showPages) {
                    etPages.text.toString().toIntOrNull()
                } else null
                onClaim(item, pages)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CaseItem>() {
            override fun areItemsTheSame(old: CaseItem, new: CaseItem) = old.id == new.id
            override fun areContentsTheSame(old: CaseItem, new: CaseItem) = old == new
        }
    }
}
