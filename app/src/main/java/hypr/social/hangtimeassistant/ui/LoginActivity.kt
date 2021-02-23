package hypr.social.hangtimeassistant.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import hypr.social.hangtimeassistant.model.HTAFirestore
import hypr.social.hangtimeassistant.R
import hypr.social.hangtimeassistant.model.User
import hypr.social.hangtimeassistant.utils.Constants
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
            authenticate(email, password)
        }
    }

    override fun onStart() {
        super.onStart()

        // skip login if user is already signed in
        if (FirebaseAuth.getInstance().currentUser != null) loginUser()
    }

    private fun validateLogin(email: String, password: String) : Boolean {
        when {
            email.isEmpty() -> Toast.makeText(baseContext, "Please enter an email address", Toast.LENGTH_LONG).show()
            password.isEmpty() -> Toast.makeText(baseContext, "Please enter a password", Toast.LENGTH_LONG).show()
            else -> return true
        }
        return false
    }

    private fun authenticate(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(this) { task ->
                loginUser()
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Authentication failed: " + it.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun loginUser() {
        HTAFirestore.getCurrentUserDetails()
            .addOnSuccessListener {
                val userInfo = it.toObject<User>()!!

                // store the user details
                getSharedPreferences(Constants.PREF_PREFS, MODE_PRIVATE)
                    .edit()
                    .putString(Constants.PREF_USERNAME, "${userInfo.firstName} ${userInfo.lastName}")
                    .apply()

                // open the main activity
                startActivity(Intent(this, MainActivity::class.java)
                    .putExtra(Constants.PREF_USERDETAILS, userInfo))

                finish()
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Could not get user details: " + it.message, Toast.LENGTH_LONG).show()
            }
    }

}