package vcmsa.projects.buggybank

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vcmsa.projects.buggybank.databinding.ActivitySignUpBinding

private const val TAG = "SignUpActivity"

class Sign_up : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signupPage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Go to sign in page when the user presses the back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressed: Going to sign in page")
                val intent = Intent(this@Sign_up, Sign_in::class.java)
                startActivity(intent)
                finish()
            }
        })

        binding.SignUpLogin.setOnClickListener {
            // Go to sign in page
            Log.d(TAG, "onClick: Going to sign in page")
            val intent = Intent(this@Sign_up, Sign_in::class.java)
            startActivity(intent)
            finish()
        }

        binding.SignUpButton.setOnClickListener {
            val email = binding.signUpEmail.text.toString()
            val password = binding.SignUpPassword.text.toString()
            val passwordConfirm = binding.SignUpPasswordConfirm.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && passwordConfirm.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        Log.d(TAG, "Creating user with email: $email and password: $password")
                        val result = auth.createUserWithEmailAndPassword(email, password).await()
                        val user = result.user
                        if (user != null) {
                            Log.d(TAG, "Sign up successful")
                            Toast.makeText(
                                this@Sign_up,
                                "Sign up successful",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } else {
                            Log.d(TAG, "Sign up failed")
                            Toast.makeText(
                                this@Sign_up,
                                "Sign up failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        Log.d(TAG, "Password is too weak")
                        Toast.makeText(
                            this@Sign_up,
                            "Password is too weak",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Log.d(TAG, "Email already in use")
                        Toast.makeText(
                            this@Sign_up,
                            "Email already in use",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Log.d(TAG, "Sign up failed: ${e.message}")
                        Toast.makeText(
                            this@Sign_up,
                            "Sign up failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}