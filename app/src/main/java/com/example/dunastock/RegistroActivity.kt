package com.example.dunastock

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class RegistroActivity : AppCompatActivity() {

    private lateinit var etRegNombre: EditText
    private lateinit var etRegCorreo: EditText
    private lateinit var etRegPassword: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var tvVolverLogin: TextView

    private val API_URL = "https://dunastock-api.onrender.com/api/usuarios"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etRegNombre = findViewById(R.id.etRegNombre)
        etRegCorreo = findViewById(R.id.etRegCorreo)
        etRegPassword = findViewById(R.id.etRegPassword)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        tvVolverLogin = findViewById(R.id.tvVolverLogin)

        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }

        // Si se arrepiente y quiere regresar al Login
        tvVolverLogin.setOnClickListener {
            finish()
        }
    }

    private fun registrarUsuario() {
        val nombre = etRegNombre.text.toString().trim()
        val correo = etRegCorreo.text.toString().trim()
        val password = etRegPassword.text.toString().trim()

        // 1. Validar que no haya campos vacíos
        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Validar que la contraseña tenga al menos 8 caracteres
        if (password.length < 8) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_LONG).show()
            return
        }

        // 3. Validar el tipo de correo (Gmail o Hotmail)
        val correoMinusculas = correo.lowercase()
        val esGmail = correoMinusculas.endsWith("@gmail.com")
        val esHotmail = correoMinusculas.endsWith("@hotmail.com")

        if (!esGmail && !esHotmail) {
            Toast.makeText(this, "Solo se permiten correos @gmail.com o @hotmail.com", Toast.LENGTH_LONG).show()
            return
        }

        // Si pasa todas las pruebas, creamos el JSON para MockAPI
        val jsonBody = JSONObject()
        jsonBody.put("nombre", nombre)
        jsonBody.put("correo", correoMinusculas) // Lo guardamos en minúsculas por seguridad
        jsonBody.put("password_hash", password)
        jsonBody.put("rol", "operador") // Rol por defecto

        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, API_URL, jsonBody,
            { response ->
                Toast.makeText(this, "¡Usuario registrado con éxito!", Toast.LENGTH_LONG).show()
                finish() // Cierra la pantalla y lo regresa automáticamente al Login
            },
            { error ->
                Toast.makeText(this, "Error al registrar: verifique su conexión", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }
}