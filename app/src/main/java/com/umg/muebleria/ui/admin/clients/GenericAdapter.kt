package com.umg.muebleria.ui.admin.clients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umg.muebleria.R

/** Adapter genérico reutilizable para listas de texto con click */
class GenericAdapter(
    private val items: List<String>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<GenericAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tvAdminItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_admin_generic, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tv.text = items[position]
        holder.itemView.setOnClickListener { onClick(position) }
    }
}
