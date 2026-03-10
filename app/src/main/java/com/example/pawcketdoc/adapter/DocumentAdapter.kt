package com.example.pawcketdoc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pawcketdoc.R
import com.example.pawcketdoc.model.Document

class DocumentAdapter(
    private val documents: List<Document>,
    private val onClick: (Document) -> Unit
) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    class DocumentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDocumentIcon: ImageView = view.findViewById(R.id.imgDocumentIcon)
        val tvDocumentName: TextView = view.findViewById(R.id.tvDocumentName)
        val tvDocumentType: TextView = view.findViewById(R.id.tvDocumentType)
        val tvDateIssued: TextView = view.findViewById(R.id.tvDateIssued)
        val tvNotes: TextView = view.findViewById(R.id.tvNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_document, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val document = documents[position]

        holder.tvDocumentName.text = document.name
        holder.tvDocumentType.text = document.type
        holder.tvDateIssued.text = "Issued: ${document.dateIssued}"

        if (document.notes.isNotBlank()) {
            holder.tvNotes.text = document.notes
            holder.tvNotes.visibility = View.VISIBLE
        } else {
            holder.tvNotes.visibility = View.GONE
        }

        // replaces extension with .png so Cloudinary renders a preview for both PDFs and images
        val previewUrl = document.fileUrl
            .replaceAfterLast(".", "png")

        Glide.with(holder.itemView.context)
            .load(previewUrl)
            .placeholder(R.drawable.docfile)
            .error(R.drawable.docfile)
            .into(holder.imgDocumentIcon)

        holder.itemView.setOnClickListener { onClick(document) }
    }

    override fun getItemCount(): Int = documents.size
}