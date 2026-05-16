package com.umg.muebleria.localization

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.R as MaterialR
import com.google.android.material.button.MaterialButton
import com.umg.muebleria.R

/**
 * Selector de idioma con botones.
 * Usa [AlertDialog] + contexto Material de la librería para evitar crashes al inflar vistas Material.
 */
object LanguagePicker {
    fun show(activity: AppCompatActivity) {
        // Contexto con tema Material de diálogo (los MaterialButton del layout lo requieren).
        val dialogContext = ContextThemeWrapper(activity, MaterialR.style.Theme_MaterialComponents_Light_Dialog_Alert)
        val view = LayoutInflater.from(dialogContext).inflate(R.layout.dialog_language, null, false)

        val btnEs = view.findViewById<MaterialButton>(R.id.btnLangEs) ?: return
        val btnEn = view.findViewById<MaterialButton>(R.id.btnLangEn) ?: return
        val btnSys = view.findViewById<MaterialButton>(R.id.btnLangSystem) ?: return

        val dialog = AlertDialog.Builder(dialogContext)
            .setTitle(R.string.language_title)
            .setView(view)
            .setNegativeButton(R.string.cancel, null)
            .create()

        fun applyAndRecreate(tag: String?) {
            AppLocale.persistAndApply(activity, tag)
            dialog.dismiss()
            activity.window.decorView.post {
                if (!activity.isFinishing) {
                    activity.recreate()
                }
            }
        }

        btnEs.setOnClickListener { applyAndRecreate("es") }
        btnEn.setOnClickListener { applyAndRecreate("en") }
        btnSys.setOnClickListener { applyAndRecreate(null) }

        dialog.show()
    }
}
