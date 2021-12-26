package com.example.unitrackerv12


import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.toObject
import com.google.type.DateTime
//import com.squareup.okhttp.internal.DiskLruCache

import java.time.LocalDateTime
import javax.security.auth.callback.Callback

val db: FirebaseFirestore = FirebaseFirestore.getInstance()
var auth: FirebaseAuth = FirebaseAuth.getInstance()

val TAG:String = "FIREBASEDEBUG"
// date: Dec 13 2021


/*
 * Position of a user at certain time
 */

data class Position(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val time: Timestamp = Timestamp.now()
)

data class UserData(
    val userid: String? = null,
    val username: String? = null,
    val lastPosition: Position? = null
)

data class GroupData(
    var groupid: String? = null,
    var name: String? = null,
    var admins: List<String>? = null,
    var users: List<String>? = null
)


class UserManagerV
{
    companion object{
        @JvmField
        val collection: CollectionReference = db.collection("users")


        @JvmStatic fun init(userid: String, email: String, username: String)
                /*
                 * Initialize a document of an user with ID: userid (Note: Use this function when the user has been created (sign up))
                 */
        {
            var document = UserManagerV.collection.document(userid)
            //var trackedGroups = listOf<String>()
            //var belongGroups = listOf<String>()
            //lastPosition = null
            //var positions = mapOf<String, Position>()

            var userData: UserData = UserData(userid = userid, username=username)

            document.set(userData)
                .addOnCompleteListener{
                    Log.d(TAG, "New user document was created! (${userData}")
                }
                .addOnFailureListener {  e -> Log.w(TAG, "Error while creating user document", e)}
        }

        @JvmStatic fun remove(user: FirebaseUser)
                /*
                 * Remove a user from users firebase collection
                 */
        {
            UserManagerV.collection.document(user.uid)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "User was successfully deleted") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        }

        @JvmStatic fun leftGroup(user: FirebaseUser, groupid: String)
        {
            var group: GroupData? = null /*GroupManager.get(groupid)*/
            var userid: String = user.uid

            if(GroupManager.isUser(groupid, userid))
            {
                GroupManager.collection.document(groupid).update("users", FieldValue.arrayRemove(userid))
            }
        }

        @JvmStatic fun positions(user: FirebaseUser): List<Position>
                /*
                 * Return all the positions of a user
                 */
        {
            var userPositions: MutableList<Position> = mutableListOf<Position>()

            UserManagerV.collection.document(user.uid).collection("positions")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.documents.forEach { doc ->
                        var position = doc.toObject(Position::class.java)
                        userPositions.add(position!!)
                    }
                }

            return userPositions
        }

        @JvmStatic fun addPosition(user: FirebaseUser?, position: Position)
                /*
                 * Add a position of a user
                 */
        {
            if(user != null)
            {
                UserManagerV.collection.document(user.uid).update("lastPosition", position)
                UserManagerV.collection.document(user.uid).collection("positions").add(position)
                    .addOnSuccessListener { Log.d(TAG, "New position successfully added!") }
                    .addOnFailureListener {  e -> Log.w(TAG, "Error adding position document", e)}
            }
        }

        @JvmStatic fun lastPosition(user: FirebaseUser?): Position?
                /*
                 * Return the last position of a user
                 */
        {
            var position: Position? = null
            var userid: String = user!!.uid
            UserManagerV.collection.document(userid).get()
                .addOnSuccessListener { documentSnapshot ->
                    var userData: UserData? = documentSnapshot.toObject(UserData::class.java)
                    Log.d(TAG, "User data: ${userData}")
                    position = userData!!.lastPosition
                    Log.d(TAG, "Last position: ${position}")
                }

            return position
        }

        @JvmStatic fun get(userid: String): UserData?
        {
            var userData: UserData? = null
            UserManagerV.collection.document(userid).get()
                .addOnSuccessListener{ documentSnapshot ->
                    userData = documentSnapshot.toObject(UserData::class.java)
                }

            return userData
        }
    }
}


