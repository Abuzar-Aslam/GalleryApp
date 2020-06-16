package com.task.galleryApp.galleryview

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.task.galleryApp.base.BaseViewModel
import com.task.galleryApp.data.ImageModel
import java.io.File
import java.io.FileInputStream

/**
 * Created by Abuzar on 6/12/2020.
 */

class GalleryViewModel(
    var context: Context,
    var storageReference: StorageReference,
    var databaseReference: DatabaseReference
) : ViewModel() {

    private var galleryNavigator: GalleryNavigator? = null
    var Storage_Path = "All_Image_Uploads/"

    var showLoading = ObservableBoolean(true)
    var FilePathUri: Uri? = null
    var list = ArrayList<ImageModel>()


    fun setNavigator(galleryNavigator: GalleryNavigator) {

        this.galleryNavigator = galleryNavigator
    }

    fun onAddImageClick(view: View) {
        galleryNavigator?.addImage()
    }


    fun uploadImageToFirebase() {
        if (FilePathUri != null) { // Setting progressDialog Title.
            showLoading.set(true)
            val storageReference2nd = storageReference!!.child(
                Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri!!)
            )
            val filePath = getPath(FilePathUri!!)
            if (!filePath.isNullOrEmpty()) {
                val file = File(filePath)
                val stream = FileInputStream(file)


                val onSuccessListener = OnSuccessListener<UploadTask.TaskSnapshot> {
                    val TempImageName = "tempImage"

                    val successListener =
                        OnSuccessListener<Uri> { uri ->
                            val imageUploadInfo = ImageModel(TempImageName, uri.toString())
                            // Getting image upload ID.
                            val ImageUploadId = databaseReference!!.push().key
                            // Adding image upload id s child element into databaseReference.
                            databaseReference!!.child(ImageUploadId!!).setValue(imageUploadInfo)
                            showLoading.set(false)
                        }
                    storageReference2nd.downloadUrl.addOnSuccessListener(successListener)
                }

                // Adding addOnSuccessListener to second StorageReference.
                storageReference2nd.putStream(stream)
                    .addOnSuccessListener(onSuccessListener)// If something goes wrong .
                    .addOnFailureListener { exception ->
                        showLoading.set(false)

                    }
            } else {
                showLoading.set(false)
            }

        } else {
            showLoading.set(false)
        }
    }


    fun GetFileExtension(uri: Uri): String? {

        var contentResolver: ContentResolver = context.contentResolver

        var mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))

    }

    fun getPath(uri: Uri): String? {
        // just some safety built in
        if (uri == null) {
            return null
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        var projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            var column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            var path = cursor.getString(column_index);
            cursor.close()
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    fun fetchImages() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list.clear()
                for (postSnapshot in dataSnapshot.children) {


                    var imageModel: ImageModel? =
                        postSnapshot.getValue(ImageModel::class.java) as ImageModel

                    list.add(imageModel!!)
                }

                showLoading.set(false)

                galleryNavigator?.setImages(list)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                // ...
            }
        }
        databaseReference.addValueEventListener(postListener)
    }


     fun selectImage(activity: Activity) {
        val options =
            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Add Photo!")
        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->

            if (options[item] == "Take Photo") {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val f =
                    File(Environment.getExternalStorageDirectory(), "temp.jpg")
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
                activity.startActivityForResult(intent, 1)
            } else if (options[item] == "Choose from Gallery") {
                if (checkWriteStoragePermission(activity)) {
                    galleryNavigator?.launchGallery()
                }

            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }


    fun checkWriteStoragePermission(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                return false
            } else return true
        }
        return true
    }


}