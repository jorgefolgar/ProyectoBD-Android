package com.umg.muebleria.ui.admin.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.umg.muebleria.R
import com.umg.muebleria.data.model.ReporteResponse
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.umg.muebleria.ui.admin.clients.GenericAdapter
import kotlinx.coroutines.launch

class ReportsFragment : Fragment() {

    private val repo = MuebleriaRepository()

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_reports, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvReportData)
        val progress = view.findViewById<CircularProgressIndicator>(R.id.progressReports)
        val tvTitle = view.findViewById<TextView>(R.id.tvReportTitle)
        val etUserId = view.findViewById<TextInputEditText>(R.id.etReportUserId)
        rv.layoutManager = LinearLayoutManager(requireContext())

        view.findViewById<MaterialButton>(R.id.btnSalesByType).setOnClickListener {
            loadReport(rv, progress, tvTitle) { repo.reportSalesByType() }
        }
        view.findViewById<MaterialButton>(R.id.btnTopProduct).setOnClickListener {
            loadReport(rv, progress, tvTitle) { repo.reportTopProduct() }
        }
        view.findViewById<MaterialButton>(R.id.btnPurchasesByClient).setOnClickListener {
            val uid = etUserId.text?.toString()?.trim()?.toIntOrNull() ?: 0
            loadReport(rv, progress, tvTitle) { repo.reportPurchasesByClient(uid) }
        }
        view.findViewById<MaterialButton>(R.id.btnCashClosures).setOnClickListener {
            loadReport(rv, progress, tvTitle) { repo.reportCashClosures() }
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingBase).setOnClickListener {
            loadReport(rv, progress, tvTitle) { repo.reportMarketingBase() }
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingLtv).setOnClickListener {
            loadReport(rv, progress, tvTitle) { repo.reportMarketingLtv() }
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingActivity).setOnClickListener {
            loadReport(rv, progress, tvTitle) { repo.reportMarketingActivity() }
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingRetention).setOnClickListener {
            loadReport(rv, progress, tvTitle) { repo.reportMarketingRetention() }
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingCohort).setOnClickListener {
            loadReport(rv, progress, tvTitle) { repo.reportMarketingCohort() }
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingRemarketing).setOnClickListener {
            loadReport(rv, progress, tvTitle) { repo.reportMarketingRemarketing() }
        }
    }

    private fun loadReport(
        rv: RecyclerView,
        progress: CircularProgressIndicator,
        tvTitle: TextView,
        call: suspend () -> Result<ReporteResponse>
    ) {
        progress.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            call().onSuccess { resp ->
                progress.visibility = View.GONE
                if (resp.title.isNotBlank()) {
                    tvTitle.text = resp.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }
                val lines = rowsToLines(resp.rows)
                rv.adapter = GenericAdapter(lines) {}
            }.onFailure { e ->
                progress.visibility = View.GONE
                tvTitle.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.report_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun rowsToLines(rows: JsonArray?): List<String> {
        if (rows == null || rows.size() == 0) {
            return listOf(getString(R.string.report_empty))
        }
        val lines = ArrayList<String>(rows.size())
        for (i in 0 until rows.size()) {
            val el = rows[i]
            if (!el.isJsonObject) continue
            val obj = el.asJsonObject
            lines.add(formatJsonRow(obj))
        }
        return if (lines.isEmpty()) listOf(getString(R.string.report_empty)) else lines
    }

    private fun formatJsonRow(obj: JsonObject): String =
        obj.entrySet().joinToString(" | ") { "${it.key}: ${jsonElToText(it.value)}" }

    private fun jsonElToText(el: JsonElement): String {
        if (el.isJsonNull) return "-"
        if (!el.isJsonPrimitive) return el.toString()
        val p = el.asJsonPrimitive
        return when {
            p.isBoolean -> p.asBoolean.toString()
            p.isNumber -> p.asNumber.toString()
            else -> p.asString
        }
    }
}
