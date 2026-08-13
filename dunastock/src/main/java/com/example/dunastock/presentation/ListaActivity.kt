package com.example.dunastock.presentation

import android.app.Activity
import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.example.dunastock.R
import java.net.URL
import org.json.JSONArray
import kotlin.concurrent.thread

class ListaActivity : Activity() {

    private lateinit var rvPedidos: WearableRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)

        rvPedidos = findViewById(R.id.rvPedidos)

        // Configuramos el layout manager especial para smartwatch (permite la curvatura)
        rvPedidos.layoutManager = WearableLinearLayoutManager(this)
        rvPedidos.isEdgeItemsCenteringEnabled = true // Centra el primer y último elemento en la pantalla

        cargarPedidosDesdeApi()
    }

    private fun cargarPedidosDesdeApi() {
        thread {
            try {
                val respuesta = URL("https://dunastock-api.onrender.com/api/pedidos").readText()
                val arregloPedidos = JSONArray(respuesta)
                val listaVirtual = mutableListOf<Pedido>()

                for (i in 0 until arregloPedidos.length()) {
                    val obj = arregloPedidos.getJSONObject(i)

                    // Extraemos las propiedades reales del JSON (ajusta los nombres según tu MockAPI)
                    val idPedido = obj.optString("id", "0000")
                    val estado = obj.optString("estado", "Pendiente")
                    val cantidadItems = obj.optString("items", "1")

                    // Construimos la descripción dinámica para el smartwatch
                    val detalleReal = "$estado • $cantidadItems items"

                    listaVirtual.add(Pedido(idPedido, detalleReal))
                }

                runOnUiThread {
                    val adaptador = PedidoAdapter(listaVirtual)
                    rvPedidos.adapter = adaptador
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}