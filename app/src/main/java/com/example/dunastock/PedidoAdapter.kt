package com.example.dunastock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PedidoAdapter(
    private val listaPedidos: MutableList<Pedido>,
    private val rolUsuario: String,
    private val onEditClick: (Pedido) -> Unit,
    private val onDeleteClick: (Pedido) -> Unit,
    private val onStatusChangeClick: (Pedido, String) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProducto: TextView = view.findViewById(R.id.tvProducto)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvUbicacion: TextView = view.findViewById(R.id.tvUbicacion)
        val tvAsignado: TextView = view.findViewById(R.id.tvAsignado)

        val btnEditar: TextView = view.findViewById(R.id.btnEditar)
        val btnBorrar: TextView = view.findViewById(R.id.btnBorrar)
        val btnProceso: Button = view.findViewById(R.id.btnProceso)
        val btnCompletar: Button = view.findViewById(R.id.btnCompletar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = listaPedidos[position]
        holder.tvProducto.text = pedido.producto
        holder.tvUbicacion.text = "📍 Ubicación: ${pedido.ubicacion_almacen}"

        // ⚠️ CORRECCIÓN: Primero declaramos la variable aquí arriba
        val estadoActual = pedido.estado.uppercase()
        holder.tvEstado.text = estadoActual

        // Y ahora sí la evaluamos en el IF
        if (estadoActual == "COMPLETADO") {
            holder.tvAsignado.text = "✅ Completado por: ${pedido.asignado_a}"
        } else {
            holder.tvAsignado.text = "👤 Asignado a: ${pedido.asignado_a}"
        }

        // LÓGICA DE ROLES PARA OCULTAR/MOSTRAR BOTONES
        if (rolUsuario == "historial_readonly") {
            // Ocultar todos los botones para la vista de Historial
            holder.btnEditar.visibility = View.GONE
            holder.btnBorrar.visibility = View.GONE
            holder.btnProceso.visibility = View.GONE
            holder.btnCompletar.visibility = View.GONE
        } else if (rolUsuario == "administrador") {
            // Lógica normal del Administrador
            holder.btnProceso.visibility = View.GONE
            holder.btnCompletar.visibility = View.GONE
            holder.btnEditar.visibility = View.VISIBLE
            holder.btnBorrar.visibility = View.VISIBLE
        } else {
            // Lógica normal del Operador
            holder.btnEditar.visibility = View.GONE
            holder.btnBorrar.visibility = View.GONE
            holder.btnProceso.visibility = View.VISIBLE
            holder.btnCompletar.visibility = View.VISIBLE

            if (estadoActual == "COMPLETADO") {
                holder.btnProceso.visibility = View.GONE
                holder.btnCompletar.visibility = View.GONE
            }
        }

        holder.btnEditar.setOnClickListener { onEditClick(pedido) }
        holder.btnBorrar.setOnClickListener { onDeleteClick(pedido) }
        holder.btnProceso.setOnClickListener { onStatusChangeClick(pedido, "proceso") }
        holder.btnCompletar.setOnClickListener { onStatusChangeClick(pedido, "completado") }
    }

    override fun getItemCount() = listaPedidos.size
}