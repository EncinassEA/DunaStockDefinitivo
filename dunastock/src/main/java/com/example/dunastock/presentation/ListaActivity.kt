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

                    // 1. Extraemos los datos intentando con minúsculas (formato común en APIs)
                    val idPedido = obj.optString("id", obj.optString("Id", "00"))
                    val estado = obj.optString("estado", obj.optString("Estado", "Pendiente"))
                    val cantidad = obj.optString("cantidad", obj.optString("Cantidad", "1"))
                    val producto = obj.optString("producto", obj.optString("Producto", "Producto sin nombre"))

                    // 2. Armamos el texto para que se vea como: "pendiente • 45 Chamoy Mega"
                    val detalleReal = "$estado • $cantidad $producto"

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