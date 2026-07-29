package com.example.dunastock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
import org.json.JSONException
import org.json.JSONObject

class AdminUsuariosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAgregar: Button
    private lateinit var adapter: UsuarioAdapter
    private val listaUsuarios = mutableListOf<Usuario>()

    // Tu enlace de MockAPI
    private val API_URL = "https://6a388f6a64a2d8269222907e.mockapi.io/usuarios"

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

    private fun mostrarDialogoEditar(usuario: Usuario) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Usuario")

        // Creamos un contenedor con dos campos de texto (Nombre y Rol)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre"
        inputNombre.setText(usuario.nombre)
        layout.addView(inputNombre)

        val inputRol = EditText(this)
        inputRol.hint = "Rol (administrador o operador)"
        inputRol.setText(usuario.rol)
        layout.addView(inputRol)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val nuevoNombre = inputNombre.text.toString().trim()
            val nuevoRol = inputRol.text.toString().trim().lowercase()

            if(nuevoNombre.isNotEmpty() && nuevoRol.isNotEmpty()) {
                actualizarUsuarioEnApi(usuario, nuevoNombre, nuevoRol)
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun actualizarUsuarioEnApi(usuario: Usuario, nuevoNombre: String, nuevoRol: String) {
        val queue = Volley.newRequestQueue(this)

        // Empaquetamos los datos modificados
        val jsonBody = JSONObject()
        jsonBody.put("nombre", nuevoNombre)
        jsonBody.put("correo", usuario.correo)
        jsonBody.put("password_hash", usuario.password_hash)
        jsonBody.put("rol", nuevoRol)

        val request = JsonObjectRequest(Request.Method.PUT, "$API_URL/${usuario.id}", jsonBody,
            { response ->
                Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                cargarUsuarios()
            },
            { error ->
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
}