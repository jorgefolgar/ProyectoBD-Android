package com.umg.muebleria.ui.admin.products

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.umg.muebleria.R
import com.umg.muebleria.data.model.ProductoAdminDto
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.umg.muebleria.ui.admin.clients.GenericAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProductsAdminFragment : Fragment() {
    private val repo = MuebleriaRepository()
    private var currentQuery: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_products_admin, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvProductsList)
        val progress = view.findViewById<CircularProgressIndicator>(R.id.progressProductsList)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etProductSearch)

        rv.layoutManager = LinearLayoutManager(requireContext())

        var searchJob: Job? = null
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s?.toString()?.trim().orEmpty()
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(350)
                    loadProducts(rv, progress, currentQuery)
                }
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            loadProducts(rv, progress, "")
        }
    }

    private fun resolveSearchParams(query: String): Triple<String?, String?, String?> {
        val q = query.trim()
        if (q.isEmpty()) return Triple(null, null, null)

        // Heurística simple: si son dígitos tratamos como referencia, si no como nombre.
        return if (q.all { it.isDigit() }) Triple(q, null, null) else Triple(null, q, null)
    }

    private suspend fun loadProducts(rv: RecyclerView, progress: CircularProgressIndicator, query: String) {
        progress.visibility = View.VISIBLE
        val (reference, name, typeFilter) = resolveSearchParams(query)

        repo.listProductsAdmin(reference = reference, name = name, typeFilter = typeFilter).onSuccess { list ->
            progress.visibility = View.GONE
            rv.adapter = GenericAdapter(list.map { "${it.reference} — ${it.name} (Stock: ${it.stock ?: 0})" }) { pos ->
                val p = list[pos]
                showProductOptionsDialog(p, rv, progress)
            }
        }.onFailure { e ->
            progress.visibility = View.GONE
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showProductOptionsDialog(product: ProductoAdminDto, rv: RecyclerView, progress: CircularProgressIndicator) {
        val msg = buildString {
            append("Ref: ${product.reference}\n")
            append("Tipo: ${product.typeName ?: "—"}\n")
            append("Material: ${product.productMaterial ?: "—"}\n")
            append("Precio: ${product.unitPrice ?: 0}\n")
            append("Stock: ${product.stock ?: 0}\n")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(product.name)
            .setMessage(msg)
            .setPositiveButton(R.string.products_edit_title) { _, _ ->
                showEditProductDialog(product.productId, rv, progress)
            }
            .setNegativeButton(R.string.products_delete) { _, _ ->
                deleteProduct(product.productId, rv, progress)
            }
            .setNeutralButton("Ver datos") { _, _ ->
                showProductReadDialog(product.productId)
            }
            .show()
    }

    private fun showProductReadDialog(productId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            repo.getProductAdmin(productId).onSuccess { product ->
                val dialogView = layoutInflater.inflate(R.layout.dialog_edit_product_admin, null)
                val etReference = dialogView.findViewById<TextInputEditText>(R.id.etProductReference)
                val etName = dialogView.findViewById<TextInputEditText>(R.id.etProductName)
                val etMaterial = dialogView.findViewById<TextInputEditText>(R.id.etProductMaterial)
                val etColor = dialogView.findViewById<TextInputEditText>(R.id.etProductColor)
                val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etProductDescription)
                val etHeight = dialogView.findViewById<TextInputEditText>(R.id.etProductHeightCm)
                val etWidth = dialogView.findViewById<TextInputEditText>(R.id.etProductWidthCm)
                val etDepth = dialogView.findViewById<TextInputEditText>(R.id.etProductDepthCm)
                val etWeight = dialogView.findViewById<TextInputEditText>(R.id.etProductWeightGrams)

                etReference.setText(product.reference)
                etName.setText(product.name)
                etMaterial.setText(product.productMaterial)
                etColor.setText(product.color)
                etDescription.setText(product.description)
                etHeight.setText(product.heightCm?.toString().orEmpty())
                etWidth.setText(product.widthCm?.toString().orEmpty())
                etDepth.setText(product.depthCm?.toString().orEmpty())
                etWeight.setText(product.weightGrams?.toString().orEmpty())

                val fields = listOf(
                    etReference, etName, etMaterial, etColor,
                    etDescription, etHeight, etWidth, etDepth, etWeight
                )
                fields.forEach { it.isEnabled = false }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Datos del producto")
                    .setView(dialogView)
                    .setPositiveButton("Cerrar", null)
                    .show()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error cargando producto: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showEditProductDialog(productId: Int, rv: RecyclerView, progress: CircularProgressIndicator) {
        // Cargamos el detalle para evitar campos nulos al editar.
        viewLifecycleOwner.lifecycleScope.launch {
            repo.getProductAdmin(productId).onSuccess { full ->
                showEditProductDialogFromModel(full, rv, progress)
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error cargando producto: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showEditProductDialogFromModel(product: ProductoAdminDto, rv: RecyclerView, progress: CircularProgressIndicator) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_product_admin, null)
        val etReference = dialogView.findViewById<TextInputEditText>(R.id.etProductReference)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etProductName)
        val etMaterial = dialogView.findViewById<TextInputEditText>(R.id.etProductMaterial)
        val etColor = dialogView.findViewById<TextInputEditText>(R.id.etProductColor)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etProductDescription)
        val etHeight = dialogView.findViewById<TextInputEditText>(R.id.etProductHeightCm)
        val etWidth = dialogView.findViewById<TextInputEditText>(R.id.etProductWidthCm)
        val etDepth = dialogView.findViewById<TextInputEditText>(R.id.etProductDepthCm)
        val etWeight = dialogView.findViewById<TextInputEditText>(R.id.etProductWeightGrams)

        etReference.setText(product.reference)
        etName.setText(product.name)
        etMaterial.setText(product.productMaterial)
        etColor.setText(product.color)
        etDescription.setText(product.description)
        etHeight.setText(product.heightCm?.toString().orEmpty())
        etWidth.setText(product.widthCm?.toString().orEmpty())
        etDepth.setText(product.depthCm?.toString().orEmpty())
        etWeight.setText(product.weightGrams?.toString().orEmpty())

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.products_edit_title)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.products_save, null)
            .create()

        dialog.setOnShowListener {
            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSave.setOnClickListener {
                val reference = etReference.text?.toString()?.trim().orEmpty()
                val name = etName.text?.toString()?.trim().orEmpty()
                if (reference.isBlank() || name.isBlank()) {
                    Toast.makeText(requireContext(), "Referencia y nombre son obligatorios.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                fun parseDoubleOrNull(s: String?): Double? {
                    val t = s?.trim().orEmpty()
                    return if (t.isBlank()) null else t.toDoubleOrNull()
                }

                fun nullableString(s: String?): String? {
                    val t = s?.trim().orEmpty()
                    return if (t.isBlank()) null else t
                }

                val updated = product.copy(
                    reference = reference,
                    name = name,
                    description = nullableString(etDescription.text?.toString()),
                    productMaterial = nullableString(etMaterial.text?.toString()),
                    color = nullableString(etColor.text?.toString()),
                    heightCm = parseDoubleOrNull(etHeight.text?.toString()),
                    widthCm = parseDoubleOrNull(etWidth.text?.toString()),
                    depthCm = parseDoubleOrNull(etDepth.text?.toString()),
                    weightGrams = parseDoubleOrNull(etWeight.text?.toString()),
                )

                viewLifecycleOwner.lifecycleScope.launch {
                    repo.updateProduct(product.productId, updated).onSuccess {
                        Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadProducts(rv, progress, currentQuery)
                    }.onFailure { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun deleteProduct(productId: Int, rv: RecyclerView, progress: CircularProgressIndicator) {
        viewLifecycleOwner.lifecycleScope.launch {
            repo.deleteProduct(productId).onSuccess {
                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                loadProducts(rv, progress, currentQuery)
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
