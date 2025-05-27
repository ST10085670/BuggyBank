package vcmsa.projects.buggybank

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AnalysisFragment : Fragment() {

    private lateinit var chart: BarChart
    private lateinit var database: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chart = view.findViewById(R.id.statusBar)
        database = FirebaseDatabase.getInstance().reference
        analyzeData()
        loadChartData()
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


    private fun analyzeData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("Firebase", "User not logged in.")
            return
        }

        val userId = currentUser.uid
        val transactionsRef = FirebaseDatabase.getInstance().getReference("users/$userId/transactions")

        val expensesByCategory = mutableMapOf<String, Double>()
        var totalExpenses = 0.0
        var totalIncome = 0.0

        transactionsRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Log.d("Analysis", "No transaction data found.")
                return@addOnSuccessListener
            }

            for (transactionSnap in snapshot.children) {
                val type = transactionSnap.child("type").getValue(String::class.java)
                val category = transactionSnap.child("category").getValue(String::class.java)
                val amount = transactionSnap.child("amount").getValue(Double::class.java) ?: 0.0

                when (type) {
                    "Expense" -> {
                        val cat = category ?: "Unknown"
                        expensesByCategory[cat] = expensesByCategory.getOrDefault(cat, 0.0) + amount
                        totalExpenses += amount
                    }
                    "Income" -> {
                        totalIncome += amount
                    }
                }
            }

            // Determine the category with the highest total expense
            val highestExpenseCategory = expensesByCategory.maxByOrNull { it.value }?.key ?: "N/A"

            // Display results in TextViews
            view?.findViewById<TextView>(R.id.txtTotalExpensesData)?.text = "R %.2f".format(totalExpenses)
            view?.findViewById<TextView>(R.id.txtTotalIncomeData)?.text = "R %.2f".format(totalIncome)
            view?.findViewById<TextView>(R.id.txtHighestExpenseData)?.text = highestExpenseCategory

            // Logging
            Log.d("Analysis", "Total Expenses: R $totalExpenses")
            Log.d("Analysis", "Total Income: R $totalIncome")
            Log.d("Analysis", "Highest Expense Category: $highestExpenseCategory")

        }.addOnFailureListener {
            Log.e("Analysis", "Failed to load transactions: ${it.message}")
        }
    }


}