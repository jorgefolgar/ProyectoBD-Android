package com.umg.muebleria.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umg.muebleria.R
import com.umg.muebleria.data.model.CarritoItem
import java.text.NumberFormat
import java.util.Locale

class CarritoAdapter(
    private val items: List<CarritoItem>,
    private val onIncrement: (CarritoItem) -> Unit,
    private val onDecrement: (CarritoItem) -> Unit,
    private val onRemove: (CarritoItem) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.VH>() {

    private val fmt = NumberFormat.getCurrencyInstance(Locale.US)

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCartItemName)
        val tvPrice: TextView = view.findViewById(R.id.tvCartItemPrice)
        val tvQty: TextView = view.findViewById(R.id.tvCartItemQty)
        val tvLineTotal: TextView = view.findViewById(R.id.tvCartItemLineTotal)
        val btnPlus: ImageButton = view.findViewById(R.id.btnCartPlus)
        val btnMinus: ImageButton = view.findViewById(R.id.btnCartMinus)
        val btnRemove: ImageButton = view.findViewById(R.id.btnCartRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_carrito, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvPrice.text = fmt.format(item.unitPrice)
        holder.tvQty.text = item.quantity.toString()
        holder.tvLineTotal.text = fmt.format(item.unitPrice * item.quantity)
        holder.btnPlus.setOnClickListener { onIncrement(item) }
        holder.btnMinus.setOnClickListener { onDecrement(item) }
        holder.btnRemove.setOnClickListener { onRemove(item) }
    }
}
