package com.example.dunastock.presentation

import com.example.dunastock.R
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import java.net.URL
import org.json.JSONArray
import kotlin.concurrent.thread

class MainActivity : Activity() {

    // Variables para vigilar los cambios
    private var totalPedidosActuales = 0

    // Configuramos el temporizador para consultar en segundo plano (cada 15 seg)
    private val handler = Handler(Looper.getMainLooper())
    private val intervaloDeConsulta = 15000L

    private val vigilanteAPI = object : Runnable {
        override fun run() {
            revisarNuevosPedidosReal()
            handler.postDelayed(this, intervaloDeConsulta)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Arrancamos el ciclo de consultas continuas al abrir la app
        handler.post(vigilanteAPI)

        // Botón para ir a la lista de órdenes
        val btnVerOrdenes = findViewById<Button>(R.id.btnVerOrdenes)
        btnVerOrdenes?.setOnClickListener {
            val intent = Intent(this, ListaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Apagamos el vigilante al salir para proteger la batería del reloj
        handler.removeCallbacks(vigilanteAPI)
    }

    private fun revisarNuevosPedidosReal() {
        thread {
            try {
                val respuesta = URL("https://dunastock-api.onrender.com/api/pedidos").readText()
                val arregloPedidos = JSONArray(respuesta)
                val totalEnLaNube = arregloPedidos.length()

                var nuevas = 0
                var proceso = 0
                var completadas = 0

                // Clasificamos cada pedido según su estado real en la API
                for (i in 0 until arregloPedidos.length()) {
                    val obj = arregloPedidos.getJSONObject(i)
                    val estado = obj.optString("estado", "nueva").lowercase()

                    when {
                        estado.contains("proceso") -> proceso++
                        estado.contains("completad") || estado.contains("lista") -> completadas++
                        else -> nuevas++
                    }
                }

                runOnUiThread {
                    // Mapeamos los totales a los TextView de la pantalla del reloj
                    findViewById<TextView>(R.id.tvNuevas)?.text = nuevas.toString()
                    findViewById<TextView>(R.id.tvProceso)?.text = proceso.toString()
                    findViewById<TextView>(R.id.tvListas)?.text = completadas.toString()

                    // Si detectamos que hay más pedidos que antes, disparamos la notificación
                    if (totalPedidosActuales in 1..<totalEnLaNube) {
                        lanzarNotificacionReal()
                    }
                    totalPedidosActuales = totalEnLaNube
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun lanzarNotificacionReal() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "canal_pedidos"

        val channel = NotificationChannel(channelId, "Pedidos Express", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, NotificacionActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("DunaStock: ¡Nuevo Pedido!")
            .setContentText("El pedido se ha registrado exitosamente")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }
}