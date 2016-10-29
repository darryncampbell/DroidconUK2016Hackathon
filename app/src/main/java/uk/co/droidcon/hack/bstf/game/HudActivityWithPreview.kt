@file:Suppress("DEPRECATION")

package uk.co.droidcon.hack.bstf.game

import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import butterknife.bindView
import timber.log.Timber
import uk.co.droidcon.hack.bstf.R

class HudActivityWithPreview : HudActivity(), SurfaceHolder.Callback {

    lateinit internal var camera: Camera
    val cameraPreview: SurfaceView by bindView(R.id.camera_preview)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupCamera()
    }

    override fun setupScanController() {
        // Don't setup the scanController here
    }

    private fun setupCamera() {
        cameraPreview.visibility = View.VISIBLE
        cameraPreview.holder.addCallback(this)
        cameraPreview.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        camera = Camera.open()
        camera.setDisplayOrientation(90)
    }

    override fun onPause() {
        super.onPause()
        camera.stopPreview()
    }

    override fun onDestroy() {
        localBroadcastManager!!.unregisterReceiver(reloadReceiver)
        camera.release()
        super.onDestroy()
    }

    override fun setupShooting() {
        super.setupShooting()
        cameraPreview.setOnClickListener {
            if (!gunEmpty) {
                shoot()
            }
        }

        cameraPreview.setOnLongClickListener {
            switchWeapon()
            true
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val params = camera.parameters
        val sizes = params.supportedPreviewSizes
        val selected = sizes[0]
        params.setPreviewSize(selected.width, selected.height)
        camera.parameters = params

        camera.startPreview()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera.setPreviewDisplay(cameraPreview.holder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Timber.w("HUD surfaceDestroyed")
    }


}
