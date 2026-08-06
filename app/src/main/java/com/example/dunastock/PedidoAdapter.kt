package com.example.dunastock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

        // NUEVO: Agregamos el enlace al botón de detalles
        val btnDetalles: Button = view.findViewById(R.id.btnDetalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = listaPedidos[position]
        holder.tvProducto.text = pedido.producto
        holder.tvUbicacion.text = "📍 Ubicación: ${pedido.ubicacion_almacen}"

        val estadoActual = pedido.estado.uppercase()
        holder.tvEstado.text = estadoActual

        if (estadoActual == "COMPLETADO") {
            holder.tvAsignado.text = "✅ Completado por: ${pedido.asignado_a}"
        } else {
            holder.tvAsignado.text = "👤 Asignado a: ${pedido.asignado_a}"
        }

        // LÓGICA DE ROLES PARA OCULTAR/MOSTRAR BOTONES
        if (rolUsuario == "historial_readonly") {
            holder.btnEditar.visibility = View.GONE
            holder.btnBorrar.visibility = View.GONE
            holder.btnProceso.visibility = View.GONE
            holder.btnCompletar.visibility = View.GONE
        } else if (rolUsuario == "administrador") {
            holder.btnProceso.visibility = View.GONE
            holder.btnCompletar.visibility = View.GONE
            holder.btnEditar.visibility = View.VISIBLE
            holder.btnBorrar.visibility = View.VISIBLE
        } else {
            holder.btnEditar.visibility = View.GONE
            holder.btnBorrar.visibility = View.GONE
            holder.btnProceso.visibility = View.VISIBLE
            holder.btnCompletar.visibility = View.VISIBLE

            if (estadoActual == "COMPLETADO") {
                holder.btnProceso.visibility = View.GONE
                holder.btnCompletar.visibility = View.GONE
            }
        }

        // Clics existentes
        holder.btnEditar.setOnClickListener { onEditClick(pedido) }
        holder.btnBorrar.setOnClickListener { onDeleteClick(pedido) }
        holder.btnProceso.setOnClickListener { onStatusChangeClick(pedido, "proceso") }
        holder.btnCompletar.setOnClickListener { onStatusChangeClick(pedido, "completado") }

        // NUEVO: Lógica del botón de detalles para mostrar la alerta
        holder.btnDetalles.setOnClickListener {
            MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle("📦 Detalles del Pedido")
                .setMessage(
                    "Producto: ${pedido.producto}\n" +
                            "Ubicación: ${pedido.ubicacion_almacen}\n\n" +
                            "🔢 Cantidad: ${pedido.cantidad}\n" +
                            "🏷️ Código/SKU: ${pedido.codigo}\n\n" +
                            "📝 Notas:\n${pedido.notas}"
                )
                .setPositiveButton("Cerrar", null)
                .show()
        }
    }

    override fun getItemCount() = listaPedidos.size
}