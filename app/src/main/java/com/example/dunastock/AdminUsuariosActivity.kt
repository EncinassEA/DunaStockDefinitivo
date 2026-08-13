package com.example.dunastock

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject

class AdminUsuariosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAgregar: Button
    private lateinit var adapter: UsuarioAdapter
    private val listaUsuarios = mutableListOf<Usuario>()

    // Tu enlace de MockAPI
    private val API_URL = "https://dunastock-api.onrender.com/api/usuarios"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_usuarios)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ==========================================
        // CONFIGURACIÓN DEL BOTÓN DE REGRESAR
        // ==========================================
        val btnRegresar = findViewById<ImageButton>(R.id.btnRegresar)
        btnRegresar.setOnClickListener {
            finish() // Esto cierra la pantalla y te devuelve al menú anterior
        }
        // ==========================================

        btnAgregar = findViewById(R.id.btnAgregar)
        recyclerView = findViewById(R.id.recyclerViewUsuarios)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializamos el adaptador con las funciones para editar y borrar
        adapter = UsuarioAdapter(listaUsuarios,
            onEditClick = { usuario -> mostrarDialogoEditar(usuario) },
            onDeleteClick = { usuario -> borrarUsuario(usuario.id) }
        )
        recyclerView.adapter = adapter

        // Botón para ir a crear un nuevo operador
        btnAgregar.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Cada vez que entremos a esta pantalla, recargamos la lista desde MockAPI
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        val queue = Volley.newRequestQueue(this)
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, API_URL, null,
            { response ->
                listaUsuarios.clear()
                try {
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val id = obj.getString("id")
                        val nombre = obj.getString("nombre")
                        val correo = obj.getString("correo")
                        val password = obj.getString("password_hash")
                        val rol = obj.getString("rol")

                        listaUsuarios.add(Usuario(id, nombre, correo, password, rol))
                    }
                    adapter.notifyDataSetChanged() // Refrescamos la lista visual
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(jsonArrayRequest)
    }

    private fun borrarUsuario(id: String) {
        val queue = Volley.newRequestQueue(this)
        val request = object : StringRequest(Method.DELETE, "$API_URL/$id",
            { response ->
                Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                cargarUsuarios() // Recargamos la lista para que desaparezca
            },
            { error ->
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        ) {}
        queue.add(request)
    }

    // ==========================================
    // NUEVO DIÁLOGO MODERNO DE EDICIÓN
    // ==========================================
    private fun mostrarDialogoEditar(usuario: Usuario) {
        // 1. Inflamos el nuevo diseño personalizado
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_usuario, null)
        val etNombreEditar = dialogView.findViewById<TextInputEditText>(R.id.etNombreEditar)
        val tvRolDesplegable = dialogView.findViewById<AutoCompleteTextView>(R.id.tvRolDesplegable)

        // 2. Pre-llenamos el nombre actual del usuario
        etNombreEditar.setText(usuario.nombre)

        // 3. Configuramos las opciones del Dropdown
        val opcionesRol = arrayOf("Administrador", "Operador")
        val adapterRoles = ArrayAdapter(this, android.R.layout.simple_list_item_1, opcionesRol)
        tvRolDesplegable.setAdapter(adapterRoles)

        // Pre-seleccionamos el rol actual formateado (con mayúscula inicial para que coincida visualmente)
        val rolActualCapitalizado = usuario.rol.replaceFirstChar { it.uppercase() }
        tvRolDesplegable.setText(rolActualCapitalizado, false)

        // 4. Construimos el diálogo moderno
        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val nuevoNombre = etNombreEditar.text.toString().trim()
                val nuevoRol = tvRolDesplegable.text.toString().trim().lowercase() // Lo mandamos en minúscula a la API

                if (nuevoNombre.isNotEmpty() && nuevoRol.isNotEmpty()) {
                    actualizarUsuarioEnApi(usuario, nuevoNombre, nuevoRol)
                } else {
                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun actualizarUsuarioEnApi(usuario: Usuario, nuevoNombre: String, nuevoRol: String) {
        val queue = Volley.newRequestQueue(this)

        // 1. Empaquetamos los datos modificados con el "Blindaje" para C#
        val jsonBody = JSONObject().apply {
            val idNum = usuario.id.toInt()
            put("id", idNum)
            put("Id", idNum) // .NET exige el ID aquí adentro

            put("nombre", nuevoNombre)
            put("Nombre", nuevoNombre)

            put("correo", usuario.correo)
            put("Correo", usuario.correo)

            put("password_hash", usuario.password_hash)
            put("Password_hash", usuario.password_hash)

            put("rol", nuevoRol)
            put("Rol", nuevoRol)
        }

        val request = JsonObjectRequest(Request.Method.PUT, "$API_URL/${usuario.id}", jsonBody,
            { response ->
                Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                cargarUsuarios()
            },
            { error ->
                // 2. EL BYPASS PARA EL CÓDIGO 204 DE .NET
                val response = error.networkResponse
                if (response != null && (response.statusCode == 204 || response.statusCode == 200)) {
                    Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            }
        )
        queue.add(request)
    }
}