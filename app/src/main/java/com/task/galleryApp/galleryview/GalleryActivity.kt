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
    var Storage_Path = "All_Image_Uploads/"

    // Root Database Name for Firebase Database.
    var Database_Path = "images_uploads"
    // Creating StorageReference and DatabaseReference object.
    var storageReference: StorageReference? = null
    var databaseReference: DatabaseReference? = null
    val Image_Request_Code = 10
    var FilePathUri: Uri? = null
    var progressDialog: ProgressDialog? = null


    // Creating RecyclerView.
    lateinit var recyclerView: RecyclerView

    // Creating RecyclerView.Adapter.
    lateinit var adapter: GalleryAdapter


    // Creating List of ImageUploadInfo class.
    var list = ArrayList<ImageModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityGalleryBinding>(this, R.layout.activity_gallery)
        binding.viewModel = galleryViewModel
        galleryViewModel.setNavigator(this)

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);


        recyclerView = findViewById(R.id.galleryRecyclerView);

        // Setting RecyclerView size true.
        recyclerView.setHasFixedSize(true);

        // Setting RecyclerView layout as LinearLayout.
        recyclerView.setLayoutManager(LinearLayoutManager(this));

        fetchImages()
    }

    private fun fetchImages() {

        progressDialog = ProgressDialog(this);

        // Setting up message in Progress dialog.
        progressDialog?.setMessage("Loading Images From Firebase.");

        // Showing progress dialog.
        progressDialog?.show();

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {


                    var imageModel: ImageModel? =
                        postSnapshot.getValue(ImageModel::class.java) as ImageModel

                    list.add(imageModel!!)
                }

                adapter = GalleryAdapter(getApplicationContext(), list);

                recyclerView.setAdapter(adapter);

                // Hiding the progress dialog.
                progressDialog?.dismiss();
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                // ...
            }
        }
        databaseReference?.addValueEventListener(postListener)
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
            } else if (options[item] == "Choose from Gallery") {
                if (checkReadStoragePermission()) {
                    if (checkWriteStoragePermission()) {

                        launchGallery()
                    }
                }

            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    @SuppressLint("IntentReset")
    fun launchGallery() {
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

    fun checkWriteStoragePermission(): Boolean {
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
                return false
            } else return true
        }
        return true
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

            FilePathUri = data.getData();

            Log.d("Abuzar", "onActivityResult URI " + FilePathUri.toString())
            try {
                UploadImageFileToFirebaseStorage();
                // Gvar bitmap: Bitmap =
                ////                    MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri);
                ////
                ////                // Setting up bitmap selected image into ImageView.
                ////                Seleetting selected image into Bitmap.
//                ctImage.setImageBitmap(bitmap);

                // After selecting image change choose button above text.
                //ChooseButton.setText("Image Selected");

            } catch (e: IOException) {

                e.printStackTrace();
            }
        }
    }

    //    // Creating Method to get the selected image file Extension from File Path URI.
    fun GetFileExtension(uri: Uri): String? {

        var contentResolver: ContentResolver = getContentResolver();

        var mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))

    }

    override fun addImage() {
        Log.d("Abuzar", "In Add Image")

        selectImage()
    }

    fun UploadImageFileToFirebaseStorage() { // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) { // Setting progressDialog Title.
            progressDialog = ProgressDialog(this);
            progressDialog?.setTitle("Image is Uploading...")
            // Showing progressDialog.
            progressDialog?.show()
            // Creating second StorageReference.
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
                            progressDialog?.dismiss()
                            // Showing toast message after done uploading.
                            Toast.makeText(
                                applicationContext,
                                "Image Uploaded Successfully ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    storageReference2nd.downloadUrl.addOnSuccessListener(successListener)
                }

                // Adding addOnSuccessListener to second StorageReference.
                storageReference2nd.putStream(stream)
                    .addOnSuccessListener(onSuccessListener)// If something goes wrong .
                    .addOnFailureListener { exception ->
                        // Hiding the progressDialog.
                        progressDialog?.dismiss()
                        // Showing exception erro message.
                        Toast.makeText(this, exception.message, Toast.LENGTH_LONG)
                            .show()
                    } // On progress change upload time.
                    .addOnProgressListener { progressDialog?.setTitle("Image is Uploading...") }
            } else {
                progressDialog?.dismiss()
                Toast.makeText(
                    this,
                    "Please Select Image or Add Image Name",
                    Toast.LENGTH_LONG
                ).show()
            }

        } else {
            Toast.makeText(
                this,
                "Please Select Image or Add Image Name",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    fun getPath(uri: Uri): String? {
        // just some safety built in
        if (uri == null) {
            Log.d("Abuzar", "URI is null")
            return null
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        var projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            var column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            var path = cursor.getString(column_index);
            cursor.close();
            Log.d("Abuzar", "Path is .. " + path)
            return path;
        }
        Log.d("Abuzar", "Path is .. " + uri.getPath())
        // this is our fallback here
        return uri.getPath();
    }


    fun getSourceStream(): FileInputStream {
        var out: FileInputStream? = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            var parcelFileDescriptor: ParcelFileDescriptor? =
                getContentResolver().openFileDescriptor(FilePathUri!!, "r");
            var fileDescriptor: FileDescriptor? = parcelFileDescriptor?.getFileDescriptor();
            out = FileInputStream(fileDescriptor)
        } else {
            out = getContentResolver()?.openInputStream(FilePathUri!!) as FileInputStream
        }
        return out;
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

                    checkWriteStoragePermission()
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
