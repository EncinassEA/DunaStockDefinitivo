package com.example.dunastock

import android.content.Intent
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
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class MainActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvCrearCuenta: TextView

    private val API_URL = "https://6a388f6a64a2d8269222907e.mockapi.io/usuarios"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvCrearCuenta = findViewById(R.id.tvCrearCuenta)

        btnLogin.setOnClickListener {
            validarLogin(etCorreo.text.toString().trim(), etPassword.text.toString().trim())
        }

        // Navegar a la pantalla de registro
        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validarLogin(correoIngresado: String, passwordIngresado: String) {
        if (correoIngresado.isEmpty() || passwordIngresado.isEmpty()) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // --- VALIDACIÓN ESTRICTA DE CORREO ---
        val correoMinusculas = correoIngresado.lowercase().trim()
        val esGmail = correoMinusculas.endsWith("@gmail.com")
        val esHotmail = correoMinusculas.endsWith("@hotmail.com")

        if (!esGmail && !esHotmail) {
            Toast.makeText(this, "Ingresa un correo válido (@gmail.com o @hotmail.com)", Toast.LENGTH_LONG).show()
            return
        }
        // -------------------------------------------

        val queue = Volley.newRequestQueue(this)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, API_URL, null,
            { response ->
                var usuarioEncontrado = false
                try {
                    for (i in 0 until response.length()) {
                        val usuario = response.getJSONObject(i)
                        val correoApi = usuario.getString("correo")
                        val passwordApi = usuario.getString("password_hash")
                        val rolApi = usuario.getString("rol")

                        // Extraemos el nombre directamente de tu MockAPI
                        val nombreApi = usuario.getString("nombre")

                        // Validamos que el correo y contraseña coincidan
                        if (correoMinusculas == correoApi.lowercase() && passwordIngresado == passwordApi) {
                            usuarioEncontrado = true

                            val intent = Intent(this, DashboardActivity::class.java)
                            intent.putExtra("ROL_USUARIO", rolApi)
                            intent.putExtra("NOMBRE_USUARIO", nombreApi) // Pasamos el nombre al Dashboard

                            startActivity(intent)
                            finish() // Evita que regresen al Login presionando "Atrás"
                            break
                        }
                    }
                    if (!usuarioEncontrado) {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(jsonArrayRequest)
    }
}