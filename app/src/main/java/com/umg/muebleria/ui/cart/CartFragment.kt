package com.umg.muebleria.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.umg.muebleria.MuebleriaApp
import com.umg.muebleria.R
import com.umg.muebleria.data.model.toCarritoItem
import com.umg.muebleria.data.repository.MuebleriaRepository
import com.umg.muebleria.ui.checkout.CheckoutActivity
import com.umg.muebleria.util.LocaleCurrency
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private val repository = MuebleriaRepository()

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_cart, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialButton>(R.id.btnCheckout).setOnClickListener {
            val session = (requireActivity().application as MuebleriaApp).sessionManager
            if (session.getCart().isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.cart_empty_toast), Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(requireContext(), CheckoutActivity::class.java))
            }
        }
        refreshCart(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { refreshCart(it) }
    }

    private fun refreshCart(view: View) {
        val app = requireActivity().application as MuebleriaApp
        val session = app.sessionManager
        lifecycleScope.launch {
            if (session.isLoggedIn()) {
                repository.refreshCartFromServer(session)
            }
            val cart = session.getCart()
            val rv = view.findViewById<RecyclerView>(R.id.rvCart)
            val tvEmpty = view.findViewById<TextView>(R.id.tvCartEmpty)
            val tvTotal = view.findViewById<TextView>(R.id.tvCartTotal)
            val btnCheckout = view.findViewById<MaterialButton>(R.id.btnCheckout)
            val fmt = LocaleCurrency.forContext(requireContext())

            if (cart.isEmpty()) {
                rv.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                btnCheckout.isEnabled = false
                tvTotal.text = fmt.format(0.0)
            } else {
                rv.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
                btnCheckout.isEnabled = true
                rv.layoutManager = LinearLayoutManager(requireContext())
                rv.adapter = CarritoAdapter(
                    requireContext(),
                    cart,
                    onIncrement = inc@{ item ->
                        if (!session.isLoggedIn()) return@inc
                        lifecycleScope.launch {
                            repository.cartAdd(item.productId, 1)
                                .onSuccess { lines -> session.saveCart(lines.map { it.toCarritoItem() }) }
                                .onFailure { e ->
                                    Toast.makeText(
                                        requireContext(),
                                        e.message ?: getString(R.string.cart_empty_toast),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            refreshCart(view)
                        }
                    },
                    onDecrement = dec@{ item ->
                        if (!session.isLoggedIn()) return@dec
                        lifecycleScope.launch {
                            repository.cartDecrement(item.productId)
                                .onSuccess { lines -> session.saveCart(lines.map { it.toCarritoItem() }) }
                            refreshCart(view)
                        }
                    },
                    onRemove = rem@{ item ->
                        if (!session.isLoggedIn()) return@rem
                        lifecycleScope.launch {
                            repository.cartRemoveLine(item.productId)
                                .onSuccess { lines -> session.saveCart(lines.map { it.toCarritoItem() }) }
                            refreshCart(view)
                        }
                    }
                )
                tvTotal.text =
                    getString(R.string.cart_total_format, fmt.format(session.getCartTotal()))
            }
        }
    }
}
