package com.umg.muebleria.ui.catalog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.data.model.ProductoDto
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.umg.muebleria.ui.detail.ProductDetailActivity
import kotlinx.coroutines.launch

class CatalogFragment : Fragment() {

    private val repository = MuebleriaRepository()
    private lateinit var recyclerView: RecyclerView
    private lateinit var progress: CircularProgressIndicator

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        return inflater.inflate(R.layout.fragment_catalog, c, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rvCatalog)
        progress = view.findViewById(R.id.progressCatalog)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    private fun loadProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            progress.visibility = View.VISIBLE
            try {
                val result = repository.listCatalog()
                result.onSuccess { products ->
                    if (!isAdded) return@onSuccess
                    val app = requireActivity().application as MuebleriaApp
                    app.notifyProductImagesMayHaveChanged()
                    val epoch = app.productImagesLoadEpoch
                    recyclerView.adapter = CatalogoAdapter(products, epoch) { product: ProductoDto ->
                        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                        intent.putExtra("productId", product.productId)
                        startActivity(intent)
                    }
                }.onFailure { e ->
                    if (!isAdded) return@launch
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.catalog_load_error, e.message ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                if (isAdded) progress.visibility = View.GONE
            }
        }
    }
}
