package com.umg.muebleria.ui.admin.clients

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
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.umg.muebleria.R
import com.umg.muebleria.data.model.ClienteDto
import com.umg.muebleria.data.repository.MuebleriaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClientsAdminFragment : Fragment() {
    private val repo = MuebleriaRepository()
    private var currentQuery: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_clients_admin, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvClientsList)
        val progress = view.findViewById<CircularProgressIndicator>(R.id.progressClientsList)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etClientSearch)

        rv.layoutManager = LinearLayoutManager(requireContext())

        // Búsqueda con heurística (DPI/nombre/correo) + debounce.
        var searchJob: Job? = null
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s?.toString()?.trim().orEmpty()
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(350)
                    loadClients(rv, progress, currentQuery)
                }
            }
        })

        // Carga inicial.
        viewLifecycleOwner.lifecycleScope.launch {
            loadClients(rv, progress, "")
        }
    }

    private fun resolveSearchParams(query: String): Triple<String?, String?, String?> {
        val q = query.trim()
        if (q.isEmpty()) return Triple(null, null, null)

        if (q.contains("@")) return Triple(null, null, q) // correo

        val digitsOnly = q.all { it.isDigit() }
        return if (digitsOnly) Triple(q, null, null) else Triple(null, q, null) // DPI o nombre
    }

    private suspend fun loadClients(rv: RecyclerView, progress: CircularProgressIndicator, query: String) {
        progress.visibility = View.VISIBLE
        val (document, name, email) = resolveSearchParams(query)

        repo.listClients(document = document, name = name, email = email).onSuccess { list ->
            progress.visibility = View.GONE
            rv.adapter = GenericAdapter(
                list.map { "${it.firstName} ${it.lastName} — ${it.email}" }
            ) { pos ->
                val client = list[pos]
                showClientOptionsDialog(client, rv, progress)
            }
        }.onFailure { e ->
            progress.visibility = View.GONE
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showClientOptionsDialog(client: ClienteDto, rv: RecyclerView, progress: CircularProgressIndicator) {
        val toggleLabel = if (client.isActive == 1) "Desactivar" else "Activar"
        val options = arrayOf("Ver datos", "Editar", toggleLabel, "Eliminar")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${client.firstName} ${client.lastName}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showClientReadDialog(client)
                    1 -> showEditClientDialog(client, rv, progress)
                    2 -> toggleClientStatus(client, rv, progress)
                    3 -> deleteClient(client.userId, rv, progress)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun toggleClientStatus(client: ClienteDto, rv: RecyclerView, progress: CircularProgressIndicator) {
        val nextActive = if (client.isActive == 1) 0 else 1
        val updated = client.copy(
            roleId = if (client.roleId > 0) client.roleId else 2,
            documentTypeId = if (client.documentTypeId > 0) client.documentTypeId else 1,
            isActive = nextActive,
            password = null
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repo.updateClient(client.userId, updated).onSuccess {
                val actionText = if (nextActive == 1) "Cliente activado" else "Cliente desactivado"
                Toast.makeText(requireContext(), actionText, Toast.LENGTH_SHORT).show()
                loadClients(rv, progress, currentQuery)
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showClientReadDialog(client: ClienteDto) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_client, null)
        val etLogin = dialogView.findViewById<TextInputEditText>(R.id.etClientLogin)
        val etDoc = dialogView.findViewById<TextInputEditText>(R.id.etClientDocumentNumber)
        val etFirst = dialogView.findViewById<TextInputEditText>(R.id.etClientFirstName)
        val etLast = dialogView.findViewById<TextInputEditText>(R.id.etClientLastName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etClientEmail)
        val etHomePhone = dialogView.findViewById<TextInputEditText>(R.id.etClientHomePhone)
        val etMobilePhone = dialogView.findViewById<TextInputEditText>(R.id.etClientMobilePhone)
        val etCountry = dialogView.findViewById<TextInputEditText>(R.id.etClientCountry)
        val etMunicipality = dialogView.findViewById<TextInputEditText>(R.id.etClientMunicipality)
        val etDepartment = dialogView.findViewById<TextInputEditText>(R.id.etClientDepartment)
        val etCity = dialogView.findViewById<TextInputEditText>(R.id.etClientCity)
        val swActive = dialogView.findViewById<SwitchMaterial>(R.id.swClientActive)

        etLogin.setText(client.login)
        etDoc.setText(client.documentNumber?.toString().orEmpty())
        etFirst.setText(client.firstName)
        etLast.setText(client.lastName)
        etEmail.setText(client.email)
        etHomePhone.setText(client.homePhone?.toString().orEmpty())
        etMobilePhone.setText(client.mobilePhone?.toString().orEmpty())
        etCountry.setText(client.country.orEmpty())
        etMunicipality.setText(client.municipality.orEmpty())
        etDepartment.setText(client.department.orEmpty())
        etCity.setText(client.city.orEmpty())
        swActive.isChecked = client.isActive == 1

        val allFields = listOf(
            etLogin, etDoc, etFirst, etLast, etEmail,
            etHomePhone, etMobilePhone, etCountry,
            etMunicipality, etDepartment, etCity
        )
        allFields.forEach { it.isEnabled = false }
        swActive.isEnabled = false

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Datos del cliente")
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun showEditClientDialog(client: ClienteDto, rv: RecyclerView, progress: CircularProgressIndicator) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_client, null)
        val etLogin = dialogView.findViewById<TextInputEditText>(R.id.etClientLogin)
        val etDoc = dialogView.findViewById<TextInputEditText>(R.id.etClientDocumentNumber)
        val etFirst = dialogView.findViewById<TextInputEditText>(R.id.etClientFirstName)
        val etLast = dialogView.findViewById<TextInputEditText>(R.id.etClientLastName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etClientEmail)
        val etHomePhone = dialogView.findViewById<TextInputEditText>(R.id.etClientHomePhone)
        val etMobilePhone = dialogView.findViewById<TextInputEditText>(R.id.etClientMobilePhone)
        val etCountry = dialogView.findViewById<TextInputEditText>(R.id.etClientCountry)
        val etMunicipality = dialogView.findViewById<TextInputEditText>(R.id.etClientMunicipality)
        val etDepartment = dialogView.findViewById<TextInputEditText>(R.id.etClientDepartment)
        val etCity = dialogView.findViewById<TextInputEditText>(R.id.etClientCity)
        val swActive = dialogView.findViewById<SwitchMaterial>(R.id.swClientActive)

        etLogin.setText(client.login)
        etDoc.setText(client.documentNumber?.toString().orEmpty())
        etFirst.setText(client.firstName)
        etLast.setText(client.lastName)
        etEmail.setText(client.email)
        etHomePhone.setText(client.homePhone?.toString().orEmpty())
        etMobilePhone.setText(client.mobilePhone?.toString().orEmpty())
        etCountry.setText(client.country.orEmpty())
        etMunicipality.setText(client.municipality.orEmpty())
        etDepartment.setText(client.department.orEmpty())
        etCity.setText(client.city.orEmpty())
        swActive.isChecked = client.isActive == 1

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clients_edit_title)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.clients_save, null)
            .create()

        dialog.setOnShowListener {
            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSave.setOnClickListener {
                val country = etCountry.text?.toString()?.trim().orEmpty()
                if (country.isBlank()) {
                    Toast.makeText(requireContext(), R.string.clients_country_required, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                fun parseLongOrNull(s: String?): Long? {
                    val t = s?.trim().orEmpty()
                    return if (t.isBlank()) null else t.toLongOrNull()
                }

                fun nullableString(s: String?): String? {
                    val t = s?.trim().orEmpty()
                    return if (t.isBlank()) null else t
                }

                val updated = client.copy(
                    roleId = if (client.roleId > 0) client.roleId else 2,
                    login = etLogin.text?.toString()?.trim().orEmpty(),
                    documentTypeId = if (client.documentTypeId > 0) client.documentTypeId else 1,
                    documentNumber = parseLongOrNull(etDoc.text?.toString()),
                    firstName = etFirst.text?.toString()?.trim().orEmpty(),
                    lastName = etLast.text?.toString()?.trim().orEmpty(),
                    email = etEmail.text?.toString()?.trim().orEmpty(),
                    homePhone = parseLongOrNull(etHomePhone.text?.toString()),
                    mobilePhone = parseLongOrNull(etMobilePhone.text?.toString()),
                    country = country,
                    municipality = nullableString(etMunicipality.text?.toString()),
                    department = nullableString(etDepartment.text?.toString()),
                    city = nullableString(etCity.text?.toString()),
                    isActive = if (swActive.isChecked) 1 else 0,
                    password = null
                )

                viewLifecycleOwner.lifecycleScope.launch {
                    repo.updateClient(client.userId, updated).onSuccess {
                        Toast.makeText(requireContext(), "Cliente actualizado", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadClients(rv, progress, currentQuery)
                    }.onFailure { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun deleteClient(id: Int, rv: RecyclerView, progress: CircularProgressIndicator) {
        viewLifecycleOwner.lifecycleScope.launch {
            repo.deleteClient(id).onSuccess {
                Toast.makeText(requireContext(), "Cliente eliminado", Toast.LENGTH_SHORT).show()
                loadClients(rv, progress, currentQuery)
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
