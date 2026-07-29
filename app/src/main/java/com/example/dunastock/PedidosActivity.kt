package com.example.dunastock

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.*
import org.json.JSONObject

class PedidosActivity : AppCompatActivity() {

    private lateinit var rvPedidos: RecyclerView
    private lateinit var btnNuevoPedido: Button
    private lateinit var adapter: PedidoAdapter

    private var nombreActual = ""
    private val listaPedidos = mutableListOf<Pedido>()
    private val API_URL = "https://6a388f6a64a2d8269222907e.mockapi.io/pedidos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos)

        val btnRegresar = findViewById<ImageButton>(R.id.btnRegresar)
        btnRegresar.setOnClickListener {
            finish()
        }

        val rol = intent.getStringExtra("ROL_USUARIO") ?: "operador"
        nombreActual = intent.getStringExtra("NOMBRE_USUARIO") ?: "Usuario"

        rvPedidos = findViewById(R.id.rvPedidos)
        btnNuevoPedido = findViewById(R.id.btnNuevoPedido)
        rvPedidos.layoutManager = LinearLayoutManager(this)

        if (rol == "operador") btnNuevoPedido.visibility = View.GONE

        adapter = PedidoAdapter(listaPedidos, rol,
            { pedido -> mostrarDialogoEditar(pedido) },
            { pedido -> mostrarAlertaBorrar(pedido) },
            { pedido, nuevoEstado -> actualizarEstado(pedido, nuevoEstado) }
        )
        rvPedidos.adapter = adapter

        btnNuevoPedido.setOnClickListener { mostrarDialogoCrear() }
        cargarPedidos()
    }

    private fun cargarPedidos() {
        val queue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(Request.Method.GET, API_URL, null,
            { response ->
                listaPedidos.clear()
                listaPedidos.clear()
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    val estadoActual = obj.getString("estado").lowercase()

                    // Aquí está la magia: SOLO agregamos si NO está completado
                    if (estadoActual != "completado") {
                        listaPedidos.add(Pedido(
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
            }, { Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show() })
        queue.add(request)
    }

    private fun mostrarDialogoCrear() {
        // Inflamos nuestro nuevo diseño personalizado
        val inflater = layoutInflater
        val vistaDialogo = inflater.inflate(R.layout.dialog_nuevo_pedido, null)

        // Conectamos los EditText del diseño
        val inputProd = vistaDialogo.findViewById<EditText>(R.id.etDialogProducto)
        val inputUbic = vistaDialogo.findViewById<EditText>(R.id.etDialogUbicacion)
        val inputAsignado = vistaDialogo.findViewById<EditText>(R.id.etDialogAsignado)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("📦 Crear Nuevo Pedido")
        builder.setView(vistaDialogo)
            .setPositiveButton("Crear") { _, _ ->
                val productoTxt = inputProd.text.toString().trim()
                val ubicacionTxt = inputUbic.text.toString().trim()
                var asignadoTxt = inputAsignado.text.toString().trim()

                // Si lo dejan vacío, le ponemos algo por defecto
                if (asignadoTxt.isEmpty()) asignadoTxt = "Por asignar"

                // Obtener la fecha real de hoy
                val formatoFecha = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val fechaHoy = formatoFecha.format(java.util.Date())

                val json = JSONObject().apply {
                    put("producto", productoTxt)
                    put("ubicacion_almacen", ubicacionTxt)
                    put("estado", "pendiente") // Siempre nace como pendiente
                    put("asignado_a", asignadoTxt)
                    put("fecha_ingreso", fechaHoy) // Ahora guarda el día real
                }

                Volley.newRequestQueue(this).add(
                    JsonObjectRequest(Request.Method.POST, API_URL, json,
                        { cargarPedidos() }, null)
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditar(pedido: Pedido) {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this).apply { setText(pedido.producto) }
        builder.setTitle("Editar Pedido").setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val json = JSONObject().apply { put("producto", input.text.toString()); put("estado", pedido.estado) }
                Volley.newRequestQueue(this).add(JsonObjectRequest(Request.Method.PUT, "$API_URL/${pedido.id}", json, { cargarPedidos() }, null))
            }.show()
    }

    private fun mostrarAlertaBorrar(pedido: Pedido) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar").setMessage("¿Seguro?").setPositiveButton("Sí") { _, _ -> borrarPedido(pedido.id) }
            .setNegativeButton("No", null).show()
    }

    private fun borrarPedido(id: String) {
        Volley.newRequestQueue(this).add(StringRequest(Request.Method.DELETE, "$API_URL/$id", { cargarPedidos() }, null))
    }

    private fun actualizarEstado(pedido: Pedido, nuevoEstado: String) {
        val json = JSONObject().apply {
            put("producto", pedido.producto)
            put("estado", nuevoEstado)

            // Si lo están completando, guardamos quién lo hizo
            if (nuevoEstado == "completado") {
                put("asignado_a", nombreActual)
            }
        }
        Volley.newRequestQueue(this).add(JsonObjectRequest(Request.Method.PUT, "$API_URL/${pedido.id}", json, { cargarPedidos() }, null))
    }
}