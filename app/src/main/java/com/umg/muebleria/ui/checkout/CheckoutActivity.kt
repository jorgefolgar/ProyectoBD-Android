package com.umg.muebleria.ui.checkout

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.lifecycleScope
import com.google.android.material.R as MaterialR
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.data.model.CheckoutItemRequest
import com.umg.muebleria.data.model.CheckoutRequest
import com.umg.muebleria.data.model.MetodoPagoDto
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.umg.muebleria.util.LocaleCurrency
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Pantalla de checkout: método de pago, tarjeta si aplica, compra vía API.
 */
class CheckoutActivity : AppCompatActivity() {

    private val repository = MuebleriaRepository()
    private var methods: List<MetodoPagoDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        setSupportActionBar(findViewById(R.id.toolbarCheckout))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.checkout_title)

        val session = (application as MuebleriaApp).sessionManager

        val tvTotal = findViewById<TextView>(R.id.tvCheckoutTotal)
        val tvItems = findViewById<TextView>(R.id.tvCheckoutItems)
        val spinnerPayment = findViewById<Spinner>(R.id.spinnerPayment)
        val cardLayout = findViewById<LinearLayout>(R.id.layoutCardFields)
        val etCardHolder = findViewById<TextInputEditText>(R.id.etCardHolder)
        val etCardNumber = findViewById<TextInputEditText>(R.id.etCardNumber)
        val etCardExpiry = findViewById<TextInputEditText>(R.id.etCardExpiry)
        val etCardCvv = findViewById<TextInputEditText>(R.id.etCardCvv)
        val btnConfirm = findViewById<MaterialButton>(R.id.btnConfirmPurchase)
        val progress = findViewById<CircularProgressIndicator>(R.id.progressCheckout)

        var isFormattingExpiry = false
        etCardExpiry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (isFormattingExpiry) return
                val current = s?.toString().orEmpty()

                val digits = current.filter { it.isDigit() }.take(4)
                val mm = digits.take(2)
                val yy = digits.drop(2).take(2)

                val formatted = when {
                    digits.isEmpty() -> ""
                    yy.isEmpty() -> mm
                    else -> "${mm}/${yy}"
                }

                if (formatted == current) return

                isFormattingExpiry = true
                etCardExpiry.setText(formatted)
                etCardExpiry.setSelection(formatted.length)
                isFormattingExpiry = false
            }
        })

        lifecycleScope.launch {
            progress.visibility = View.VISIBLE
            repository.refreshCartFromServer(session)
            repository.listPaymentMethods().onSuccess { list ->
                methods = list
                val adapter = ArrayAdapter(
                    this@CheckoutActivity,
                    android.R.layout.simple_spinner_item,
                    list.map { it.paymentMethodName }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPayment.adapter = adapter
            }
            progress.visibility = View.GONE

            val cart = session.getCart()
            if (cart.isEmpty()) {
                Toast.makeText(this@CheckoutActivity, getString(R.string.cart_empty_toast), Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            val fmt = LocaleCurrency.forContext(this@CheckoutActivity)
            tvTotal.text = getString(R.string.cart_total_format, fmt.format(session.getCartTotal()))
            tvItems.text = cart.joinToString("\n") { "${it.quantity}x ${it.name} — ${fmt.format(it.unitPrice * it.quantity)}" }

            spinnerPayment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    val name = methods.getOrNull(pos)?.paymentMethodName ?: ""
                    cardLayout.visibility = if (isCardLikePayment(name)) View.VISIBLE else View.GONE
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            btnConfirm.setOnClickListener {
                val selectedIndex = spinnerPayment.selectedItemPosition
                if (selectedIndex < 0 || selectedIndex >= methods.size) {
                    Toast.makeText(this@CheckoutActivity, getString(R.string.checkout_select_payment), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val method = methods[selectedIndex]
                if (isCardLikePayment(method.paymentMethodName)) {
                    val validationError = validateCardFields(
                        holder = etCardHolder.text?.toString(),
                        number = etCardNumber.text?.toString(),
                        expiry = etCardExpiry.text?.toString(),
                        cvv = etCardCvv.text?.toString()
                    )
                    if (validationError != null) {
                        Toast.makeText(this@CheckoutActivity, validationError, Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                }

                val currentCart = session.getCart()
                if (currentCart.isEmpty()) {
                    Toast.makeText(this@CheckoutActivity, getString(R.string.cart_empty_toast), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val request = CheckoutRequest(
                    paymentMethodId = method.paymentMethodId,
                    cardHolderName = etCardHolder.text?.toString(),
                    cardNumber = etCardNumber.text?.toString(),
                    cardExpiry = etCardExpiry.text?.toString(),
                    cardCvv = etCardCvv.text?.toString(),
                    items = currentCart.map { CheckoutItemRequest(it.productId, it.quantity) }
                )

                progress.visibility = View.VISIBLE
                btnConfirm.isEnabled = false

                lifecycleScope.launch {
                    val result = repository.checkout(request)
                    progress.visibility = View.GONE
                    btnConfirm.isEnabled = true

                    result.onSuccess { response ->
                        session.clearCart()
                        if (isFinishing) return@onSuccess
                        val msg = response.message?.trim().orEmpty().ifEmpty {
                            getString(R.string.checkout_success_fallback)
                        }
                        val dialogCtx = ContextThemeWrapper(
                            this@CheckoutActivity,
                            MaterialR.style.Theme_MaterialComponents_Light_Dialog_Alert
                        )
                        AlertDialog.Builder(dialogCtx)
                            .setTitle(R.string.checkout_success_title)
                            .setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton(R.string.accept) { _, _ ->
                                if (!isFinishing) finish()
                            }
                            .show()
                    }.onFailure { e ->
                        Toast.makeText(
                            this@CheckoutActivity,
                            getString(R.string.checkout_error_format, e.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun isCardLikePayment(name: String): Boolean {
        val n = name.lowercase()
        return n.contains("tarjeta") || n.contains("card")
    }

    private fun validateCardFields(
        holder: String?,
        number: String?,
        expiry: String?,
        cvv: String?
    ): String? {
        val holderTrim = holder?.trim().orEmpty()
        val numberDigits = number.orEmpty().filter { it.isDigit() }
        val expiryDigits = expiry.orEmpty().filter { it.isDigit() }
        val cvvDigits = cvv.orEmpty().filter { it.isDigit() }

        if (holderTrim.isBlank()) return getString(R.string.card_holder_required)
        if (numberDigits.length !in 13..19) return getString(R.string.card_number_invalid)
        if (expiryDigits.length != 4) return getString(R.string.card_expiry_invalid)
        if (cvvDigits.length != 3) return getString(R.string.card_cvv_invalid)

        val month = expiryDigits.substring(0, 2).toIntOrNull() ?: return getString(R.string.card_month_invalid)
        val year2 = expiryDigits.substring(2, 4).toIntOrNull() ?: return getString(R.string.card_year_invalid)

        if (month !in 1..12) return getString(R.string.card_month_range_invalid)

        val now = Calendar.getInstance()
        val currentYear2 = now.get(Calendar.YEAR) % 100
        val currentMonth = now.get(Calendar.MONTH) + 1

        if (year2 < currentYear2) return getString(R.string.card_expired)
        if (year2 == currentYear2 && month < currentMonth) return getString(R.string.card_expired)

        return null
    }
}
