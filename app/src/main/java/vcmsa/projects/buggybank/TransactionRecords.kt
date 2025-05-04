package vcmsa.projects.buggybank

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBindings
import androidx.viewbinding.ViewBindings.findChildViewById
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
import org.w3c.dom.Text
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [TransactionRecords.newInstance] factory method to
 * create an instance of this fragment.
 */

class TransactionRecords : Fragment() {
    
    private lateinit var rootNode: FirebaseDatabase
    private lateinit var userReference: DatabaseReference
    private lateinit var adapter: TransactionRecordsAdapter
    private lateinit var transactionsList: RecyclerView
    private lateinit var noTransactions: TextView
    private lateinit var sortCategory: TextView
    
    private val transactions = ArrayList<Transaction>()
    private val allTransactions = ArrayList<Transaction>()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.fragment_transaction_records, container, false)
        
        transactionsList = layout.findViewById(R.id.rvTransactions)
        noTransactions = layout.findViewById(R.id.tvNoTransactions)
        sortCategory = layout.findViewById(R.id.SortCategory)
        
        adapter = TransactionRecordsAdapter(transactions)
        transactionsList.adapter = adapter
        
        rootNode = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        userReference = rootNode.getReference("transactions").child(userId)
        
        fetchTransactionsFromFirebase()
        
        sortCategory.setOnClickListener {
            showCategoryPopup(it)
        }
        
        return layout
    }
    
    private fun fetchTransactionsFromFirebase() {
        lifecycleScope.launch {
            try {
                val snapshot = withContext(Dispatchers.IO) {
                    suspendCoroutine<DataSnapshot> { continuation ->
                        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                continuation.resume(dataSnapshot)
                            }
                            
                            override fun onCancelled(databaseError: DatabaseError) {
                                continuation.resumeWithException(databaseError.toException())
                            }
                        })
                    }
                }
                
                allTransactions.clear()
                for (snapshot1 in snapshot.children) {
                    val transaction = snapshot1.getValue(Transaction::class.java)
                    if (transaction != null) {
                        allTransactions.add(transaction)
                    }
                }
                
                updateDisplayedTransactions(allTransactions)
                
            } catch (e: Exception) {
                Log.e("TransactionRecords", "Failed to fetch transactions", e)
                context?.let {
                    Toast.makeText(it, "You have no transactions", Toast.LENGTH_SHORT).show()
                }
                noTransactions.visibility = View.VISIBLE
            }
        }
    }
    
    private fun updateDisplayedTransactions(filteredList: List<Transaction>) {
        transactions.clear()
        transactions.addAll(filteredList)
        adapter.notifyDataSetChanged()
        
        noTransactions.visibility = if (transactions.isEmpty()) View.VISIBLE else View.INVISIBLE
    }
    
    private fun showCategoryPopup(anchor: View) {
        val context = anchor.context
        val categories = allTransactions.map { it.category }.distinct()
        
        if (categories.isEmpty()) {
            Toast.makeText(context, "No categories to filter", Toast.LENGTH_SHORT).show()
            return
        }
        
        val popupMenu = PopupMenu(context, anchor)
        categories.forEachIndexed { index, category ->
            popupMenu.menu.add(Menu.NONE, index, Menu.NONE, category)
        }
        
        popupMenu.setOnMenuItemClickListener { item ->
            val selectedCategory = categories[item.itemId]
            val filtered = allTransactions.filter { it.category == selectedCategory }
            updateDisplayedTransactions(filtered)
            true
        }
        
        popupMenu.show()
    }
}