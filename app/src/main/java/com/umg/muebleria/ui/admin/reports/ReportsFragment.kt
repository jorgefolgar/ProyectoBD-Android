package com.umg.muebleria.ui.admin.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.umg.muebleria.R

class ReportsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_reports, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<MaterialButton>(R.id.btnSalesByType).setOnClickListener {
            openReportForm(ReportFormFragment.ReportType.SALES_BY_TYPE)
        }
        view.findViewById<MaterialButton>(R.id.btnTopProduct).setOnClickListener {
            openReportForm(ReportFormFragment.ReportType.TOP_PRODUCT)
        }
        view.findViewById<MaterialButton>(R.id.btnPurchasesByClient).setOnClickListener {
            openReportForm(ReportFormFragment.ReportType.PURCHASES_BY_CLIENT)
        }
        view.findViewById<MaterialButton>(R.id.btnCashClosures).setOnClickListener {
            openReportForm(ReportFormFragment.ReportType.CASH_CLOSURES)
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingLtv).setOnClickListener {
            openReportForm(ReportFormFragment.ReportType.MARKETING_LTV)
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingActivity).setOnClickListener {
            openReportForm(ReportFormFragment.ReportType.MARKETING_ACTIVITY)
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingRetention).setOnClickListener {
            openReportForm(ReportFormFragment.ReportType.MARKETING_RETENTION)
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingCohort).setOnClickListener {
            openReportForm(ReportFormFragment.ReportType.MARKETING_COHORT)
        }
        view.findViewById<MaterialButton>(R.id.btnMarketingRemarketing).setOnClickListener {
            openReportForm(ReportFormFragment.ReportType.MARKETING_REMARKETING)
        }
    }

    private fun openReportForm(type: ReportFormFragment.ReportType) {
        val fragment = ReportFormFragment().apply {
            arguments = Bundle().apply {
                putString(ReportFormFragment.ARG_REPORT_TYPE, type.key)
            }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
