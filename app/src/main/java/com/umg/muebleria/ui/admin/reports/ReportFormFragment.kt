package com.umg.muebleria.ui.admin.reports

import android.app.DatePickerDialog
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
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.umg.muebleria.R
import com.umg.muebleria.data.model.ReporteResponse
import com.umg.muebleria.data.repository.MuebleriaRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ReportFormFragment : Fragment() {

    private val repo = MuebleriaRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_report_form, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val reportType = requireArguments().getString(ARG_REPORT_TYPE).orEmpty()
        val selected = ReportType.fromKey(reportType)
        if (selected == null) {
            Toast.makeText(requireContext(), getString(R.string.report_invalid_type), Toast.LENGTH_LONG).show()
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        val tvFormTitle = view.findViewById<TextView>(R.id.tvReportFormTitle)
        val tilStartDate = view.findViewById<TextInputLayout>(R.id.tilStartDate)
        val tilEndDate = view.findViewById<TextInputLayout>(R.id.tilEndDate)
        val tilCity = view.findViewById<TextInputLayout>(R.id.tilCity)
        val tilUserId = view.findViewById<TextInputLayout>(R.id.tilUserId)
        val etStartDate = view.findViewById<TextInputEditText>(R.id.etStartDate)
        val etEndDate = view.findViewById<TextInputEditText>(R.id.etEndDate)
        val etCity = view.findViewById<TextInputEditText>(R.id.etCity)
        val etUserId = view.findViewById<TextInputEditText>(R.id.etUserId)
        val btnGenerate = view.findViewById<MaterialButton>(R.id.btnGenerateReport)
        val rv = view.findViewById<RecyclerView>(R.id.rvReportData)
        val progress = view.findViewById<CircularProgressIndicator>(R.id.progressReports)
        val tvTitle = view.findViewById<TextView>(R.id.tvReportTitle)

        rv.layoutManager = LinearLayoutManager(requireContext())
        tvFormTitle.text = selected.label(requireContext())

        tilStartDate.visibility = if (selected.usesDateRange) View.VISIBLE else View.GONE
        tilEndDate.visibility = if (selected.usesDateRange) View.VISIBLE else View.GONE
        tilCity.visibility = if (selected.usesCity) View.VISIBLE else View.GONE
        tilUserId.visibility = if (selected.usesUserId) View.VISIBLE else View.GONE
        setupDatePicker(etStartDate)
        setupDatePicker(etEndDate)

        btnGenerate.setOnClickListener {
            val startDate = etStartDate.text?.toString()?.trim().orEmpty().ifBlank { null }
            val endDate = etEndDate.text?.toString()?.trim().orEmpty().ifBlank { null }
            val city = etCity.text?.toString()?.trim().orEmpty().ifBlank { null }
            val userId = etUserId.text?.toString()?.trim()?.toIntOrNull() ?: 0

            if (selected.usesDateRange && (startDate == null || endDate == null)) {
                Toast.makeText(requireContext(), getString(R.string.report_dates_required), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            when {
                selected == ReportType.SALES_BY_TYPE ->
                    loadReport(rv, progress, tvTitle) { repo.reportSalesByType(startDate, endDate, city) }
                selected == ReportType.TOP_PRODUCT ->
                    loadReport(rv, progress, tvTitle) { repo.reportTopProduct(startDate, endDate, city, null) }
                selected == ReportType.PURCHASES_BY_CLIENT ->
                    loadReport(rv, progress, tvTitle) { repo.reportPurchasesByClient(userId) }
                selected == ReportType.CASH_CLOSURES ->
                    loadReport(rv, progress, tvTitle) { repo.reportCashClosures() }
                selected == ReportType.MARKETING_LTV ->
                    loadReport(rv, progress, tvTitle) { repo.reportMarketingLtv(startDate, endDate) }
                selected == ReportType.MARKETING_ACTIVITY ->
                    loadReport(rv, progress, tvTitle) { repo.reportMarketingActivity(startDate, endDate) }
                selected == ReportType.MARKETING_RETENTION ->
                    loadReport(rv, progress, tvTitle) { repo.reportMarketingRetention(startDate, endDate) }
                selected == ReportType.MARKETING_COHORT ->
                    loadReport(rv, progress, tvTitle) { repo.reportMarketingCohort(startDate, endDate) }
                selected == ReportType.MARKETING_REMARKETING ->
                    loadReport(rv, progress, tvTitle) { repo.reportMarketingRemarketing(startDate, endDate) }
            }
        }
    }

    private fun setupDatePicker(target: TextInputEditText) {
        target.setOnClickListener {
            val now = Calendar.getInstance()
            val dialog = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val value = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                    target.setText(value)
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            )
            dialog.show()
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
                val rows = rowsToUi(resp.rows)
                rv.adapter = ReportResultAdapter(rows)
            }.onFailure { e ->
                progress.visibility = View.GONE
                tvTitle.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.report_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun rowsToUi(rows: JsonArray?): List<ReportRowUi> {
        if (rows == null || rows.size() == 0) {
            return listOf(ReportRowUi(getString(R.string.report_result_empty_title), getString(R.string.report_empty)))
        }
        val uiRows = ArrayList<ReportRowUi>(rows.size())
        for (i in 0 until rows.size()) {
            val el = rows[i]
            if (!el.isJsonObject) continue
            val obj = el.asJsonObject
            uiRows.add(
                ReportRowUi(
                    title = getString(R.string.report_row_title, i + 1),
                    body = formatJsonRow(obj)
                )
            )
        }
        return if (uiRows.isEmpty()) {
            listOf(ReportRowUi(getString(R.string.report_result_empty_title), getString(R.string.report_empty)))
        } else {
            uiRows
        }
    }

    private fun formatJsonRow(obj: JsonObject): String =
        obj.entrySet().joinToString("\n") { "• ${it.key}: ${jsonElToText(it.value)}" }

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

    enum class ReportType(
        val key: String,
        val usesDateRange: Boolean,
        val usesCity: Boolean,
        val usesUserId: Boolean
    ) {
        SALES_BY_TYPE("sales_by_type", true, true, false),
        TOP_PRODUCT("top_product", true, true, false),
        PURCHASES_BY_CLIENT("purchases_by_client", false, false, true),
        CASH_CLOSURES("cash_closures", false, false, false),
        MARKETING_LTV("marketing_ltv", true, false, false),
        MARKETING_ACTIVITY("marketing_activity", true, false, false),
        MARKETING_RETENTION("marketing_retention", true, false, false),
        MARKETING_COHORT("marketing_cohort", true, false, false),
        MARKETING_REMARKETING("marketing_remarketing", true, false, false);

        fun label(context: android.content.Context): String = when (this) {
            SALES_BY_TYPE -> context.getString(R.string.report_sales_by_type)
            TOP_PRODUCT -> context.getString(R.string.report_top_product)
            PURCHASES_BY_CLIENT -> context.getString(R.string.report_purchases_by_client)
            CASH_CLOSURES -> context.getString(R.string.report_cash_closures)
            MARKETING_LTV -> context.getString(R.string.report_marketing_ltv)
            MARKETING_ACTIVITY -> context.getString(R.string.report_marketing_activity)
            MARKETING_RETENTION -> context.getString(R.string.report_marketing_retention)
            MARKETING_COHORT -> context.getString(R.string.report_marketing_cohort)
            MARKETING_REMARKETING -> context.getString(R.string.report_marketing_remarketing)
        }

        companion object {
            fun fromKey(key: String): ReportType? = entries.firstOrNull { it.key == key }
        }
    }

    companion object {
        const val ARG_REPORT_TYPE = "report_type"
    }
}
