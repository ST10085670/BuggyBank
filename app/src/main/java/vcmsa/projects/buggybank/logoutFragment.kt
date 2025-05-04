package vcmsa.projects.buggybank

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class logoutFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_logout, container, false)

        val btnCancel: Button = view.findViewById(R.id.cancelBtn)
        btnCancel.setOnClickListener {
            val dbRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            dismiss()
        }

        val btnLogout: Button = view.findViewById(R.id.btnlogout)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val dbRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
            dbRef.child("signedIn").setValue(false)
            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, Sign_in::class.java)
            startActivity(intent)
            dismiss()
        }

        return view
    }
    
    
}