package com.task.galleryApp.injection

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.task.galleryApp.galleryview.GalleryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.experimental.builder.viewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

/**
 * Created by Abuzar on 6/13/2020.
 */

val viewModelModule = module {

    viewModel {
        GalleryViewModel(androidContext(),get(),get())
    }

    single {
        FirebaseStorage.getInstance().getReference()
    }

    single {
        FirebaseDatabase.getInstance().getReference("images_uploads")
    }

}