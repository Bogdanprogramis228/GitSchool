package com.example.gitschool.adapters

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gitschool.R
import com.example.gitschool.data.Comments
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter(
    private val commentList: MutableList<Comments>
) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("HH:mm   dd.MM.yyyy", Locale.getDefault())
    private val rtdb = FirebaseDatabase.getInstance(
        "https://gitschool-9eede-default-rtdb.europe-west1.firebasedatabase.app/"
    ).reference
    private val firestore = FirebaseFirestore.getInstance()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val animeTitle: TextView = itemView.findViewById(R.id.animeTitle)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteCommentButton)
        private val avatar: ShapeableImageView = itemView.findViewById(R.id.commentUserAvatar)
        private val userName: TextView = itemView.findViewById(R.id.commentUserName)
        private val commentText: TextView = itemView.findViewById(R.id.commentText)
        private val timestamp: TextView = itemView.findViewById(R.id.commentTimestamp)
        private val likeButton: ImageButton = itemView.findViewById(R.id.commentLikeButton)
        private val likeCount: TextView = itemView.findViewById(R.id.commentLikeCount)
        private val dislikeButton: ImageButton = itemView.findViewById(R.id.commentDislikeButton)
        private val dislikeCount: TextView = itemView.findViewById(R.id.commentDislikeCount)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(comment: Comments) {
            // Заголовок аніме
            animeTitle.text = comment.animeTitle ?: "Аніме"

            // Текст і час
            commentText.text = comment.text
            timestamp.text = comment.timestamp?.toDate()?.let { dateFormat.format(it) } ?: ""

            // Попередні лічильники
            likeCount.text = comment.likeCount.toString()
            dislikeCount.text = comment.dislikeCount.toString()
            likeButton.isClickable = false
            dislikeButton.isClickable = false

            // Завантаження імені
            firestore.collection("users").document(comment.userId)
                .get()
                .addOnSuccessListener { doc ->
                    userName.text = doc.getString("name") ?: "Анонім"
                }
                .addOnFailureListener {
                    userName.text = "Анонім"
                }

            // Завантаження аватарки
            val uid = comment.userId
            avatar.tag = uid
            if (uid.isNotEmpty()) {
                val userRef = rtdb.child("users").child(uid)
                userRef.child("avatarBase64").get()
                    .addOnSuccessListener { snap ->
                        if (avatar.tag != uid) return@addOnSuccessListener
                        val base64 = snap.getValue(String::class.java)
                        if (!base64.isNullOrEmpty()) {
                            try {
                                val bytes = Base64.decode(base64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                Glide.with(avatar.context)
                                    .load(bitmap)
                                    .placeholder(R.drawable.ava_01)
                                    .circleCrop()
                                    .into(avatar)
                            } catch (e: Exception) {
                                avatar.setImageResource(R.drawable.ava_01)
                            }
                        } else {
                            userRef.child("avatarResourceId").get()
                                .addOnSuccessListener { resSnap ->
                                    if (avatar.tag != uid) return@addOnSuccessListener
                                    val resId = resSnap.getValue(Int::class.java)
                                    if (resId != null && resId != 0) {
                                        Glide.with(avatar.context)
                                            .load(resId)
                                            .placeholder(R.drawable.ava_01)
                                            .circleCrop()
                                            .into(avatar)
                                    } else {
                                        avatar.setImageResource(R.drawable.ava_01)
                                    }
                                }
                                .addOnFailureListener {
                                    avatar.setImageResource(R.drawable.ava_01)
                                }
                        }
                    }
                    .addOnFailureListener {
                        avatar.setImageResource(R.drawable.ava_01)
                    }
            } else {
                avatar.setImageResource(R.drawable.ava_01)
            }

            // Оновлення лічильників лайків/дизлайків з Firestore
            comment.documentId?.let { docId ->
                firestore.collection("commentReactions").document(docId)
                    .get()
                    .addOnSuccessListener { snap ->
                        val likes = snap.getLong("likesCount") ?: 0L
                        val dislikes = snap.getLong("dislikesCount") ?: 0L
                        likeCount.text = likes.toString()
                        dislikeCount.text = dislikes.toString()
                    }
                    .addOnFailureListener {
                        // Якщо помилка — залишаємо попередні значення
                    }
            }

            // Видалення
            deleteButton.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Видалити коментар?")
                    .setMessage("Підтвердити видалення коментаря?")
                    .setPositiveButton("Так") { _, _ ->
                        comment.documentId?.let { docId ->
                            firestore.collection("comments").document(docId)
                                .delete()
                                .addOnSuccessListener {
                                    val pos = adapterPosition
                                    if (pos != RecyclerView.NO_POSITION) {
                                        commentList.removeAt(pos)
                                        notifyItemRemoved(pos)
                                    }
                                }
                        }
                    }
                    .setNegativeButton("Ні", null)
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comments, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(commentList[position])
    }

    override fun getItemCount(): Int = commentList.size

    fun updateComments(newComments: List<Comments>) {
        commentList.clear()
        commentList.addAll(newComments)
        notifyDataSetChanged()
    }
}
