package com.example.dunastock.presentation
import android.app.Activity
import android.os.Bundle
import com.example.dunastock.R

class ListaActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carga la pantalla de la lista que acabas de hacer
        setContentView(R.layout.activity_lista)
    }
}