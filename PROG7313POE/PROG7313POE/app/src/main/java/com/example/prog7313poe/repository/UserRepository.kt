package com.example.prog7313poe.repository

import com.example.prog7313poe.data.AppDatabase
import com.example.prog7313poe.model.User
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepository(
    private val database: AppDatabase
) {
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")

    fun startSync() {
        dbRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                CoroutineScope(Dispatchers.IO).launch {
                    for (data in snapshot.children) {
                        val user = data.getValue(User::class.java)

                        user?.let {
                            database.userDao().insert(it)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // handle error if needed
            }
        })
    }
}