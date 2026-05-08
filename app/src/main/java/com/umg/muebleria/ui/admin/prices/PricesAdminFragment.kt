package com.umg.muebleria.ui.admin.prices

import android.text.Editable
import android.text.TextWatcher
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.umg.muebleria.R
import com.umg.muebleria.data.model.PrecioDto
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.umg.muebleria.ui.admin.clients.GenericAdapter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PricesAdminFragment : Fragment() {
    private val repo = MuebleriaRepository()
    private val usdFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private var allRows: List<Pair<PrecioDto, String>> = emptyList()
    private lateinit var rv: RecyclerView
    private lateinit var progress: CircularProgressIndicator
    private lateinit var search: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_prices_admin, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv = view.findViewById(R.id.rvAdminList)
        progress = view.findViewById(R.id.progressAdminList)
        search = view.findViewById(R.id.etPriceSearch)

        rv.layoutManager = LinearLayoutManager(requireContext())
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) = renderRows()
        })

        loadData()
    }

    private fun loadData() {
        progress.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            repo.listPrices().onSuccess { list ->
                // Complementar desde catálogo por si el backend aún no trae todo en /api/prices.
                val catalogMap = repo.listCatalog().getOrNull()
                    ?.associateBy { it.productId }
                    .orEmpty()

                allRows = list.map { p ->
                    val c = catalogMap[p.productId]
                    val name = p.productName?.takeIf { it.isNotBlank() } ?: c?.name ?: "Producto #${p.productId}"
                    val reference = p.productReference?.takeIf { it.isNotBlank() } ?: c?.reference ?: "Sin ref"
                    val stock = p.stock ?: c?.stock ?: 0
                    val price = p.value ?: c?.unitPrice ?: 0.0
                    val status = if (p.active == 1) "Activo ✓" else "Inactivo ✗"

                    p to "$reference — $name\nPrecio: ${usdFormat.format(price)} | Stock: $stock | $status"
                }

                progress.visibility = View.GONE
                renderRows()
            }.onFailure { e ->
                progress.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun renderRows() {
        val q = search.text?.toString()?.trim()?.lowercase().orEmpty()
        val filtered = if (q.isBlank()) {
            allRows
        } else {
            allRows.filter { (_, line) -> line.lowercase().contains(q) }
        }

        if (filtered.isEmpty()) {
            rv.adapter = GenericAdapter(listOf("Sin precios registrados")) {}
            return
        }

        rv.adapter = GenericAdapter(filtered.map { it.second }) { pos ->
            showEditDialog(filtered[pos].first)
        }
    }

    private fun showEditDialog(item: PrecioDto) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_price, null, false)
        val etValue = dialogView.findViewById<EditText>(R.id.etEditPriceValue)
        val etStock = dialogView.findViewById<EditText>(R.id.etEditPriceStock)
        val swActive = dialogView.findViewById<SwitchMaterial>(R.id.swEditPriceActive)

        etValue.setText((item.value ?: 0.0).toString())
        etStock.setText((item.stock ?: 0).toString())
        swActive.isChecked = item.active == 1

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar precio #${item.priceId}")
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.profile_action_save, null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val value = etValue.text?.toString()?.trim()?.toDoubleOrNull()
                        val stock = etStock.text?.toString()?.trim()?.toIntOrNull()
                        if (value == null || stock == null) {
                            Toast.makeText(requireContext(), getString(R.string.prices_edit_invalid), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val payload = item.copy(
                            value = value,
                            stock = stock,
                            active = if (swActive.isChecked) 1 else 0
                        )

                        progress.visibility = View.VISIBLE
                        viewLifecycleOwner.lifecycleScope.launch {
                            repo.updatePrice(item.priceId, payload).onSuccess {
                                progress.visibility = View.GONE
                                Toast.makeText(requireContext(), getString(R.string.prices_updated), Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                loadData()
                            }.onFailure { e ->
                                progress.visibility = View.GONE
                                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                dialog.show()
            }
    }
}
