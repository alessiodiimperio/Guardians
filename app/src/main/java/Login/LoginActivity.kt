package Login

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R
import se.diimperio.guardians.hideKeyboard


class LoginActivity : AppCompatActivity() {

    lateinit var auth:FirebaseAuth
    lateinit var progressBar:ProgressBar
    lateinit var usernameEditText:EditText
    lateinit var passwordEditText:EditText
    lateinit var loginBttn:Button
    lateinit var registerBttn:Button
    lateinit var forgotPwTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        progressBar = findViewById(R.id.login_loading)
        usernameEditText = findViewById(R.id.login_username)
        passwordEditText = findViewById(R.id.login_password)

        loginBttn = findViewById(R.id.login_button)
        registerBttn = findViewById(R.id.register_button)
        forgotPwTextView = findViewById(R.id.forgot_password_textview)

        if (auth.uid != null){
            goToMainActivity()
        }

        loginBttn.setOnClickListener {
            hideKeyboard()
            progressBar.visibility = VISIBLE
            signIn()

        }
        registerBttn.setOnClickListener {
            hideKeyboard()
            goToRegisterActivity()
        }

        forgotPwTextView.setOnClickListener {
            val email = usernameEditText.text.toString()

            if(email != null && email != ""){
                auth.sendPasswordResetEmail(email).addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent to $email",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {error->
                    Toast.makeText(this, "$error", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Insert valid email in the email field.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn(){
        val email = usernameEditText.text.toString().toLowerCase()
        val password = passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()){
           Toast.makeText(this,"Email and password required", Toast.LENGTH_LONG).show()
            hideKeyboard()
            progressBar.visibility = GONE
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    progressBar.visibility = GONE
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { error ->
                hideKeyboard()
                progressBar.visibility = GONE
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun goToRegisterActivity(){
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
    fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
