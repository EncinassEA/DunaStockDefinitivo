package com.example.dunastock

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley

class HistorialActivity : AppCompatActivity() {

    private lateinit var rvHistorial: RecyclerView
    private lateinit var adapter: PedidoAdapter
    private val listaHistorial = mutableListOf<Pedido>()
    private val API_URL = "https://6a388f6a64a2d8269222907e.mockapi.io/pedidos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        // ==========================================
        // CONFIGURACIÓN DEL BOTÓN DE REGRESAR
        // ==========================================
        val btnRegresar = findViewById<ImageButton>(R.id.btnRegresar)
        btnRegresar.setOnClickListener {
            finish() // Cierra la pantalla y te devuelve al Dashboard
        }
        // ==========================================

        rvHistorial = findViewById(R.id.rvHistorial)
        rvHistorial.layoutManager = LinearLayoutManager(this)

        // Usamos "historial_readonly" como rol para que el Adapter oculte
        // automáticamente los botones de Editar, Borrar y Cambiar Estado.
        adapter = PedidoAdapter(listaHistorial, "historial_readonly",
            { /* No hace nada en historial */ },
            { /* No hace nada en historial */ },
            { _, _ -> /* No hace nada en historial */ }
        )
        rvHistorial.adapter = adapter

        cargarHistorial()
    }

    private fun cargarHistorial() {
        val queue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(
            Request.Method.GET, API_URL, null,
            { response ->
                listaHistorial.clear()
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    val estadoActual = obj.getString("estado").lowercase()

                    // MAGIA INVERSA: AQUÍ SOLO AGREGAMOS SI ES "COMPLETADO"
                    if (estadoActual == "completado") {
                        listaHistorial.add(Pedido(
                            obj.getString("id"),
                            obj.getString("producto"),
                            obj.getString("ubicacion_almacen"),
                            obj.getString("estado"),
                            obj.getString("asignado_a"),
                            obj.getString("fecha_ingreso")
                        ))
                    }
                }
                adapter.notifyDataSetChanged()

                if (listaHistorial.isEmpty()) {
                    Toast.makeText(this, "No hay pedidos en el historial.", Toast.LENGTH_SHORT).show()
                }
            },
            { Toast.makeText(this, "Error de red al cargar el historial.", Toast.LENGTH_SHORT).show() }
        )
        queue.add(request)
    }
}