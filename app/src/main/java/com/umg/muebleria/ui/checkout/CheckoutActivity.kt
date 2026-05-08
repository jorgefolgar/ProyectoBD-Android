package com.umg.muebleria.ui.checkout

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.data.model.CheckoutItemRequest
import com.umg.muebleria.data.model.CheckoutRequest
import com.umg.muebleria.data.model.MetodoPagoDto
import com.umg.muebleria.data.repository.MuebleriaRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pantalla de checkout. Selecciona método de pago, valida tarjeta si aplica,
 * y ejecuta la compra vía API (transacción ACID en Oracle).
 */
class CheckoutActivity : AppCompatActivity() {

    private val repository = MuebleriaRepository()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private var methods: List<MetodoPagoDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        setSupportActionBar(findViewById(R.id.toolbarCheckout))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Confirmar compra"

        val session = (application as MuebleriaApp).sessionManager
        val cart = session.getCart()

        if (cart.isEmpty()) {
            Toast.makeText(this, "Carrito vacío", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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

        tvTotal.text = "Total: ${currencyFormat.format(session.getCartTotal())}"
        tvItems.text = cart.joinToString("\n") { "${it.quantity}x ${it.name} — ${currencyFormat.format(it.unitPrice * it.quantity)}" }

        // Formatea automáticamente MM/AA: inserta la "/" después de los primeros 2 dígitos.
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
                // Posicionar el cursor al final para evitar saltos.
                etCardExpiry.setSelection(formatted.length)
                isFormattingExpiry = false
            }
        })

        // Cargar métodos de pago
        lifecycleScope.launch {
            repository.listPaymentMethods().onSuccess { list ->
                methods = list
                val adapter = ArrayAdapter(this@CheckoutActivity, android.R.layout.simple_spinner_item, list.map { it.paymentMethodName })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPayment.adapter = adapter
            }
        }

        spinnerPayment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val name = methods.getOrNull(pos)?.paymentMethodName ?: ""
                cardLayout.visibility = if (name.contains("tarjeta", ignoreCase = true)) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnConfirm.setOnClickListener {
            val selectedIndex = spinnerPayment.selectedItemPosition
            if (selectedIndex < 0 || selectedIndex >= methods.size) {
                Toast.makeText(this, "Selecciona forma de pago", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val method = methods[selectedIndex]
            val isCardPayment = method.paymentMethodName.contains("tarjeta", ignoreCase = true)
            if (isCardPayment) {
                val validationError = validateCardFields(
                    holder = etCardHolder.text?.toString(),
                    number = etCardNumber.text?.toString(),
                    expiry = etCardExpiry.text?.toString(),
                    cvv = etCardCvv.text?.toString()
                )
                if (validationError != null) {
                    Toast.makeText(this, validationError, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            val request = CheckoutRequest(
                paymentMethodId = method.paymentMethodId,
                cardHolderName = etCardHolder.text?.toString(),
                cardNumber = etCardNumber.text?.toString(),
                cardExpiry = etCardExpiry.text?.toString(),
                cardCvv = etCardCvv.text?.toString(),
                items = cart.map { CheckoutItemRequest(it.productId, it.quantity) }
            )

            progress.visibility = View.VISIBLE
            btnConfirm.isEnabled = false

            lifecycleScope.launch {
                val result = repository.checkout(request)
                progress.visibility = View.GONE
                btnConfirm.isEnabled = true

                result.onSuccess { response ->
                    session.clearCart()
                    Toast.makeText(this@CheckoutActivity, response.message, Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@CheckoutActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

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

        if (holderTrim.isBlank()) return "Ingresa el nombre del titular."
        if (numberDigits.length !in 13..19) return "Número de tarjeta inválido."
        if (expiryDigits.length != 4) return "Fecha inválida. Usa formato MM/AA."
        if (cvvDigits.length != 3) return "CVV inválido."

        val month = expiryDigits.substring(0, 2).toIntOrNull() ?: return "Mes de expiración inválido."
        val year2 = expiryDigits.substring(2, 4).toIntOrNull() ?: return "Año de expiración inválido."

        if (month !in 1..12) return "Mes de expiración inválido (01-12)."

        val now = Calendar.getInstance()
        val currentYear2 = now.get(Calendar.YEAR) % 100
        val currentMonth = now.get(Calendar.MONTH) + 1

        if (year2 < currentYear2) return "La tarjeta está vencida."
        if (year2 == currentYear2 && month < currentMonth) return "La tarjeta está vencida."

        return null
    }
}
