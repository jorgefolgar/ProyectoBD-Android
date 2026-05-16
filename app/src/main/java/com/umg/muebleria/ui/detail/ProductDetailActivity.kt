package com.umg.muebleria.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.bumptech.glide.signature.ObjectKey
import com.umg.muebleria.BuildConfig
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.data.model.ProductoDto
import com.umg.muebleria.data.model.toCarritoItem
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.umg.muebleria.ui.catalog.CatalogoAdapter
import com.umg.muebleria.util.LocaleCurrency
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity() {

    private val repository = MuebleriaRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        setSupportActionBar(findViewById(R.id.toolbarDetail))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val productId = intent.getIntExtra("productId", 0)
        if (productId == 0) {
            finish()
            return
        }

        val progress = findViewById<CircularProgressIndicator>(R.id.progressDetail)
        val content = findViewById<ScrollView>(R.id.contentDetail)
        val fmt = LocaleCurrency.forContext(this)

        progress.visibility = View.VISIBLE
        content.visibility = View.GONE

        lifecycleScope.launch {
            val result = repository.getCatalogDetail(productId)
            progress.visibility = View.GONE

            result.onSuccess { product ->
                val app = application as MuebleriaApp
                app.notifyProductImagesMayHaveChanged()
                val imageEpoch = app.productImagesLoadEpoch

                content.visibility = View.VISIBLE
                supportActionBar?.title = product.name

                findViewById<TextView>(R.id.tvDetailName).text = product.name
                findViewById<TextView>(R.id.tvDetailPrice).text = fmt.format(product.unitPrice)
                findViewById<TextView>(R.id.tvDetailRef).text =
                    getString(R.string.detail_ref_format, product.reference ?: "")
                findViewById<TextView>(R.id.tvDetailType).text = product.type ?: ""
                findViewById<TextView>(R.id.tvDetailMaterial).text = getString(
                    R.string.detail_material_format,
                    product.material ?: getString(R.string.detail_na)
                )
                findViewById<TextView>(R.id.tvDetailDesc).text =
                    product.description ?: getString(R.string.detail_no_description)
                findViewById<TextView>(R.id.tvDetailStock).text =
                    getString(R.string.detail_stock_format, product.stock)
                findViewById<TextView>(R.id.tvDetailDimensions).text = getString(
                    R.string.detail_dimensions_format,
                    (product.widthCm ?: 0).toString(),
                    (product.heightCm ?: 0).toString(),
                    (product.depthCm ?: 0).toString(),
                    (product.weightGrams ?: 0).toString()
                )

                val ivPhoto = findViewById<ImageView>(R.id.ivDetailPhoto)
                val imageUrl = "${BuildConfig.API_BASE_URL}api/catalog/$productId/image"
                Glide.with(this@ProductDetailActivity)
                    .load(imageUrl)
                    .signature(ObjectKey("${imageEpoch}_$productId"))
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivPhoto)

                val btnAdd = findViewById<MaterialButton>(R.id.btnAddToCart)
                if (product.isAvailable) {
                    btnAdd.visibility = View.VISIBLE
                    btnAdd.setOnClickListener {
                        lifecycleScope.launch {
                            val session = (application as MuebleriaApp).sessionManager
                            val result = repository.cartAdd(product.productId, 1)
                            result.onSuccess { lines ->
                                session.saveCart(lines.map { it.toCarritoItem() })
                                Toast.makeText(
                                    this@ProductDetailActivity,
                                    getString(R.string.detail_added_to_cart),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.onFailure { e ->
                                Toast.makeText(
                                    this@ProductDetailActivity,
                                    e.message ?: getString(R.string.detail_not_found),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                } else {
                    btnAdd.visibility = View.GONE
                }

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
                            rvRecommended.adapter = CatalogoAdapter(recommended, imageEpoch) { selected: ProductoDto ->
                                startActivity(
                                    Intent(this@ProductDetailActivity, ProductDetailActivity::class.java)
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
                Toast.makeText(this@ProductDetailActivity, getString(R.string.detail_not_found), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
