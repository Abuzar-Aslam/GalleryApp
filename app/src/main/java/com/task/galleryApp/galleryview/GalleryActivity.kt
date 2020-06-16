package com.task.galleryApp.galleryview

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.task.galleryApp.R
import com.task.galleryApp.data.ImageModel
import com.task.galleryApp.databinding.ActivityGalleryBinding
import com.task.galleryApp.util.FetchPath
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.IOException


/**
 * Created by Abuzar on 6/12/2020.
 */
class GalleryActivity : AppCompatActivity(), GalleryNavigator {


    private val galleryViewModel: GalleryViewModel by viewModel()
    // Folder path for Firebase Storage.

    val Image_Request_Code = 10

    // Creating RecyclerView.
    lateinit var recyclerView: RecyclerView

    // Creating RecyclerView.Adapter.
    lateinit var adapter: GalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityGalleryBinding>(this, R.layout.activity_gallery)
        binding.viewModel = galleryViewModel
        galleryViewModel.setNavigator(this)

        recyclerView = findViewById(R.id.galleryRecyclerView);

        // Setting RecyclerView size true.
        recyclerView.setHasFixedSize(true);

        // Setting RecyclerView layout as LinearLayout.
        recyclerView.setLayoutManager(LinearLayoutManager(this));

        galleryViewModel.fetchImages()
    }


//    private fun selectImage() {
//        val options =
//            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Add Photo!")
//        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
//
//            if (options[item] == "Take Photo") {
//                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                val f =
//                    File(Environment.getExternalStorageDirectory(), "temp.jpg")
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
//                startActivityForResult(intent, 1)
//            } else if (options[item] == "Choose from Gallery") {
//                if (checkReadStoragePermission()) {
//                    if (checkWriteStoragePermission()) {
//                        launchGallery()
//                    }
//                }
//
//            } else if (options[item] == "Cancel") {
//                dialog.dismiss()
//            }
//        })
//        builder.show()
//    }

    @SuppressLint("IntentReset")
    override fun launchGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        intent.type = "image/*";
        startActivityForResult(intent, Image_Request_Code)
    }

    fun checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }
    }


    fun checkReadStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    2
                )
                return false
            } else return true
        }
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            galleryViewModel.FilePathUri = data.getData();

            Log.d("Abuzar", "onActivityResult URI " + galleryViewModel.FilePathUri.toString())
            try {
                galleryViewModel.uploadImageToFirebase()

            } catch (e: IOException) {

                e.printStackTrace();
            }
        }
    }

    override fun addImage() {
        Log.d("Abuzar", "In Add Image")

        galleryViewModel.selectImage(this)
    }

    override fun setImages(list: List<ImageModel>) {

        adapter = GalleryAdapter(getApplicationContext(), list);

        recyclerView.setAdapter(adapter);
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        when (requestCode) {
            1 -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    launchGallery()
                    // permission was granted, yay! Do the
                    // camera-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast.makeText(this, "Permission denied to read SMS", Toast.LENGTH_SHORT)
                        .show();
                    //finish();
                }
            }

            2 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    launchGallery()
                    // permission was granted, yay! Do the
                    // camera-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast.makeText(this, "Permission denied to read SMS", Toast.LENGTH_SHORT)
                        .show();
                    //finish();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
