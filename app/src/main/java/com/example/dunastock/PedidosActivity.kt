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

    private val API_URL = "https://dunastock-api.onrender.com/api/pedidos"
    private val USUARIOS_API_URL = "https://dunastock-api.onrender.com/api/usuarios"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos)

        val btnRegresar = findViewById<ImageButton>(R.id.btnRegresar)
        btnRegresar.setOnClickListener { finish() }

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

                    if (estadoActual != "completado") {
                        listaPedidos.add(Pedido(
                            obj.getString("id"),
                            obj.getString("producto"),
                            obj.getString("ubicacion_almacen"),
                            obj.getString("estado"),
                            obj.getString("asignado_a"),
                            obj.getString("fecha_ingreso"),
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

    private fun cargarOperadoresEnDropdown(tvOperador: AutoCompleteTextView) {
        val queue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(
            Request.Method.GET, USUARIOS_API_URL, null,
            { response ->
                val listaEquipo = mutableListOf<String>()
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    val nombre = obj.getString("nombre")
                    val rol = obj.getString("rol").lowercase()
                    listaEquipo.add("$nombre (${rol.replaceFirstChar { it.uppercase() }})")
                }
                val adapterOperadores = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaEquipo)
                tvOperador.setAdapter(adapterOperadores)
            },
            { Toast.makeText(this, "Error al cargar la lista", Toast.LENGTH_SHORT).show() }
        )
        queue.add(request)
    }

    private fun mostrarDialogoCrear() {
        val vistaDialogo = layoutInflater.inflate(R.layout.dialog_nuevo_pedido, null)

        val inputProd = vistaDialogo.findViewById<TextInputEditText>(R.id.etProducto)
        val inputCantidad = vistaDialogo.findViewById<TextInputEditText>(R.id.etCantidad)
        val inputCodigo = vistaDialogo.findViewById<TextInputEditText>(R.id.etCodigo)
        val inputUbic = vistaDialogo.findViewById<TextInputEditText>(R.id.etUbicacion)
        val inputNotas = vistaDialogo.findViewById<TextInputEditText>(R.id.etNotas)
        val tvOperador = vistaDialogo.findViewById<AutoCompleteTextView>(R.id.tvOperadorDesplegable)

        cargarOperadoresEnDropdown(tvOperador)

        MaterialAlertDialogBuilder(this)
            .setView(vistaDialogo)
            .setPositiveButton("Crear") { _, _ ->
                val productoTxt = inputProd.text.toString().trim()
                val cantidadTxt = inputCantidad.text.toString().trim()
                val codigoTxt = inputCodigo.text.toString().trim()
                val ubicacionTxt = inputUbic.text.toString().trim()
                val notasTxt = inputNotas.text.toString().trim()
                var asignadoTxt = tvOperador.text.toString().trim()

                if (productoTxt.isNotEmpty() && ubicacionTxt.isNotEmpty()) {
                    if (asignadoTxt.isEmpty()) asignadoTxt = "Por asignar"
                    val formatoFecha = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val fechaHoy = formatoFecha.format(java.util.Date())

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
        val vistaDialogo = layoutInflater.inflate(R.layout.dialog_nuevo_pedido, null)

        val inputProd = vistaDialogo.findViewById<TextInputEditText>(R.id.etProducto)
        val inputCantidad = vistaDialogo.findViewById<TextInputEditText>(R.id.etCantidad)
        val inputCodigo = vistaDialogo.findViewById<TextInputEditText>(R.id.etCodigo)
        val inputUbic = vistaDialogo.findViewById<TextInputEditText>(R.id.etUbicacion)
        val inputNotas = vistaDialogo.findViewById<TextInputEditText>(R.id.etNotas)
        val tvOperador = vistaDialogo.findViewById<AutoCompleteTextView>(R.id.tvOperadorDesplegable)

        inputProd.setText(pedido.producto)
        inputCantidad.setText(pedido.cantidad)
        inputCodigo.setText(pedido.codigo)
        inputUbic.setText(pedido.ubicacion_almacen)
        inputNotas.setText(pedido.notas)
        tvOperador.setText(pedido.asignado_a, false)

        cargarOperadoresEnDropdown(tvOperador)

        MaterialAlertDialogBuilder(this)
            .setView(vistaDialogo)
            .setTitle("Editar Pedido")
            .setPositiveButton("Guardar Cambios") { _, _ ->
                val productoTxt = inputProd.text.toString().trim()
                val cantidadTxt = inputCantidad.text.toString().trim()
                val codigoTxt = inputCodigo.text.toString().trim()
                val ubicacionTxt = inputUbic.text.toString().trim()
                val notasTxt = inputNotas.text.toString().trim()
                var asignadoTxt = tvOperador.text.toString().trim()

                if (productoTxt.isNotEmpty() && ubicacionTxt.isNotEmpty()) {
                    if (asignadoTxt.isEmpty()) asignadoTxt = "Por asignar"

                    val json = JSONObject().apply {
                        val idNum = pedido.id.toInt()
                        put("id", idNum)
                        put("Id", idNum)
                        put("producto", productoTxt)
                        put("Producto", productoTxt)
                        put("ubicacion_almacen", ubicacionTxt)
                        put("UbicacionAlmacen", ubicacionTxt)
                        put("estado", pedido.estado.lowercase())
                        put("Estado", pedido.estado.lowercase())
                        put("asignado_a", asignadoTxt)
                        put("AsignadoA", asignadoTxt)
                        put("fecha_ingreso", pedido.fecha_ingreso)
                        put("FechaIngreso", pedido.fecha_ingreso)
                        put("cantidad", cantidadTxt)
                        put("codigo", codigoTxt)
                        put("notas", notasTxt)
                    }

                    Volley.newRequestQueue(this).add(
                        JsonObjectRequest(Request.Method.PUT, "$API_URL/${pedido.id}", json,
                            {
                                Toast.makeText(this, "Pedido Actualizado", Toast.LENGTH_SHORT).show()
                                cargarPedidos()
                            },
                            { error ->
                                // --- EL BYPASS PARA EL CÓDIGO 204 DE .NET ---
                                val response = error.networkResponse
                                if (response != null && (response.statusCode == 204 || response.statusCode == 200)) {
                                    Toast.makeText(this, "Pedido Actualizado", Toast.LENGTH_SHORT).show()
                                    cargarPedidos()
                                } else {
                                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    )
                } else {
                    Toast.makeText(this, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
        var estadoSeguro = nuevoEstado.lowercase()
        if (estadoSeguro.contains("completar")) estadoSeguro = "completado"
        if (estadoSeguro.contains("proceso")) estadoSeguro = "en proceso"

        val asignadoFinal = if (estadoSeguro == "completado") nombreActual else pedido.asignado_a

        val json = JSONObject().apply {
            val idNum = pedido.id.toInt()
            put("id", idNum)
            put("Id", idNum)
            put("producto", pedido.producto)
            put("Producto", pedido.producto)
            put("ubicacion_almacen", pedido.ubicacion_almacen)
            put("UbicacionAlmacen", pedido.ubicacion_almacen)
            put("estado", estadoSeguro)
            put("Estado", estadoSeguro)
            put("fecha_ingreso", pedido.fecha_ingreso)
            put("FechaIngreso", pedido.fecha_ingreso)
            put("asignado_a", asignadoFinal)
            put("AsignadoA", asignadoFinal)
            put("cantidad", pedido.cantidad)
            put("codigo", pedido.codigo)
            put("notas", pedido.notas)
        }

        Volley.newRequestQueue(this).add(
            JsonObjectRequest(Request.Method.PUT, "$API_URL/${pedido.id}", json,
                {
                    Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
                    cargarPedidos()
                },
                { error ->
                    // --- EL BYPASS PARA EL CÓDIGO 204 DE .NET ---
                    val response = error.networkResponse
                    if (response != null && (response.statusCode == 204 || response.statusCode == 200)) {
                        Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
                        cargarPedidos()
                    } else {
                        Toast.makeText(this, "Error al actualizar el estado", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        )
    }
}