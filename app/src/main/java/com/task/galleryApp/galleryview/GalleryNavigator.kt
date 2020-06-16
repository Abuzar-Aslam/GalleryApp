package com.task.galleryApp.galleryview

import com.task.galleryApp.data.ImageModel

/**
 * Created by Abuzar on 6/13/2020.
 */

interface GalleryNavigator {

    fun addImage()

    fun setImages(list: List<ImageModel>)

    fun launchGallery()

}