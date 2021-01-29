package hypr.social.hangtimeassistant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        FirebaseApp.initializeApp(applicationContext)


        // add event handlers
        text_register_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        button_register_register.setOnClickListener {
            val email = text_register_email.text.toString().trim()
            val password = text_register_password.text.toString().trim()

            // try to register the user
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Registration not successful: " + it.message.toString(), Toast.LENGTH_LONG).show()
                }
        }
    }
}