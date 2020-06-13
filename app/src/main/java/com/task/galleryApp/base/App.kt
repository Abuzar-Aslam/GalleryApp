package com.task.galleryApp.base

import android.app.Application
import com.task.galleryApp.injection.viewModelModule
import org.koin.android.ext.android.startKoin

/**
 * Created by Abuzar on 6/13/2020.
 */
class App : Application() {


    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(viewModelModule))
    }

}