package com.task.galleryApp.util

import android.view.View
import androidx.databinding.BindingAdapter

/**
 * Created by Abuzar on 6/16/2020.
 */

@BindingAdapter("visible")
fun setVisible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}