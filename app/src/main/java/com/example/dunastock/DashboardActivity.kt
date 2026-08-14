package com.example.dunastock

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class DashboardActivity : AppCompatActivity() {

    private lateinit var cardOrdenes: CardView
    private lateinit var cardHistorial: CardView
    private lateinit var cardAdmin: CardView
    private lateinit var tvBienvenida: TextView
    private lateinit var btnMenu: TextView

    private lateinit var tvStatPendientes: TextView
    private lateinit var tvStatProceso: TextView
    private lateinit var tvStatCompletados: TextView

    private val PEDIDOS_API_URL = "https://dunastock-api.onrender.com/api/pedidos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de vistas
        cardOrdenes = findViewById(R.id.cardOrdenes)
        cardHistorial = findViewById(R.id.cardHistorial)
        cardAdmin = findViewById(R.id.cardAdmin)
        tvBienvenida = findViewById(R.id.tvBienvenida)
        btnMenu = findViewById(R.id.btnMenu)

        tvStatPendientes = findViewById(R.id.tvStatPendientes)
        tvStatProceso = findViewById(R.id.tvStatProceso)
        tvStatCompletados = findViewById(R.id.tvStatCompletados)

        // ==========================================
        // TRADUCCIÓN DEL ROL Y VISIBILIDAD
        // ==========================================
        val rolCrudo = intent.getStringExtra("ROL_USUARIO") ?: "operador"
        val nombreUsuario = intent.getStringExtra("NOMBRE_USUARIO") ?: "Usuario"

        // Detectamos si es administrador buscando "adm"
        val esAdmin = rolCrudo.contains("adm", ignoreCase = true)

        // Mejoramos el mensaje de bienvenida para que se vea profesional
        val nombreRolBonito = if (esAdmin) "Administrador" else "Operador"
        tvBienvenida.text = "¡Bienvenido, $nombreRolBonito!"

        // Ocultamos la tarjeta si NO es administrador
        if (!esAdmin) {
            cardAdmin.visibility = View.GONE
        }
        // ==========================================

        // Menú lateral (Cerrar sesión)
        btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add(0, 1, 0, "👤 $nombreUsuario").isEnabled = false
            popup.menu.add(0, 2, 0, "🚪 Cerrar Sesión")
            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == 2) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                true
            }
            popup.show()
        }

        // Acciones de las tarjetas
        cardOrdenes.setOnClickListener {
            val intent = Intent(this, PedidosActivity::class.java)
            // Mandamos el rol original para que PedidosActivity haga sus propios filtros
            intent.putExtra("ROL_USUARIO", rolCrudo)
            intent.putExtra("NOMBRE_USUARIO", nombreUsuario)
            startActivity(intent)
        }

        cardHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        cardAdmin.setOnClickListener {
            startActivity(Intent(this, AdminUsuariosActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarEstadisticas()
    }

    private fun cargarEstadisticas() {
        val queue = Volley.newRequestQueue(this)
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, PEDIDOS_API_URL, null,
            { response ->
                var pendientes = 0
                var enProceso = 0
                var completados = 0

                try {
                    for (i in 0 until response.length()) {
                        val orden = response.getJSONObject(i)

                        // Extraemos el texto, lo pasamos a minúsculas y cortamos espacios fantasma
                        val estado = orden.getString("estado").lowercase().trim()

                        // Usamos 'contains' para que los detecte a la fuerza sin importar ligeros errores de tipeo
                        if (estado.contains("pendiente")) {
                            pendientes++
                        } else if (estado.contains("proceso")) {
                            enProceso++
                        } else if (estado.contains("completado")) {
                            completados++
                        }
                    }
                    tvStatPendientes.text = pendientes.toString()
                    tvStatProceso.text = enProceso.toString()
                    tvStatCompletados.text = completados.toString()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            {
                tvStatPendientes.text = "0"
                tvStatProceso.text = "0"
                tvStatCompletados.text = "0"
            }
        )
        queue.add(jsonArrayRequest)
    }
}