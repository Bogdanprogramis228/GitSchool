package com.example.gitschool.adapters

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.gitschool.R

class TeacherDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_PHOTO_URL = "photo_url"
        private const val ARG_TEACHER_NAME = "teacher_name"

        fun newInstance(photoUrl: String, teacherName: String): TeacherDialogFragment {
            return TeacherDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PHOTO_URL, photoUrl)
                    putString(ARG_TEACHER_NAME, teacherName)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val photoUrl = arguments?.getString(ARG_PHOTO_URL)
        val teacherName = arguments?.getString(ARG_TEACHER_NAME)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_teacher, null)
        val imageView = view.findViewById<ImageView>(R.id.dialog_teacher_image)
        val nameView = view.findViewById<TextView>(R.id.dialog_teacher_name)
        nameView.text = teacherName

        Glide.with(requireContext())
            .load(photoUrl)
            .into(imageView)

        val dialog = Dialog(requireContext())
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }
}
