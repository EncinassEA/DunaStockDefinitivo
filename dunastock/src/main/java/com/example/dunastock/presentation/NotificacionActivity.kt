package com.example.dunastock.presentation

import com.example.dunastock.R
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class NotificacionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carga tu diseño visual de "NUEVO PEDIDO"
        setContentView(R.layout.activity_notificacion)

        // Funcionalidad del botón PREPARAR para regresar al menú de semáforos
        val btnPreparar = findViewById<Button>(R.id.btnPreparar)
        btnPreparar?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra esta pantalla
        }
    }
}