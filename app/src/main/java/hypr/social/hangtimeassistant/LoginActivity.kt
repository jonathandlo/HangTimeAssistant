package hypr.social.hangtimeassistant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // add event handlers
        text_login_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        button_login_login.setOnClickListener {
            val email = text_login_email.text.toString().trim()
            val password = text_login_password.text.toString().trim()

            if (!validateLogin(email, password)) return@setOnClickListener

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(this) { task ->
                    proceed()
                }
                .addOnFailureListener {
                    Toast.makeText(baseContext, "Authentication failed: " + it.message, Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onStart() {
        super.onStart()

        // skip login if user is already signed in
        //if (FirebaseAuth.getInstance().currentUser != null) proceed()
    }

    private fun proceed(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun validateLogin(email: String, password: String) : Boolean {
        when {
            email.isEmpty() -> Toast.makeText(baseContext, "Please enter an email address", Toast.LENGTH_LONG).show()
            password.isEmpty() -> Toast.makeText(baseContext, "Please enter a password", Toast.LENGTH_LONG).show()
            else -> return true
        }
        return false
    }
}