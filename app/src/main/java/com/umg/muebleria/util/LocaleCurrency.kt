package com.umg.muebleria.util



import android.content.Context

import java.text.NumberFormat

import java.util.Currency

import java.util.Locale



/**

 * Pesos colombianos (COP): símbolo habitual «$» en Colombia; no es USD.

 * Fija locale es-CO para que no dependa de la moneda del sistema (p. ej. GTQ).

 */

object LocaleCurrency {

    fun forContext(@Suppress("UNUSED_PARAMETER") context: Context): NumberFormat =

        NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {

            currency = Currency.getInstance("COP")

        }

}

