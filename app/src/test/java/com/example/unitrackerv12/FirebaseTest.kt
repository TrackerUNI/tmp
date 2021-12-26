package com.example.unitrackerv12

import org.junit.Test

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.example.unitrackerv12.UserManagerV
import com.example.unitrackerv12.Position
import com.example.unitrackerv12.UserData

import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass


val db: FirebaseFirestore = FirebaseFirestore.getInstance()
var auth: FirebaseAuth = FirebaseAuth.getInstance()

// created user for testing purpose (move this line to setUp method)

/**
 * Unittests for firebase interface
 */

class FirebaseTest {
    @Before
    fun login()
    {
        var email = "test@example.com"
        var password = "password"
        //auth.createUserWithEmailAndPassword(email, password)
        auth.signInWithEmailAndPassword(email, password)
    }

    @Test
    fun test_add_position() {
        var position = Position(71.2, -23.7)
        UserManagerV.addPosition(auth.currentUser, position)

        var last_position: Position? = null
        var userid: String = auth.currentUser!!.uid
        UserManagerV.collection.document(userid).get()
            .addOnSuccessListener { documentSnapshot ->
                var userData: UserData? = documentSnapshot.toObject(UserData::class.java)
                last_position = userData!!.lastPosition


                assertEquals(position.latitude, last_position!!.latitude)
                assertEquals(position.longitude, last_position!!.longitude)
            }
    }

    @Test
    fun test_create_group()
    {
        var name = "G00-test"
        var groupid: String = ""

        // init - GroupManager.create

        var userid: String? = auth.currentUser?.uid
        var admins: List<String> = listOf(userid!!)
        var users: List<String> = listOf()

        var group: GroupData? = GroupData(
            name = name,
            users = users,
            admins = admins
        )

        GroupManager.collection.add(group!!)
            .addOnSuccessListener { doc ->
                groupid = doc.id
                doc.update("groupid", groupid)
            }
        // END - GroupManager.create

        var data: GroupData? = null

        var doc = GroupManager.collection.document(groupid)
        doc.get()
            .addOnSuccessListener { documentSnapshot ->
                data = documentSnapshot.toObject(GroupData::class.java)

                assertEquals(data!!.groupid, groupid)
                assertEquals(data!!.name, name)
            }
    }
}