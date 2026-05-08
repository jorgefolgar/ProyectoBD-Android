package com.umg.muebleria.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.umg.muebleria.BuildConfig
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.data.model.CarritoItem
import com.umg.muebleria.data.model.ProductoDto
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.umg.muebleria.ui.catalog.CatalogoAdapter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private val repository = MuebleriaRepository()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        setSupportActionBar(findViewById(R.id.toolbarDetail))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val productId = intent.getIntExtra("productId", 0)
        if (productId == 0) { finish(); return }

        val progress = findViewById<CircularProgressIndicator>(R.id.progressDetail)
        val content = findViewById<ScrollView>(R.id.contentDetail)

        progress.visibility = View.VISIBLE
        content.visibility = View.GONE

        lifecycleScope.launch {
            val result = repository.getCatalogDetail(productId)
            progress.visibility = View.GONE

            result.onSuccess { product ->
                content.visibility = View.VISIBLE
                supportActionBar?.title = product.name

                findViewById<TextView>(R.id.tvDetailName).text = product.name
                findViewById<TextView>(R.id.tvDetailPrice).text = currencyFormat.format(product.unitPrice)
                findViewById<TextView>(R.id.tvDetailRef).text = "Ref: ${product.reference}"
                findViewById<TextView>(R.id.tvDetailType).text = product.type ?: ""
                findViewById<TextView>(R.id.tvDetailMaterial).text = "Material: ${product.material ?: "N/A"}"
                findViewById<TextView>(R.id.tvDetailDesc).text = product.description ?: "Sin descripción"
                findViewById<TextView>(R.id.tvDetailStock).text = "Stock: ${product.stock}"
                findViewById<TextView>(R.id.tvDetailDimensions).text =
                    "Dimensiones: ${product.widthCm ?: 0}×${product.heightCm ?: 0}×${product.depthCm ?: 0} cm | Peso: ${product.weightGrams ?: 0}g"

                val ivPhoto = findViewById<ImageView>(R.id.ivDetailPhoto)
                if (product.hasPhoto) {
                    Glide.with(this@ProductDetailActivity)
                        .load("${BuildConfig.API_BASE_URL}catalog/$productId/image")
                        .centerCrop()
                        .into(ivPhoto)
                }

                val btnAdd = findViewById<MaterialButton>(R.id.btnAddToCart)
                if (product.isAvailable) {
                    btnAdd.visibility = View.VISIBLE
                    btnAdd.setOnClickListener {
                        val session = (application as MuebleriaApp).sessionManager
                        session.addToCart(CarritoItem(
                            productId = product.productId,
                            reference = product.reference ?: "",
                            name = product.name ?: "",
                            unitPrice = product.unitPrice,
                            quantity = 1
                        ))
                        Toast.makeText(this@ProductDetailActivity, "Agregado al carrito", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    btnAdd.visibility = View.GONE
                }

                // "También te puede interesar": replica la idea del web (4 productos con stock y precio),
                // excluyendo el producto actual.
                val layoutRecommended = findViewById<LinearLayout>(R.id.layoutRecommended)
                val rvRecommended = findViewById<RecyclerView>(R.id.rvRecommended)
                rvRecommended.layoutManager = GridLayoutManager(this@ProductDetailActivity, 2)
                rvRecommended.setHasFixedSize(true)

                lifecycleScope.launch {
                    val recResult = repository.listCatalog()
                    recResult.onSuccess { all ->
                        val recommended = all
                            .filter { it.productId != product.productId && it.stock > 0 && it.unitPrice > 0.0 }
                            .take(4)
                        if (recommended.isNotEmpty()) {
                            rvRecommended.adapter = CatalogoAdapter(recommended) { selected: ProductoDto ->
                                startActivity(
                                    android.content.Intent(this@ProductDetailActivity, ProductDetailActivity::class.java)
                                        .putExtra("productId", selected.productId)
                                )
                            }
                            layoutRecommended.visibility = View.VISIBLE
                        } else {
                            layoutRecommended.visibility = View.GONE
                        }
                    }
                }
            }.onFailure {
                Toast.makeText(this@ProductDetailActivity, "Producto no encontrado", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
