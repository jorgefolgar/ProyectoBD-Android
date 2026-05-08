package com.umg.muebleria.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.umg.muebleria.R
import com.umg.muebleria.ui.admin.clients.ClientsAdminFragment
import com.umg.muebleria.ui.admin.prices.PricesAdminFragment
import com.umg.muebleria.ui.admin.reports.ReportsFragment
import com.umg.muebleria.ui.profile.ProfileFragment

/**
 * Destinos de administración que no caben en la barra inferior (máx. 5 ítems en Material).
 */
class AdminMoreFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_admin_more, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fm = requireActivity().supportFragmentManager
        view.findViewById<MaterialButton>(R.id.btnAdminPrices).setOnClickListener {
            fm.beginTransaction()
                .replace(R.id.fragmentContainer, PricesAdminFragment())
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<MaterialButton>(R.id.btnAdminReports).setOnClickListener {
            fm.beginTransaction()
                .replace(R.id.fragmentContainer, ReportsFragment())
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<MaterialButton>(R.id.btnAdminProfile).setOnClickListener {
            fm.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<MaterialButton>(R.id.btnAdminUsers).setOnClickListener {
            // "Usuarios" en el menú Más corresponde al manejo admin de clientes/usuarios.
            fm.beginTransaction()
                .replace(R.id.fragmentContainer, ClientsAdminFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
