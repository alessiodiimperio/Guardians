package Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import models.DataStore
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R

const     val FIREBASE : String = "FIREBASE"

class RegisterActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var db:FirebaseFirestore

    lateinit var progressBar:ProgressBar
    lateinit var nameField: EditText
    lateinit var emailField: EditText
    lateinit var passwordField: EditText
    lateinit var numberField: EditText
    lateinit var registerBttn:Button
    lateinit var name:String
    lateinit var email:String
    lateinit var password:String
    lateinit var number:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        progressBar = findViewById(R.id.register_loading)
        nameField = findViewById(R.id.register_name)
        emailField = findViewById(R.id.register_email)
        passwordField = findViewById(R.id.register_password)
        numberField = findViewById(R.id.register_number)
        registerBttn = findViewById(R.id.registerActivity_register_button)

        registerBttn.setOnClickListener {
            signUp()
        }
    }

    private fun signUp() {

        progressBar.visibility = VISIBLE
        name = nameField.text.toString()
        email = emailField.text.toString()
        password = passwordField.text.toString()
        number = numberField.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || number.isEmpty()) {
            Toast.makeText(this, "Fill in all the required fields", Toast.LENGTH_SHORT).show()
            progressBar.visibility = GONE
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,
                        "User created",
                        Toast.LENGTH_SHORT).show()
                    saveUserToFirebase()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "User could not be created: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = GONE
            }

    }
    private fun saveUserToFirebase(){

        val userId = auth.uid
        name = nameField.text.toString()
        email = emailField.text.toString()
        number = numberField.text.toString()

        val userRef =  db.collection("/users/")
        val emailRef = db.collection("/emailToUid/")

        DataStore.currentUser.uid = userId
        DataStore.currentUser.name = name
        DataStore.currentUser.email = email
        DataStore.currentUser.mobilNR = number
        DataStore.currentUser.location = mutableListOf()
        DataStore.currentUser.guardians = mutableListOf()

        userRef.document("$userId").set(DataStore.currentUser).addOnSuccessListener {
            Log.d(FIREBASE, "CurrentUser object added to firestore")
            logInNewUser()
        }.addOnFailureListener {error->
            Log.d(FIREBASE, "Exception: $error ")
            progressBar.visibility = GONE
        }
        if(email != null || email.isNotEmpty()){
            emailRef.document("$email").set("$userId")
                .addOnSuccessListener {
                    Log.d(FIREBASE, "email to uid added to firestore")
            }
                .addOnFailureListener{
                    Log.d(FIREBASE, "email to uid failed")
                }
        }
    }
    private fun logInNewUser(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        progressBar.visibility = GONE
    }
}
