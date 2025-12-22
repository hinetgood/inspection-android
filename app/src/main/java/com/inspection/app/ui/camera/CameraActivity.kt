package com.inspection.app.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.inspection.app.databinding.ActivityCameraBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService

    private var addressId: Long = -1
    private var addressName: String = ""

    private var currentZoom = 1.0f
    private var minZoom = 0.5f // 目標支援 0.5X
    private var maxZoom = 5.0f

    companion object {
        const val EXTRA_ADDRESS_ID = "address_id"
        const val EXTRA_ADDRESS_NAME = "address_name"
        const val EXTRA_PHOTO_PATH = "photo_path"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val TAG = "CameraActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addressId = intent.getLongExtra(EXTRA_ADDRESS_ID, -1)
        addressName = intent.getStringExtra(EXTRA_ADDRESS_NAME) ?: ""

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        setupUI()
    }

    private fun setupUI() {
        // 拍照按鈕
        binding.btnCapture.setOnClickListener {
            takePhoto()
        }

        // 返回按鈕
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 0.5X 按鈕
        binding.btnZoom05.setOnClickListener {
            setZoom(0.5f)
            updateZoomButtons()
        }

        // 1X 按鈕
        binding.btnZoom1.setOnClickListener {
            setZoom(1.0f)
            updateZoomButtons()
        }

        // 2X 按鈕
        binding.btnZoom2.setOnClickListener {
            setZoom(2.0f)
            updateZoomButtons()
        }

        // 縮放滑桿
        binding.zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val zoom = minZoom + (maxZoom - minZoom) * (progress / 100f)
                    setZoom(zoom)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        updateZoomButtons()
    }

    private fun updateZoomButtons() {
        binding.btnZoom05.alpha = if (currentZoom <= 0.6f) 1.0f else 0.5f
        binding.btnZoom1.alpha = if (currentZoom in 0.9f..1.1f) 1.0f else 0.5f
        binding.btnZoom2.alpha = if (currentZoom >= 1.9f) 1.0f else 0.5f

        binding.tvZoomLevel.text = String.format("%.1fX", currentZoom)
    }

    private fun setZoom(zoom: Float) {
        camera?.let { cam ->
            val zoomState = cam.cameraInfo.zoomState.value
            if (zoomState != null) {
                val clampedZoom = zoom.coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)
                cam.cameraControl.setZoomRatio(clampedZoom)
                currentZoom = clampedZoom

                // 更新 SeekBar
                val progress = ((clampedZoom - minZoom) / (maxZoom - minZoom) * 100).toInt()
                binding.zoomSeekBar.progress = progress.coerceIn(0, 100)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            // 選擇後置廣角鏡頭
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

                // 取得實際的縮放範圍
                camera?.cameraInfo?.zoomState?.observe(this) { zoomState ->
                    minZoom = zoomState.minZoomRatio
                    maxZoom = zoomState.maxZoomRatio
                    Log.d(TAG, "Zoom range: $minZoom - $maxZoom")

                    // 如果支援 0.5X，預設使用 0.5X
                    if (minZoom <= 0.6f) {
                        setZoom(0.5f)
                        binding.btnZoom05.visibility = View.VISIBLE
                    } else {
                        binding.btnZoom05.visibility = View.GONE
                        setZoom(1.0f)
                    }
                    updateZoomButtons()
                }

            } catch (exc: Exception) {
                Log.e(TAG, "Camera binding failed", exc)
                Toast.makeText(this, "相機啟動失敗", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        binding.btnCapture.isEnabled = false

        // 建立輸出檔案
        val photoDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "inspection")
        if (!photoDir.exists()) photoDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(photoDir, "IMG_${timestamp}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // 加上浮水印
                    val watermarkedFile = addWatermark(photoFile)

                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_PHOTO_PATH, watermarkedFile.absolutePath)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(this@CameraActivity, "拍照失敗", Toast.LENGTH_SHORT).show()
                    binding.btnCapture.isEnabled = true
                }
            }
        )
    }

    private fun addWatermark(photoFile: File): File {
        val originalBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val width = mutableBitmap.width
        val height = mutableBitmap.height

        // 計算字體大小 (基於圖片寬度)
        val fontSize = (width * 0.04f).coerceIn(40f, 120f)

        // 日期浮水印 (右上角)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val dateText = dateFormat.format(Date())

        val datePaint = Paint().apply {
            color = Color.WHITE
            textSize = fontSize
            isAntiAlias = true
            setShadowLayer(4f, 2f, 2f, Color.BLACK)
        }

        val dateWidth = datePaint.measureText(dateText)
        canvas.drawText(dateText, width - dateWidth - 30f, fontSize + 20f, datePaint)

        // 地址浮水印 (底部)
        val addressPaint = Paint().apply {
            color = Color.WHITE
            textSize = fontSize * 0.9f
            isAntiAlias = true
        }

        // 底部黑色半透明背景
        val bgPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0)
        }
        val bgHeight = fontSize * 1.8f
        canvas.drawRect(0f, height - bgHeight, width.toFloat(), height.toFloat(), bgPaint)

        // 地址文字
        val addressY = height - (bgHeight - fontSize) / 2 - 10f
        canvas.drawText(addressName, 30f, addressY, addressPaint)

        // 儲存
        val watermarkedFile = File(photoFile.parent, "WM_${photoFile.name}")
        FileOutputStream(watermarkedFile).use { out ->
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        originalBitmap.recycle()
        mutableBitmap.recycle()

        return watermarkedFile
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "需要相機權限才能拍照", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
