package vcmsa.projects.buggybank

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.transactionrecords.TransactionRecordsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class TransactionRecords : Fragment() {
    private lateinit var rootNode: FirebaseDatabase
    private lateinit var userReference: DatabaseReference
    private lateinit var adapter: TransactionRecordsAdapter
    private lateinit var transactionsList: RecyclerView
    private val transactions = ArrayList<Transaction>()
    private lateinit var noTransactions :TextView
    private val TAG = "TransactionRecords"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val layout = inflater.inflate(R.layout.fragment_transaction_records, container, false)

        transactionsList = layout.findViewById(R.id.rvTransactions)
        noTransactions = layout.findViewById<TextView>(R.id.tvNoTransactions)
        transactionsList.layoutManager = LinearLayoutManager(requireContext())
        adapter = TransactionRecordsAdapter(transactions)
        transactionsList.adapter = adapter

        // Initialize Firebase
        rootNode = FirebaseDatabase.getInstance()

        // Use FirebaseAuth to get the current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid



        Log.e(TAG, userId.toString())

        if (userId == null) {
            Log.e(TAG, "User not logged in")
            noTransactions.visibility = View.VISIBLE
            return layout
        }

        Log.e(TAG, "User: ${FirebaseAuth.getInstance().currentUser}")
        userReference = rootNode.getReference("users").child(userId).child("transactions")
        Log.e(TAG, "$userReference")

        fetchTransactionsFromFirebase()
        return layout
    }

    private fun fetchTransactionsFromFirebase() {

        lifecycleScope.launch {
            try {

                val snapshot = withContext(Dispatchers.IO) {
                    val dataSnapshot = suspendCoroutine<DataSnapshot> { continuation ->
                        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                Log.e(TAG, "$dataSnapshot ")
                                continuation.resume(dataSnapshot)
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                continuation.resumeWithException(databaseError.toException())
                            }
                        })
                    }
                    dataSnapshot
                }

                // Update the UI on the main thread
                transactions.clear()
                for (snapshot1 in snapshot.children) {
                    Log.e(TAG, "$snapshot1")
                    val transaction = snapshot1.getValue(Transaction::class.java)
                    if (transaction != null) {
                        transactions.add(transaction)
                    }
                }
                adapter.notifyDataSetChanged()

                if (transactions.isEmpty()) {
                    noTransactions.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                // Handle errors, e.g. network failures
//                context?.let {
//                    Toast.makeText(
//                        it,
//                        "You have no transactions",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }

                Log.e(TAG, "Failed to Fetch Transactions ", e )
                noTransactions.visibility = View.VISIBLE;

            }
        }
    }
}
