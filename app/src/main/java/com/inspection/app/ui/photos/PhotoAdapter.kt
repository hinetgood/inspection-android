package com.inspection.app.ui.photos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.inspection.app.data.entity.Photo
import com.inspection.app.databinding.ItemPhotoBinding
import java.io.File

class PhotoAdapter(
    private val onItemClick: (Photo) -> Unit,
    private val onDeleteClick: (Photo) -> Unit
) : ListAdapter<Photo, PhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PhotoViewHolder(private val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: Photo) {
            binding.tvSequence.text = String.format("%02d", photo.sequence)
            binding.tvInfo.text = "${photo.position} ${photo.material}"

            binding.imageView.load(File(photo.watermarkedPath)) {
                crossfade(true)
            }

            binding.root.setOnClickListener { onItemClick(photo) }
            binding.root.setOnLongClickListener {
                onDeleteClick(photo)
                true
            }
        }
    }

    class PhotoDiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Photo, newItem: Photo) = oldItem == newItem
    }
}
