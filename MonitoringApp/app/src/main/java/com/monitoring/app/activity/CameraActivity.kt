package com.monitoring.app.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.monitoring.app.databinding.ActivityCameraBinding
import com.monitoring.app.utils.LogUtils
import com.monitoring.app.utils.ToastUtils

class CameraActivity : BaseActivity() {

    private lateinit var viewBinding: ActivityCameraBinding

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private var imageReader: ImageReader? = null

    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private lateinit var cameraId: String
    private lateinit var cameraCharacteristics: CameraCharacteristics

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 200
    }

    override fun initViewBinding() {
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    override fun registerOnClickListener() {
        viewBinding.buttonBack.setOnClickListener {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            setupCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera()
            } else {
                ToastUtils.showShort("permission rejected")
                finish()
            }
        }
    }

    private fun setupCamera() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    cameraCharacteristics = characteristics
                    break
                }
            }

            setupImageReader()

        } catch (e: CameraAccessException) {
            LogUtils.e("CameraActivity", "error when accessing camera: ${e.message}")
        }
    }

    private fun setupImageReader() {
        val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes = map?.getOutputSizes(ImageFormat.JPEG) ?: emptyArray()

        val size = sizes.maxByOrNull { it.width * it.height } ?: Size(1920, 1080)

        imageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 1)
        // imageReader?.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            LogUtils.e("CameraActivity", "stop background thread fail: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        if (viewBinding.textureView.isAvailable) {
            openCamera()
        } else {
            viewBinding.textureView.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private fun openCamera() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }

            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)

        } catch (e: CameraAccessException) {
            LogUtils.e("CameraActivity", "open camera failed: ${e.message}")
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
            LogUtils.e("CameraActivity", "camera error: $error")
        }
    }

    private fun createCameraPreview() {
        try {
            val texture = viewBinding.textureView.surfaceTexture
            texture?.setDefaultBufferSize(1920, 1080)

            val surface = Surface(texture)

            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            cameraDevice!!.createCaptureSession(
                listOf(surface, imageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraDevice ?: return

                        captureSession = session
                        updatePreview()
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        ToastUtils.showShort("camera configuration change failed")
                    }
                },
                null
            )

        } catch (e: CameraAccessException) {
            LogUtils.e("CameraActivity", "create camera preview failed: ${e.message}")
        }
    }

    private fun updatePreview() {
        cameraDevice ?: return

        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            captureSession?.setRepeatingRequest(
                captureRequestBuilder.build(),
                null,
                backgroundHandler
            )

        } catch (e: CameraAccessException) {
            LogUtils.e("CameraActivity", "update preview failed: ${e.message}")
        }
    }

    private fun capturePhoto() {
        cameraDevice ?: return

        try {
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(imageReader!!.surface)

            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    LogUtils.i("CameraActivity", "photo captured")
                }
            }

            captureSession?.capture(captureBuilder.build(), captureCallback, backgroundHandler)

        } catch (e: CameraAccessException) {
            LogUtils.e("CameraActivity", "photo capture failed: ${e.message}")
        }
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null

        cameraDevice?.close()
        cameraDevice = null

        imageReader?.close()
        imageReader = null
    }
}
