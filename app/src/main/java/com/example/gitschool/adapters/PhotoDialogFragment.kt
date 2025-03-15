package com.example.gitschool.adapters

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.gitschool.R

class PhotoDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_PHOTO_URL = "photo_url"

        fun newInstance(photoUrl: String): PhotoDialogFragment {
            return PhotoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PHOTO_URL, photoUrl)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val photoUrl = arguments?.getString(ARG_PHOTO_URL)

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_photo, null)
        val imageView = view.findViewById<ImageView>(R.id.dialog_photo_image)

        Glide.with(requireContext())
            .load(photoUrl)
            .into(imageView)

        val dialog = Dialog(requireContext())
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true) // Закриття при натисканні поза рамками
        return dialog
    }
}
