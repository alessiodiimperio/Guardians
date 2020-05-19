package models

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService

const val DATASTORE: String = "DATASTORE"

object DataStore {

    //Firestore db ref
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val currentUser = User(null,null, null, null, mutableListOf(), mutableListOf())

    fun syncChangesToFirebase(){
        val db = FirebaseFirestore.getInstance()
        val userCollection = db.collection("users")
        val profilePath = currentUser.uid ?: return

        userCollection.document(profilePath).set(currentUser)
            .addOnSuccessListener {
                Log.d(DATASTORE,"Sync Success")
            }
            .addOnFailureListener { error ->
              Log.d(DATASTORE, "Sync error: ${error.message}")
            }
    }

    fun addGuardian(guardian:Guardian){
        currentUser.guardians.add(guardian)
        syncChangesToFirebase()
    }
    fun removeGuardian(position:Int){
        currentUser.guardians.removeAt(position)
        syncChangesToFirebase()
    }

    /*
    fun keepDataSynced() {
        Log.d(DATASTORE, "inside init")
        val userId = auth.uid
        if (userId != null) {
            val userRef = db.collection("user").document(userId)
            userRef.addSnapshotListener {
                    documentSnapshot,
                    firebaseFirestoreException ->

                Log.d(DATASTORE, "In snapshotlistener")
                if (documentSnapshot != null) {
                    val user = documentSnapshot.toObject(User::class.java)
                    currentUser.uid = user?.uid
                    currentUser.name = user?.name
                    currentUser.mobilNR = user?.mobilNR
                    currentUser.email = user?.email

                    if (user?.location != null) {
                        currentUser.location = user.location
                    }
                    if (user?.guardians != null) {
                        currentUser.guardians = user.guardians
                    }
                } else {
                    Log.d(DATASTORE, "$firebaseFirestoreException")
                }

            }
        }
    }

     */
}