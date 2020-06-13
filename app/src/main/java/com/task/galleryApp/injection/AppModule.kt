package com.task.galleryApp.injection

import com.task.galleryApp.galleryview.GalleryViewModel
import org.koin.android.viewmodel.experimental.builder.viewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

/**
 * Created by Abuzar on 6/13/2020.
 */

val viewModelModule = module {

    viewModel {
        GalleryViewModel()
    }

}