class GroupManager
/*
 * Group to tracking several users
 */ {

    companion object{
        @JvmField
        val collection: CollectionReference = db.collection("groups")

        @JvmStatic fun create(name: String)
                /*
                 * Create a group (document) to DB
                 */
        {
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
                    var groupid: String = doc.id
                    Log.d(TAG, "Grupo ${groupid} agregado")
                }
        }

        @JvmStatic fun get(groupid: String): GroupData?
        {
            var data: GroupData? = null

            var doc = GroupManager.collection.document(groupid)
            doc.get()
                .addOnSuccessListener { documentSnapshot ->
                    data = documentSnapshot.toObject(GroupData::class.java)
                    Log.d(TAG, "Group ${groupid}: ${data}")
                }
            return data
        }

        @JvmStatic fun isAdmin(groupid: String, userid: String): Boolean
                /*
                 * Check if a user is an administrator of the group
                 */ {
            var belong: Boolean = false
            var data: GroupData? = null
            var doc = GroupManager.collection.document(groupid)

            doc.get()
                .addOnSuccessListener { documentSnapshot ->
                    data = documentSnapshot.toObject(GroupData::class.java)
                    Log.d(TAG, "Group ${groupid}: ${data}")
                    if (data != null) {
                        for (admin in data!!.admins!!) { // replace by: belong = (admins_id in ARRAY)
                            if (admin == userid) {
                                belong = true
                                break
                            }
                        }
                    }
                }
            return belong
        }

        @JvmStatic fun isUser(groupid: String, userid: String): Boolean
                /*
                 * Check if a user belong to the a group
                 */
        {
            var belong: Boolean = false
            var data: GroupData? = null /*GroupManager.get(groupid)*/
            var doc = GroupManager.collection.document(groupid)

            doc.get()
                .addOnSuccessListener { documentSnapshot ->
                    data = documentSnapshot.toObject(GroupData::class.java)
                    Log.d(TAG, "Group ${groupid}: ${data}")
                    if (data != null) {
                        for (user in data!!.users!!) { // replace by: belong = (user_id in ARRAY)
                            if (user == userid) {
                                belong = true
                                break
                            }
                        }
                    }
                }
            return belong
        }

        @JvmStatic fun users(groupid: String): List<String>
                /*
                 * Return the tracked users of a group
                 */
        {
            var groupData: GroupData? = null /*GroupManager.get(groupid)*/

            return groupData!!.users!!
        }

        @JvmStatic fun addAdmin(groupid: String, userid: String)
                /*
                 * Add an user to admins
                 */
        {
            GroupManager.collection.document(groupid).update("admins", FieldValue.arrayUnion(userid))
            /*
            var userid: String = auth.currentUser!!.uid
            if(GroupManager.isAdmin(groupid, userid)) {
                GroupManager.collection.document(groupid).update("admins", FieldValue.arrayUnion(userid))
                Log.d(TAG, "Usuario ${userid} agreado a admins de grupo ${groupid}")
            }
            else
            {
                Log.d(TAG, "Solo administradores pueden agregar otros adminis")
            }
             */
        }


        @JvmStatic fun addUser(groupid: String, userid: String)
                /*
                 * Add an user to users
                 */
        {
            GroupManager.collection.document(groupid).update("users", FieldValue.arrayUnion(userid))

            /*
            var userid: String = auth.currentUser!!.uid
            if(GroupManager.isAdmin(groupid, userid)) {
                GroupManager.collection.document(groupid).update("users", FieldValue.arrayUnion(userid))
                Log.d(TAG, "Usuario ${userid} agreado al grupo ${groupid}")
            }
            else
            {
                Log.d(TAG, "Solo administradores pueden agregar otros usuarios")
            }
             */
        }

        @JvmStatic fun trackedGroups(userid: String)
                /*
                 * Get all the tracked groups by user userid
                 * NOTE (IMPORTANT): Copy this function in your code
                 */
        {
            var tracked_groups: MutableList<GroupData> = mutableListOf()

            Log.d(TAG, "Tracked groups by ${userid}:")

            GroupManager.collection.get()
                .addOnSuccessListener { snapshot ->
                    snapshot.documents.forEach { doc ->
                        val data: GroupData? = doc.toObject(GroupData::class.java)
                        var isadmin: Boolean = false
                        for (admin in data!!.admins!!) { // replace by: belong = (admins_id in ARRAY)
                            if (admin == userid) {
                                isadmin = true
                                break
                            }
                        }

                        if(isadmin){
                            tracked_groups.add(data)
                            Log.d(TAG, "Tracked group: ${data.groupid}")
                        }
                    }
                    // At this point you have all the groups in which user 'userid' is an admin user
                }
        }

        @JvmStatic fun remove(groupid: String)
                /*
                 * Remove a group (document) from groups firebase collection
                 */
        {
            GroupManager.collection.document(groupid)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "Group was successfully deleted") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        }

        @JvmStatic fun lastPositions(groupid: String): MutableMap<String?, Position?> // {username: position, ...}
                /*
                 * Return the last position of all the users of this group
                 */
        {
            var groupData: GroupData? = null /*GroupManager.get(groupid)*/
            var lastPositions: MutableMap<String?, Position?> = mutableMapOf()

            var doc = GroupManager.collection.document(groupid)
            doc.get()
                .addOnSuccessListener { documentSnapshot ->
                    groupData = documentSnapshot.toObject(GroupData::class.java)
                    Log.d(TAG, "Positions group: ${groupid}")
                    groupData!!.users?.forEach { userid ->
                        UserManagerV.collection.document(userid)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                var userData = documentSnapshot.toObject(UserData::class.java)
                                var position: Position? = userData?.lastPosition
                                lastPositions[userData!!.username] = position
                                Log.d(TAG, "Position user ${userData.userid} (${userData.username}): (${position!!.latitude}, ${position!!.longitude})")
                            }
                        //var position: Position = UserManagerV.lastPosition(userid)!!
                        //lastPositions[userid] = position
                    }
                }

            return lastPositions
        }
    }
}
