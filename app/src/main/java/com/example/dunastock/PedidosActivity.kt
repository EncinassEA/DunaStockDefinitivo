package com.example.dunastock

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
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
                            obj.getString("fecha_ingreso"),
                            // Agregamos esto para que lea los nuevos datos de MockAPI
                            obj.optString("cantidad", "N/A"),
                            obj.optString("codigo", "N/A"),
                            obj.optString("notas", "Sin notas")
                        ))
                    }
                }
                adapter.notifyDataSetChanged()
            }, { Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show() })
        queue.add(request)
    }


    private fun mostrarDialogoCrear() {
        // Inflamos nuestro nuevo diseño personalizado
        val vistaDialogo = layoutInflater.inflate(R.layout.dialog_nuevo_pedido, null)

        // Conectamos los TextInputEditText del nuevo diseño
        val inputProd = vistaDialogo.findViewById<TextInputEditText>(R.id.etProducto)
        val inputCantidad = vistaDialogo.findViewById<TextInputEditText>(R.id.etCantidad)
        val inputCodigo = vistaDialogo.findViewById<TextInputEditText>(R.id.etCodigo)
        val inputUbic = vistaDialogo.findViewById<TextInputEditText>(R.id.etUbicacion)
        val inputNotas = vistaDialogo.findViewById<TextInputEditText>(R.id.etNotas)
        val tvOperador = vistaDialogo.findViewById<AutoCompleteTextView>(R.id.tvOperadorDesplegable)

        // Configurar el menú desplegable con el equipo
        val opcionesOperador = arrayOf("Osvaldo", "Carlos Melendrez", "Eclud", "Alan")
        val adapterOperadores = ArrayAdapter(this, android.R.layout.simple_list_item_1, opcionesOperador)
        tvOperador.setAdapter(adapterOperadores)

        // Usamos MaterialAlertDialogBuilder para un diseño más limpio
        MaterialAlertDialogBuilder(this)
            .setView(vistaDialogo)
            .setPositiveButton("Crear") { _, _ ->
                val productoTxt = inputProd.text.toString().trim()
                val cantidadTxt = inputCantidad.text.toString().trim()
                val codigoTxt = inputCodigo.text.toString().trim()
                val ubicacionTxt = inputUbic.text.toString().trim()
                val notasTxt = inputNotas.text.toString().trim()
                var asignadoTxt = tvOperador.text.toString().trim()

                // Validar que al menos pongan el producto y la ubicación
                if (productoTxt.isNotEmpty() && ubicacionTxt.isNotEmpty()) {

                    if (asignadoTxt.isEmpty()) asignadoTxt = "Por asignar"

                    // Obtener la fecha real de hoy
                    val formatoFecha = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val fechaHoy = formatoFecha.format(java.util.Date())

                    // Empaquetamos todo, incluyendo los campos nuevos para MockAPI
                    val json = JSONObject().apply {
                        put("producto", productoTxt)
                        put("cantidad", cantidadTxt)
                        put("codigo", codigoTxt)
                        put("ubicacion_almacen", ubicacionTxt)
                        put("notas", notasTxt)
                        put("estado", "pendiente")
                        put("asignado_a", asignadoTxt)
                        put("fecha_ingreso", fechaHoy)
                    }

                    Volley.newRequestQueue(this).add(
                        JsonObjectRequest(Request.Method.POST, API_URL, json,
                            {
                                Toast.makeText(this, "Pedido Creado", Toast.LENGTH_SHORT).show()
                                cargarPedidos()
                            },
                            { Toast.makeText(this, "Error al crear", Toast.LENGTH_SHORT).show() }
                        )
                    )
                } else {
                    Toast.makeText(this, "Producto y Ubicación son obligatorios", Toast.LENGTH_SHORT).show()
                }
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