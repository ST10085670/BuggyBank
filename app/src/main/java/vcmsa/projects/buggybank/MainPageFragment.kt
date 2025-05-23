package vcmsa.projects.buggybank

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.fragment.app.Fragment

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import vcmsa.projects.buggybank.databinding.FragmentMainPageBinding

// Constants for fragment arguments
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "MainPageFragment"

class MainPageFragment : Fragment() {
    // Fragment arguments
    private var param1: String? = null
    private var param2: String? = null
    // Binding for the fragment layout
    private var _binding: FragmentMainPageBinding? = null
    // Convenience property to access the binding
    private val binding get() = _binding!!
    //chart variable
    private lateinit var chart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the fragment arguments
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
        // Inflate the fragment layout

        _binding = FragmentMainPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        // Get the current user and the database reference
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseDatabase.getInstance()
        // Get the text view for the username
        val textViewUserName = _binding?.mainText

        val vAmount = _binding?.walletAmount

        // Initialize chart
        chart = binding.statusBar

        // Load the chart data from Firebase
        loadChartData()

        // If the user is logged in, get the username from the database
        user?.let {
            val userId = it.uid
            val userRef = db.getReference("users").child(userId).child("details").child("username")

            // Add a listener to get the username from the database
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange: snapshot: $snapshot")
                    // Get the username from the snapshot
                    val userName = snapshot.getValue<String>()
                    // Set the username text view
                    textViewUserName?.text = "Welcome " + (userName ?: "No name found").toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: $error")
                    // Set the username text view to an error message if the database call fails
                    textViewUserName?.text = "Error fetching user data"
                }
            })

            val walletRef = db.getReference("users").child(userId).child("transactions").child("amount")
            walletRef.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange: snapshot: $snapshot")
                    // Get the username from the snapshot
                    val walletAmount = snapshot.getValue<String>()
                    // Set the username text view
                    vAmount?.text = "Wallet: " + (walletAmount ?: "No amount found").toString()
                }


                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: $error")
                    // Set the username text view to an error message if the database call fails
                    vAmount?.text = "Error fetching user data"
                }
            })
        }

        Log.d(TAG, "onViewCreated: user: $user")

        val buttonClickAnimation = AlphaAnimation(1f, 0.5f).apply {
            duration = 200
            repeatMode = Animation.REVERSE
            repeatCount = 1
        }

        binding.addTransaction.setOnClickListener {
            it.startAnimation(buttonClickAnimation)
            Log.d(TAG, "onClick: Going to create transaction page")
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, CreateTransactionFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.viewTransition.setOnClickListener {
            it.startAnimation(buttonClickAnimation)
            Log.d(TAG, "onClick: Going to view transaction page")
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, TransactionRecords())
            transaction.addToBackStack(null)
            transaction.commit()
           
        }

        binding.viewReport.setOnClickListener {
            it.startAnimation(buttonClickAnimation)
            Log.d(TAG, "onClick: Going to view report page")
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, ReportFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        // Clear the binding when the fragment is destroyed
        _binding = null
    }

    private fun loadChartData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("Firebase", "User not logged in.")
            return
        }

        val userId = currentUser.uid
        val transactionsRef = FirebaseDatabase.getInstance().getReference("users/$userId/transactions")

        transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactionData = mutableMapOf<String, Float>()

                for (transactionSnapshot in snapshot.children) {
                    val title = transactionSnapshot.child("title").getValue(String::class.java)
                    val amount = transactionSnapshot.child("amount").getValue(Float::class.java)

                    if (!title.isNullOrBlank() && amount != null) {
                        transactionData[title] = amount
                    }
                }

                if (transactionData.isEmpty()) {
                    Log.w("Firebase", "No transactions found.")
                } else {
                    showChart(transactionData)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
            }
        })
    }

    private fun showChart(transactions: Map<String, Float>) {
        val entries = ArrayList<BarEntry>()
        val labels = transactions.keys.toList()

        for ((index, title) in labels.withIndex()) {
            entries.add(BarEntry(index.toFloat(), transactions[title] ?: 0f))
        }

        val dataSet = BarDataSet(entries, "Transactions").apply {
            color = Color.rgb(114,191,120)
        }

        val data = BarData(dataSet)
        data.barWidth = 0.9f

        chart.data = data
        chart.setFitBars(true)

        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.granularity = 1f
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.invalidate()
    }
    
}