package com.umg.muebleria.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.ui.checkout.CheckoutActivity
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_cart, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshCart(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { refreshCart(it) }
    }

    private fun refreshCart(view: View) {
        val session = (requireActivity().application as MuebleriaApp).sessionManager
        val cart = session.getCart()
        val rv = view.findViewById<RecyclerView>(R.id.rvCart)
        val tvEmpty = view.findViewById<TextView>(R.id.tvCartEmpty)
        val tvTotal = view.findViewById<TextView>(R.id.tvCartTotal)
        val btnCheckout = view.findViewById<MaterialButton>(R.id.btnCheckout)

        if (cart.isEmpty()) {
            rv.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            btnCheckout.isEnabled = false
            tvTotal.text = currencyFormat.format(0.0)
        } else {
            rv.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            btnCheckout.isEnabled = true
            rv.layoutManager = LinearLayoutManager(requireContext())
            rv.adapter = CarritoAdapter(cart,
                onIncrement = { item ->
                    session.addToCart(item.copy(quantity = 1))
                    refreshCart(view)
                },
                onDecrement = { item ->
                    session.decrementInCart(item.productId)
                    refreshCart(view)
                },
                onRemove = { item ->
                    session.removeFromCart(item.productId)
                    refreshCart(view)
                }
            )
            tvTotal.text = "Total: ${currencyFormat.format(session.getCartTotal())}"
        }

        btnCheckout.setOnClickListener {
            if (cart.isEmpty()) {
                Toast.makeText(requireContext(), "Carrito vacío", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(requireContext(), CheckoutActivity::class.java))
            }
        }
    }
}
