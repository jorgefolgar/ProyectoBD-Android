package com.umg.muebleria.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.data.model.PerfilDto
import com.umg.muebleria.data.model.ProfileUpdateRequest
import com.umg.muebleria.data.repository.MuebleriaRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val repository = MuebleriaRepository()
    private var loadedProfile: PerfilDto? = null

    private lateinit var tvSubtitle: TextView
    private lateinit var progress: CircularProgressIndicator
    private lateinit var groupRead: LinearLayout
    private lateinit var groupEdit: LinearLayout
    private lateinit var btnEdit: MaterialButton
    private lateinit var groupSaveActions: LinearLayout
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton

    private lateinit var tvFirst: TextView
    private lateinit var tvLast: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvCountry: TextView

    private lateinit var etFirst: TextInputEditText
    private lateinit var etLast: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etCity: TextInputEditText
    private lateinit var etCountry: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_profile, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvSubtitle = view.findViewById(R.id.tvProfileSubtitle)
        progress = view.findViewById(R.id.progressProfile)
        groupRead = view.findViewById(R.id.groupProfileReadContent)
        groupEdit = view.findViewById(R.id.groupProfileEdit)
        btnEdit = view.findViewById(R.id.btnEditProfile)
        groupSaveActions = view.findViewById(R.id.groupProfileSaveActions)
        btnCancel = view.findViewById(R.id.btnCancelEditProfile)
        btnSave = view.findViewById(R.id.btnSaveProfile)

        tvFirst = view.findViewById(R.id.tvProfileFirstName)
        tvLast = view.findViewById(R.id.tvProfileLastName)
        tvEmail = view.findViewById(R.id.tvProfileEmail)
        tvPhone = view.findViewById(R.id.tvProfilePhone)
        tvCity = view.findViewById(R.id.tvProfileCity)
        tvCountry = view.findViewById(R.id.tvProfileCountry)

        etFirst = view.findViewById(R.id.etProfileFirstName)
        etLast = view.findViewById(R.id.etProfileLastName)
        etEmail = view.findViewById(R.id.etProfileEmail)
        etPhone = view.findViewById(R.id.etProfilePhone)
        etCity = view.findViewById(R.id.etProfileCity)
        etCountry = view.findViewById(R.id.etProfileCountry)

        btnEdit.setOnClickListener { showEditMode() }
        btnCancel.setOnClickListener { cancelEdit() }
        btnSave.setOnClickListener { saveProfile() }

        loadProfile()
    }

    private fun loadProfile() {
        progress.visibility = View.VISIBLE
        groupRead.visibility = View.GONE
        groupEdit.visibility = View.GONE
        btnEdit.visibility = View.GONE
        groupSaveActions.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProfile()
                .onSuccess { p ->
                    if (!isAdded) return@onSuccess
                    loadedProfile = p
                    progress.visibility = View.GONE
                    bindProfileToViews(p)
                    syncToolbarWithProfile(p)
                    showReadMode()
                }
                .onFailure {
                    if (!isAdded) return@onFailure
                    progress.visibility = View.GONE
                    Toast.makeText(requireContext(), R.string.profile_load_error, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun displayNameFromProfile(p: PerfilDto): String {
        return listOfNotNull(
            p.firstName.trim().takeIf { it.isNotEmpty() },
            p.middleName?.trim()?.takeIf { it.isNotEmpty() },
            p.lastName.trim().takeIf { it.isNotEmpty() },
            p.secondLastName?.trim()?.takeIf { it.isNotEmpty() }
        ).joinToString(" ")
    }

    private fun syncToolbarWithProfile(p: PerfilDto) {
        val name = displayNameFromProfile(p)
        if (name.isBlank()) return
        val act = activity as? AppCompatActivity ?: return
        val session = (act.application as MuebleriaApp).sessionManager
        session.updateUserFullName(name)
        act.supportActionBar?.subtitle = name
    }

    private fun dashIfBlank(value: String?): String {
        val t = value?.trim().orEmpty()
        return if (t.isEmpty()) "—" else t
    }

    private fun bindProfileToViews(p: PerfilDto) {
        tvFirst.text = dashIfBlank(p.firstName)
        tvLast.text = dashIfBlank(p.lastName)
        tvEmail.text = dashIfBlank(p.email)
        tvPhone.text = p.homePhone?.toString() ?: "—"
        tvCity.text = dashIfBlank(p.city)
        tvCountry.text = dashIfBlank(p.country)

        etFirst.setText(p.firstName)
        etLast.setText(p.lastName)
        etEmail.setText(p.email)
        etPhone.setText(p.homePhone?.takeIf { it > 0 }?.toString().orEmpty())
        etCity.setText(p.city.orEmpty())
        etCountry.setText(p.country.orEmpty())
    }

    private fun showReadMode() {
        tvSubtitle.setText(R.string.profile_subtitle_read)
        groupRead.visibility = View.VISIBLE
        groupEdit.visibility = View.GONE
        btnEdit.visibility = View.VISIBLE
        groupSaveActions.visibility = View.GONE
    }

    private fun showEditMode() {
        loadedProfile?.let { bindProfileToViews(it) }
        tvSubtitle.setText(R.string.profile_subtitle_edit)
        groupRead.visibility = View.GONE
        groupEdit.visibility = View.VISIBLE
        btnEdit.visibility = View.GONE
        groupSaveActions.visibility = View.VISIBLE
    }

    private fun cancelEdit() {
        loadedProfile?.let { bindProfileToViews(it) }
        showReadMode()
    }

    private fun saveProfile() {
        val first = etFirst.text?.toString()?.trim().orEmpty()
        val last = etLast.text?.toString()?.trim().orEmpty()
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val country = etCountry.text?.toString()?.trim().orEmpty()

        if (first.isEmpty() || last.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), R.string.profile_required_fields, Toast.LENGTH_SHORT).show()
            return
        }
        if (country.isEmpty()) {
            Toast.makeText(requireContext(), R.string.profile_country_required, Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProfileUpdateRequest(
            firstName = first,
            lastName = last,
            email = email,
            homePhone = etPhone.text?.toString()?.trim()?.toIntOrNull(),
            city = etCity.text?.toString()?.trim()?.takeIf { it.isNotEmpty() },
            country = country
        )

        progress.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            repository.updateProfile(request)
                .onSuccess {
                    if (!isAdded) return@onSuccess
                    progress.visibility = View.GONE
                    Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show()
                    loadProfile()
                }
                .onFailure { e ->
                    if (!isAdded) return@onFailure
                    progress.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.profile_save_error)}: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
