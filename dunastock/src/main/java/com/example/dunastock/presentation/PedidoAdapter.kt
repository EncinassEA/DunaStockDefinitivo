package com.example.dunastock.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dunastock.R

// Un modelo de datos sencillo para representar el pedido
data class Pedido(val id: String, val detalles: String)

class PedidoAdapter(private val pedidos: List<Pedido>) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderNumber: TextView = view.findViewById(R.id.tvOrderNumber)
        val tvOrderDetails: TextView = view.findViewById(R.id.tvOrderDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.tvOrderNumber.text = "#${pedido.id}"
        holder.tvOrderDetails.text = pedido.detalles
    }

    override fun getItemCount() = pedidos.size
}