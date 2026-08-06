package com.example.dunastock

data class Pedido(
    val id: String,
    val producto: String,
    val ubicacion_almacen: String,
    var estado: String,
    val asignado_a: String,
    val fecha_ingreso: String,
    val cantidad: String = "",
    val codigo: String = "",
    val notas: String = ""
)