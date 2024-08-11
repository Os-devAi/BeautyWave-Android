package com.nexusdev.beautywave.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nexusdev.beautywave.R
import com.nexusdev.beautywave.model.ImagesModel

class ImagesAdapter(private val imagesList: List<ImagesModel>) :
    RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {


    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.images_view, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int = imagesList.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageItem = imagesList[position]
        Glide.with(holder.itemView.context)
            .load(imageItem.imagesUrl)
            .placeholder(R.drawable.cosmetics_one)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.imageView)
    }
}