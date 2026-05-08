package com.umg.muebleria.ui.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umg.muebleria.BuildConfig
import com.umg.muebleria.R
import com.umg.muebleria.data.model.ProductoDto
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class CatalogoAdapter(
    private val items: List<ProductoDto>,
    private val onClick: (ProductoDto) -> Unit
) : RecyclerView.Adapter<CatalogoAdapter.VH>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivProductPhoto)
        val tvName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvType: TextView = view.findViewById(R.id.tvProductType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_catalogo, parent, false)
        return VH(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name ?: "Sin nombre"
        holder.tvPrice.text = if (item.unitPrice > 0) currencyFormat.format(item.unitPrice) else "Sin precio"
        holder.tvType.text = item.type ?: ""

        if (item.hasPhoto) {
            val imageUrl = "${BuildConfig.API_BASE_URL}catalog/${item.productId}/image"
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(holder.ivPhoto)
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_launcher_foreground)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }
}
