package com.tvsori.library.util


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import com.tvsori.library.R
import java.io.File

@SuppressWarnings("deprecation")
object Utils {
    fun getApplicationVersionCode(context: Context): Int {
        try {
            return context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: Exception) {
        }
        return 0
    }

    fun getApplicationVersionName(context: Context?): String {
        try {
            context?.let {
                return context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }
        } catch (e: Exception) {
        }
        return ""
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context?): String {
        try {
            return android.provider.Settings.Secure.getString(context!!.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {

        }
        return ""
    }

    fun parseDemention(context: Context?, value: Int): Int {
        context?.let {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics).toInt()
        }
        return 0
    }

    fun parseDemention(context: Context?, value: Float): Int {
        context?.let {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics).toInt()
        }
        return 0
    }

    fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    fun getUserImagePath(userid: String?): String {
        userid?.let { uid ->
            return "http://minihome.tvsori.com/user/" + uid + "/main_image/" + uid + "_Img1.jpg"
        }
        return ""
    }

    fun removeKeyboard(context: Context?, view: View) {
        try {
            val et = view as EditText
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(et.windowToken, 0)
        } catch (e: Exception) {
        }
    }

    fun decodeSampledBitmapFromResource(res: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(res.absolutePath, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(res.absolutePath, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            while (height / inSampleSize >= reqHeight && width / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun getMimeType(url: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    @Suppress("DEPRECATION")
    fun getCameraDisplayOrientation(window: WindowManager, cameraId: Int): Int {
        try {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(cameraId, info)
            val rotation = window.defaultDisplay.rotation
            var degrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }
            var result: Int
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360
            } else {
                result = (info.orientation - degrees + 360) % 360
            }
            return result
        } catch (e: Exception) {
        }
        return 0
    }

    fun isLandscape(context: Context?): Boolean {
        context?.let { c ->
            return c.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }
        return false
    }

    fun getBackFacingCameraId(cManager: CameraManager): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                for (cameraId in cManager.cameraIdList) {
                    val characteristics = cManager.getCameraCharacteristics(cameraId)
                    val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                    if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun getStatusBarHeight(context: Context?): Int {
        var status_bar = 0
        context?.let { c ->
            val resourceId = c.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                status_bar = c.resources.getDimensionPixelSize(resourceId)
            }
        }
        return status_bar
    }

    private val icon_img = intArrayOf(R.drawable.ic_level_01, R.drawable.ic_level_02, R.drawable.ic_level_03, R.drawable.ic_level_04, R.drawable.ic_level_05, R.drawable.ic_level_06, R.drawable.ic_level_07)
    private val icon_range = longArrayOf(0, 100000, 300000, 500000, 1000000, 3000000, 5000000)

    fun getUserPointImageResources(userPoint: Long): Int {
        for (i in icon_range.indices.reversed()) {
            if (userPoint >= icon_range[i]) {
                return icon_img[i]
            }
        }
        return icon_img[0]
    }
}