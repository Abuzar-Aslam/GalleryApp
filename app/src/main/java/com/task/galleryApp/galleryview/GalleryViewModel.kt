package com.task.galleryApp.galleryview

import android.view.View
import androidx.lifecycle.ViewModel
import com.task.galleryApp.base.BaseViewModel

/**
 * Created by Abuzar on 6/12/2020.
 */

class GalleryViewModel : ViewModel() {

    private var galleryNavigator: GalleryNavigator? = null

    var showLoading = true

    fun setNavigator(galleryNavigator: GalleryNavigator) {

        this.galleryNavigator = galleryNavigator
    }

    fun onAddImageClick(view: View) {
        galleryNavigator?.addImage()
    }


}