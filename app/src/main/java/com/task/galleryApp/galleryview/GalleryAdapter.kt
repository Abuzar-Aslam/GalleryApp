package com.task.galleryApp.galleryview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.task.galleryApp.R
import com.task.galleryApp.data.ImageModel

/**
 * Created by Abuzar on 6/12/2020.
 */

class GalleryAdapter(val context: Context, val imageModelList: List<ImageModel>) :
    RecyclerView.Adapter<GalleryAdapter.RecyclerViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {

        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageModelList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val imageModel = imageModelList[position]
        //Loading image from Glide library.
        Glide.with(context).load(imageModel.url).into(holder.image);
    }


    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var image: ImageView = itemView.findViewById(R.id.iv)


    }

}