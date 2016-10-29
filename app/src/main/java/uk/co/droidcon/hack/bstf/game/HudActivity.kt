@file:Suppress("DEPRECATION")

package uk.co.droidcon.hack.bstf.game

import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import uk.co.droidcon.hack.bstf.R

class HudActivity : AppCompatActivity(), SurfaceHolder.Callback {

    lateinit internal var mCamera: Camera
    lateinit internal var mPreview: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hud)

        mPreview = findViewById(R.id.preview) as SurfaceView
        mPreview.holder.addCallback(this)
        mPreview.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        mCamera = Camera.open()
        mCamera.setDisplayOrientation(90)
    }

    override fun onPause() {
        super.onPause()
        mCamera.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCamera.release()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val params = mCamera.getParameters()
        val sizes = params.getSupportedPreviewSizes()
        val selected = sizes.get(0)
        params.setPreviewSize(selected.width, selected.height)
        mCamera.setParameters(params)

        mCamera.startPreview()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            mCamera.setPreviewDisplay(mPreview.holder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.i("PREVIEW", "surfaceDestroyed")
    }
}
