package com.ahmedalkhashab.daggerexample.ucropexample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { pickFromGallery(); }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_MODE -> {
                    val selectedUri = data?.data
                    if (selectedUri != null) startCrop(selectedUri)
                    else Toast.makeText(this@MainActivity, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show()
                }
                UCrop.REQUEST_CROP -> data?.let { handleCropResult(it) }
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) data?.let { handleCropError(it) }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_STORAGE_READ_ACCESS_PERMISSION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromGallery()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        const val REQUEST_MODE = 1
        private const val SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage"
        const val COMPRESS_QUALITY = 90
    }

    private fun pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    BaseActivity.REQUEST_STORAGE_READ_ACCESS_PERMISSION)
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val mimeTypes = arrayOf("image/jpeg", "image/png")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_MODE)
        }
    }

    private fun startCrop(uri: Uri) {
        var destinationFileName = SAMPLE_CROPPED_IMAGE_NAME
        destinationFileName += ".png"
        var uCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, destinationFileName)))
        uCrop = basisConfig(uCrop)
        uCrop = advancedConfig(uCrop)
        // else start uCrop Activity
        uCrop.start(this@MainActivity)
    }

    /**
     * In most cases you need only to set crop aspect ration and max size for resulting image.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private fun basisConfig(uCrop: UCrop): UCrop {
        return uCrop.useSourceImageAspectRatio()
    }

    /**
     * Sometimes you want to adjust more options, it's done via [com.yalantis.ucrop.UCrop.Options] class.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private fun advancedConfig(uCrop: UCrop): UCrop {
        val options = UCrop.Options()
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
        options.setCompressionQuality(COMPRESS_QUALITY)
        options.setHideBottomControls(false)
        options.setFreeStyleCropEnabled(false)
        return uCrop.withOptions(options)
    }

    private fun handleCropResult(result: Intent) {
        val resultUri = UCrop.getOutput(result)
        if (resultUri != null) ResultActivity.startWithUri(this@MainActivity, resultUri)
        else Toast.makeText(this@MainActivity, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show()
    }

    private fun handleCropError(result: Intent) {
        val cropError = UCrop.getError(result)
        if (cropError != null) Toast.makeText(this@MainActivity, cropError.message, Toast.LENGTH_LONG).show()
        else Toast.makeText(this@MainActivity, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show()
    }

}