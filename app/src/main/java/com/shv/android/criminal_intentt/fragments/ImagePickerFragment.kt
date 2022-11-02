package com.shv.android.criminal_intentt.fragments

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources.Theme
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.shv.android.criminal_intentt.R
import com.shv.android.criminal_intentt.getScaleBitmap
import com.shv.android.criminal_intentt.getScaledBitmap
import java.io.File
import java.util.*

private const val TAG = "ImagePickerFragment"

class ImagePickerFragment : DialogFragment(), View.OnClickListener {
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var viewTreeObserver: ViewTreeObserver
    private var viewWidth = 0
    private var viewHeight = 0

    override fun onStart() {
        super.onStart()
        photoView.viewTreeObserver.apply {
            if (isAlive) {
                addOnGlobalLayoutListener {
                    updatePhotoView()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.image_view_dialog, null)
        photoView = view.findViewById(R.id.crime_photo_view_dialog) as ImageView

        viewTreeObserver = photoView.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener {
            viewWidth = photoView.width
            viewHeight = photoView.height
        }
        Log.i(TAG, "viewWidth: $viewWidth\nviewHeight: $viewHeight")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoFile = if (Build.VERSION.SDK_INT >= 33)
            arguments?.getSerializable(RESULT_IMAGE, File::class.java) as File
        else {
            @Suppress("DEPRECATED")
            arguments?.getSerializable(RESULT_IMAGE) as File
        }
        Log.i(TAG, "photoFile: ${photoFile.path}")

        updatePhotoView()

        photoView.run {
            setOnClickListener(this@ImagePickerFragment)
        }
    }

    private fun updatePhotoView() {
        val bitMap = getScaledBitmap(photoFile.path, photoView.width, photoView.height)
        photoView.rotation = 90F
        photoView.setImageBitmap(bitMap)
    }

    companion object {
        fun newInstance(file: File): ImagePickerFragment {
            val args = Bundle().apply {
                putSerializable(RESULT_IMAGE, file)
            }
            return ImagePickerFragment().apply {
                arguments = args
            }
        }
    }

    override fun onClick(p0: View?) {
        Log.i(TAG, "Clicked")
        onStop()
    }
}