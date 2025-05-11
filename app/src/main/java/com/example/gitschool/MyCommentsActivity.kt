package com.example.gitschool

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gitschool.adapters.CommentsAdapter
import com.example.gitschool.data.Comments
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyCommentsActivity : AppCompatActivity() {
    private lateinit var adapter: CommentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.commentsRecyclerView)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = CommentsAdapter(mutableListOf())
        rv.adapter = adapter

        loadMyComments()
    }

    private fun loadMyComments() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("comments")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snaps ->
                val list = snaps.map { doc ->
                    val c = doc.toObject(Comments::class.java)
                    c.documentId = doc.id
                    c.animeTitle = doc.getString("animeTitle")
                    c
                }
                adapter.updateComments(list)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Не вдалося завантажити: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
