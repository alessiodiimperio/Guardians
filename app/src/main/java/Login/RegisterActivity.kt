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
import com.google.firebase.iid.FirebaseInstanceId
import Managers.UserManager
import models.Guardian
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R
import kotlin.collections.HashMap

     const     val FIREBASE : String = "FIREBASE"
const val REGISTER_USER:String = "REGISTER_USER"
class RegisterActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var db:FirebaseFirestore
    lateinit var progressBar:ProgressBar
    lateinit var nameEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var numberEditText: EditText
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

        //Ui Components
        progressBar = findViewById(R.id.register_loading)
        nameEditText = findViewById(R.id.register_name)
        emailEditText = findViewById(R.id.register_email)
        passwordEditText = findViewById(R.id.register_password)
        numberEditText = findViewById(R.id.register_number)
        registerBttn = findViewById(R.id.registerActivity_register_button)

        registerBttn.setOnClickListener {
            signUp()
        }
    }

    private fun signUp() {

        progressBar.visibility = VISIBLE

        //Get strings from edittexts
        name = nameEditText.text.toString()
        email = emailEditText.text.toString()
        password = passwordEditText.text.toString()
        number = numberEditText.text.toString()

        //Check so they are not empty
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || number.isEmpty()) {
            Toast.makeText(this, "Fill in the required fields", Toast.LENGTH_SHORT).show()
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

        //Save user to users and store create a reference between email and uid
        val userId = auth.uid
        name = nameEditText.text.toString()
        email = emailEditText.text.toString().toLowerCase()
        number = numberEditText.text.toString()

        val userRef =  db.collection("/users/")
        val emailRef = db.collection("/emailToUid/")

        UserManager.currentUser.uid = userId
        UserManager.currentUser.name = name
        UserManager.currentUser.email = email
        UserManager.currentUser.number = number
        UserManager.currentUser.location = null
        UserManager.currentUser.guardians = mutableListOf<Guardian>()

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {task->
            if(task != null){
                UserManager.currentUser.token = task.token
                val emailUid = HashMap<String,String>()
                emailUid["email"] = email
                emailUid["uid"] = UserManager.currentUser.uid!!

                emailRef.document(email).set(emailUid).addOnSuccessListener {
                    Log.d(REGISTER_USER,"Email to Uid = $email to $userId")
                }
            }
        }

        userRef.document(UserManager.currentUser.uid!!).set(
            UserManager.currentUser).addOnSuccessListener {
            Log.d(FIREBASE, "CurrentUser object added to firestore")
            logInNewUser()

        }.addOnFailureListener {error->
            Log.d(FIREBASE, "Exception: $error ")
            progressBar.visibility = GONE
        }
    }
    private fun logInNewUser(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        progressBar.visibility = GONE
    }
}
