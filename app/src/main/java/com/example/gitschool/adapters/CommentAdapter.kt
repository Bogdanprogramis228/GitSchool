package com.example.gitschool.adapters

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gitschool.DetailAnimeActivity
import com.example.gitschool.R
import com.example.gitschool.data.Comment
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(
    private val commentList: MutableList<Comment>,
    private val onLikeClick: (Comment, ViewHolder) -> Unit,
    private val onDislikeClick: (Comment, ViewHolder) -> Unit
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("HH:mm   dd.MM.yyyy", Locale.getDefault())
    private val rtdb = FirebaseDatabase.getInstance("https://gitschool-9eede-default-rtdb.europe-west1.firebasedatabase.app/").reference

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ShapeableImageView = itemView.findViewById(R.id.commentUserAvatar)
        val userName: TextView = itemView.findViewById(R.id.commentUserName)
        val commentText: TextView = itemView.findViewById(R.id.commentText)
        val timestamp: TextView = itemView.findViewById(R.id.commentTimestamp)
        val likeButton: ImageButton = itemView.findViewById(R.id.commentLikeButton)
        val likeCount: TextView = itemView.findViewById(R.id.commentLikeCount)
        val dislikeButton: ImageButton = itemView.findViewById(R.id.commentDislikeButton)
        val dislikeCount: TextView = itemView.findViewById(R.id.commentDislikeCount)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(comment: Comment) {
            // Коментар
            commentText.text = comment.text
            timestamp.text = comment.timestamp?.toDate()?.let { dateFormat.format(it) } ?: ""

            // Відображення поточних лічильників
            likeCount.text = comment.likeCount.toString()
            dislikeCount.text = comment.dislikeCount.toString()

            // Завантаження даних користувача з Firestore (ім'я)
            FirebaseFirestore.getInstance().collection("users").document(comment.userId ?: "")
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: "Анонім"
                        userName.text = name
                    } else {
                        userName.text = "Анонім"
                    }
                }
                .addOnFailureListener {
                    userName.text = "Анонім"
                }

            // Завантаження аватарки з Realtime Database
            val uid = comment.userId
            avatar.tag = uid

            if (uid.isNotEmpty()) {
                val userRef = rtdb.child("users").child(uid)

                userRef.child("avatarBase64").get().addOnSuccessListener { snapshot ->
                    if (avatar.tag != uid) return@addOnSuccessListener

                    val base64 = snapshot.getValue(String::class.java)
                    if (!base64.isNullOrEmpty()) {
                        try {
                            val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            Glide.with(avatar.context)
                                .load(bitmap)
                                .placeholder(R.drawable.ava_01)
                                .circleCrop()
                                .into(avatar)
                        } catch (e: Exception) {
                            avatar.setImageResource(R.drawable.ava_01)
                        }
                    } else {
                        // Якщо немає base64 — перевірити ресурс
                        userRef.child("avatarResourceId").get().addOnSuccessListener { resSnapshot ->
                            if (avatar.tag != uid) return@addOnSuccessListener

                            val resId = resSnapshot.getValue(Int::class.java)
                            if (resId != null && resId != 0) {
                                Glide.with(avatar.context)
                                    .load(resId)
                                    .placeholder(R.drawable.ava_01)
                                    .circleCrop()
                                    .into(avatar)
                            } else {
                                avatar.setImageResource(R.drawable.ava_01)
                            }
                        }.addOnFailureListener {
                            avatar.setImageResource(R.drawable.ava_01)
                        }
                    }
                }.addOnFailureListener {
                    avatar.setImageResource(R.drawable.ava_01)
                }
            } else {
                avatar.setImageResource(R.drawable.ava_01)
            }

            // Завантаження реакцій
            (itemView.context as? DetailAnimeActivity)?.loadCommentReaction(comment) { likes, dislikes, userReaction ->
                likeCount.text = likes.toString()
                dislikeCount.text = dislikes.toString()
                likeButton.isSelected = (userReaction == "like")
                dislikeButton.isSelected = (userReaction == "dislike")
            }

            // Обробники
            likeButton.setOnClickListener {
                onLikeClick(comment, this)
            }
            dislikeButton.setOnClickListener {
                onDislikeClick(comment, this)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(commentList[position])
    }

    override fun getItemCount(): Int = commentList.size

    fun addComment(comment: Comment) {
        commentList.add(0, comment)
        notifyItemInserted(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateComments(newComments: List<Comment>) {
        commentList.clear()
        commentList.addAll(newComments)
        notifyDataSetChanged()
    }
}