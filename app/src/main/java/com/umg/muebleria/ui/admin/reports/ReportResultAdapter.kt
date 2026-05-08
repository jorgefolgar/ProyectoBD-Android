package com.umg.muebleria.ui.admin.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umg.muebleria.R

data class ReportRowUi(
    val title: String,
    val body: String
)

class ReportResultAdapter(
    private val rows: List<ReportRowUi>
) : RecyclerView.Adapter<ReportResultAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvReportRowTitle)
        val tvBody: TextView = view.findViewById(R.id.tvReportRowBody)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_report_row, parent, false))

    override fun getItemCount(): Int = rows.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = rows[position]
        holder.tvTitle.text = row.title
        holder.tvBody.text = row.body
    }
}
