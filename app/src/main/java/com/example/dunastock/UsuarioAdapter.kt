package com.example.dunastock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuarioAdapter(
    private val listaUsuarios: MutableList<Usuario>,
    private val onEditClick: (Usuario) -> Unit,
    private val onDeleteClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvRol: TextView = view.findViewById(R.id.tvRol)
        val btnEditar: TextView = view.findViewById(R.id.btnEditar)
        val btnBorrar: TextView = view.findViewById(R.id.btnBorrar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = listaUsuarios[position]
        holder.tvNombre.text = usuario.nombre

        // Convertimos la primera letra del rol a mayúscula para que se vea mejor
        val rolFormateado = usuario.rol.replaceFirstChar { it.uppercase() }
        holder.tvRol.text = "Rol: $rolFormateado"

        // Acciones al hacer clic en los emojis
        holder.btnEditar.setOnClickListener { onEditClick(usuario) }
        holder.btnBorrar.setOnClickListener { onDeleteClick(usuario) }
    }

    override fun getItemCount() = listaUsuarios.size
}