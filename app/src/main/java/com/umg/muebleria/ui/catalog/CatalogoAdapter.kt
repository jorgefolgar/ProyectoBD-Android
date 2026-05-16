package com.umg.muebleria.ui.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.umg.muebleria.BuildConfig
import com.umg.muebleria.R
import com.umg.muebleria.data.model.ProductoDto
import com.umg.muebleria.util.LocaleCurrency
import java.text.NumberFormat

class CatalogoAdapter(
    private val items: List<ProductoDto>,
    /** Epoch de última carga API de catálogo/detalle; fuerza a Glide a no reusar bitmap en disco/memoria obsoleto. */
    private val imageLoadEpoch: Long,
    private val onClick: (ProductoDto) -> Unit
) : RecyclerView.Adapter<CatalogoAdapter.VH>() {

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

    override fun onViewRecycled(holder: VH) {
        // No usar Glide.with(ivPhoto): al destruir MainActivity el contexto de la vista puede estar muerto
        // y clear() lanzaría igual que un load. applicationContext sigue vivo.
        Glide.with(holder.ivPhoto.context.applicationContext).clear(holder.ivPhoto)
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val ctx = holder.itemView.context
        val currencyFormat: NumberFormat = LocaleCurrency.forContext(ctx)

        holder.tvName.text = item.name ?: ctx.getString(R.string.catalog_no_name)
        holder.tvPrice.text =
            if (item.unitPrice > 0) currencyFormat.format(item.unitPrice)
            else ctx.getString(R.string.catalog_no_price)
        holder.tvType.text = item.type ?: ""

        val imageUrl = "${BuildConfig.API_BASE_URL}api/catalog/${item.productId}/image"
        // Usar la ImageView (no el Context de la Activity): al cerrar sesión la Activity se destruye
        // y Glide.with(activity) puede lanzar "You cannot start a load for a destroyed activity".
        Glide.with(holder.ivPhoto)
            .load(imageUrl)
            .signature(ObjectKey("${imageLoadEpoch}_${item.productId}"))
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .centerCrop()
            .into(holder.ivPhoto)

        holder.itemView.setOnClickListener { onClick(item) }
    }
}
