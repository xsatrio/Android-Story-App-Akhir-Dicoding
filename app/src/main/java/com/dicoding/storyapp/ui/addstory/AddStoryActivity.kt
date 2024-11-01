package com.dicoding.storyapp.ui.addstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.dicoding.storyapp.R
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.Results
import com.dicoding.storyapp.databinding.ActivityAddStoryBinding
import com.dicoding.storyapp.helper.reduceFileImage
import com.dicoding.storyapp.helper.uriToFile
import com.dicoding.storyapp.ui.addstory.CameraActivity.Companion.CAMERAX_RESULT
import com.dicoding.storyapp.ui.home.HomeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null
    private lateinit var materialSwitch: MaterialSwitch
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lat: Double? = null
    private var lon: Double? = null

    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, R.string.permission_request_granted, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.permission_request_denied, Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        materialSwitch = binding.locationSwitch

        materialSwitch.isChecked = false
        materialSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, R.string.location_on_switch_on, Toast.LENGTH_SHORT).show()
                getLocation()
            } else {
                Toast.makeText(this, R.string.location_on_switch_off, Toast.LENGTH_SHORT).show()
                lat = null
                lon = null
            }
        }

        viewModel.imageUri.observe(this) { uri ->
            if (uri != null) {
                currentImageUri = uri
                showImage()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.uploadResult.observe(this) { result ->
            when (result) {
                is Results.Loading -> showLoading(true)
                is Results.Success -> {
                    result.data.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    }
                    showLoading(false)
                    Toast.makeText(this, R.string.upload_success, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                is Results.Error -> {
                    Snackbar.make(binding.root, result.error, Snackbar.LENGTH_SHORT).show()
                    showLoading(true)
                }
            }
        }


        binding.galleryBtn.setOnClickListener { startGallery() }
        binding.cameraBtn.setOnClickListener { startCameraX() }
        binding.uploadBtn.setOnClickListener { uploadImage() }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                    Toast.makeText(this, R.string.location_succes, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.location_failed, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun uploadImage() {
        val description = binding.descriptionEdt.text.toString()
        if (description.isEmpty()) {
            Snackbar.make(binding.root, R.string.empty_description, Snackbar.LENGTH_SHORT).show()
            return
        }
        val descriptionReqBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        if (currentImageUri == null) {
            Snackbar.make(binding.root, R.string.empty_image, Snackbar.LENGTH_SHORT).show()
            return
        }

        val imageFile = uriToFile(currentImageUri!!, this)
        val compressedImage = imageFile.reduceFileImage()
        val photoRequestBody = compressedImage.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val photoMultipart =
            MultipartBody.Part.createFormData(
                "photo",
                compressedImage.name,
                photoRequestBody
            )

        val lat = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val lon = lon?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        viewModel.uploadStory(descriptionReqBody, photoMultipart, lat, lon)
    }

    private fun showImage() {
        viewModel.getImageUri()?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri)
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCameraX() {
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        } else {

            val intent = Intent(this, CameraActivity::class.java)
            launcherIntentCameraX.launch(intent)
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            val uri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            if (uri != null) {
                viewModel.setImageUri(uri)
            }
            currentImageUri = uri
            showImage()
        }
    }


    private fun showLoading(b: Boolean) {
        binding.progressIndicator.visibility = if (b) View.VISIBLE else View.GONE
        binding.uploadBtn.isEnabled = !b
        binding.cameraBtn.isEnabled = !b
        binding.galleryBtn.isEnabled = !b
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}