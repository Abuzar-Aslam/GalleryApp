package com.task.galleryApp.galleryview

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.task.galleryApp.R
import com.task.galleryApp.databinding.ActivityGalleryBinding
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File


/**
 * Created by Abuzar on 6/12/2020.
 */
class GalleryActivity : AppCompatActivity(), GalleryNavigator {


    private val galleryViewModel: GalleryViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityGalleryBinding>(this, R.layout.activity_gallery)
        binding.viewModel = galleryViewModel
        galleryViewModel.setNavigator(this)
    }

    private fun selectImage() {
        val options =
            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photo!")
        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            if (options[item] == "Take Photo") {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val f =
                    File(Environment.getExternalStorageDirectory(), "temp.jpg")
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
                startActivityForResult(intent, 1)
            }
            else if (options[item] == "Choose from Gallery") {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(intent, 2)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    override fun addImage() {
        Log.d("Abuzar", "In Add Image")
        selectImage()
    }
}
