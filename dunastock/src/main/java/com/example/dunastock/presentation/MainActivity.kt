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
    private val handler = Handler(Looper.getMainLooper())
    private val intervaloDeConsulta = 3000L // 3 segundos

    // Este es el "vigilante" que se ejecuta cada 3 segundos
    private val vigilanteAPI = object : Runnable {
        override fun run() {
            revisarNuevosPedidosReal()
            handler.postDelayed(this, intervaloDeConsulta)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Arrancamos el vigilante para que empiece a revisar MockAPI
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
        // Apagamos el vigilante si se cierra la app para no gastar memoria
        handler.removeCallbacks(vigilanteAPI)
    }

    private fun revisarNuevosPedidosReal() {
        thread {
            try {
                // Leemos MockAPI
                val respuesta = URL("https://6a388f6a64a2d8269222907e.mockapi.io/pedidos").readText()
                val arregloPedidos = JSONArray(respuesta)
                val totalEnLaNube = arregloPedidos.length()

                runOnUiThread {
                    // 1. Actualizamos el número rojo en la pantalla siempre
                    val tvNuevas = findViewById<TextView>(R.id.tvNuevas)
                    tvNuevas?.text = totalEnLaNube.toString()

                    // 2. Lógica para la notificación
                    if (totalPedidosActuales == 0) {
                        // Es la primera vez que abre la app, solo guardamos cuántos hay (ej. 3)
                        totalPedidosActuales = totalEnLaNube
                    } else if (totalEnLaNube > totalPedidosActuales) {
                        // ¡MAGIA REAL! El número en MockAPI subió (ej. de 3 a 4).
                        totalPedidosActuales = totalEnLaNube
                        lanzarNotificacionReal() // Disparamos la alerta
                    }
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