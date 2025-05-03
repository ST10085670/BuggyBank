package vcmsa.projects.buggybank

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import vcmsa.projects.buggybank.databinding.FragmentMainPageBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "MainPageFragment"

class MainPageFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentMainPageBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Log.d(TAG, "onCreate: PARAM1: $param1, PARAM2: $param2")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMainPageBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseDatabase.getInstance()
        val textViewUserName = _binding?.mainText
        
        user?.let {
            val userId = it.uid
            val userRef = db.getReference("users").child(userId).child("username")
            
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange: snapshot: $snapshot")
                    val userName = snapshot.getValue<String>()
                    textViewUserName?.text = (userName ?: "No name found").toString()
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: $error")
                    textViewUserName?.text = "Error fetching user data"
                }
            })
        }
        
        Log.d(TAG, "onViewCreated: user: $user")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }
    
}