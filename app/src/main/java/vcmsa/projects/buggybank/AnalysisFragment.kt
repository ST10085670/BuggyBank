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

    val dummyTransactions = listOf(
        Transaction(
            title = "Groceries",
            category = "Food",
            paymentMethod = "Card",
            amount = 1500.0,
            date = "2025-04-10",
            type = "Expense",
            description = "Monthly groceries",
            startTime = "14:00",
            endTime = "14:30"
        ),
        Transaction(
            title = "Bus Pass",
            category = "Transport",
            paymentMethod = "Cash",
            amount = 150.0,
            date = "2025-04-03",
            type = "Expense",
            description = "Bus fare for April",
            startTime = "09:00",
            endTime = "09:05"
        ),
        Transaction(
            title = "Gym Membership",
            category = "Health",
            paymentMethod = "Card",
            amount = 600.0,
            date = "2025-04-05",
            type = "Expense",
            description = "Monthly gym",
            startTime = "10:00",
            endTime = "10:10"
        ),
        Transaction(
            title = "Movie Night",
            category = "Entertainment",
            paymentMethod = "Card",
            amount = 270.0,
            date = "2025-04-15",
            type = "Expense",
            description = "Cinema ticket",
            startTime = "18:00",
            endTime = "20:30"
        ),
        Transaction(
            title = "Freelance Project",
            category = "Work",
            paymentMethod = "Bank Transfer",
            amount = 20000.0,
            date = "2025-04-20",
            type = "Income",
            description = "App development job",
            startTime = "09:00",
            endTime = "17:00"
        ),
        Transaction(
            title = "Allowance",
            category = "Personal",
            paymentMethod = "Cash",
            amount = 300.0,
            date = "2025-04-01",
            type = "Income",
            description = "Monthly allowance from family",
            startTime = "08:00",
            endTime = "08:10"
        )
    )

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
        analyzeDummyData()
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
            color = Color.BLUE
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


    private fun analyzeDummyData() {
        val expensesByCategory = mutableMapOf<String, Double>()
        var totalExpenses = 0.00

        for (transaction in dummyTransactions) {
            if (transaction.type == "Expense") {
                expensesByCategory[transaction.title] =
                    expensesByCategory.getOrDefault(transaction.title, 0.00) + transaction.amount
                totalExpenses += transaction.amount
            }
        }

        // Simulate current month data to compare
        val currentMonthExpenses = mapOf(
            "Food" to 1000f,
            "Transport" to 400f,
            "Entertainment" to 800f
        )

        // Calculate differences
        val savingsMap = mutableMapOf<String, Float>()
        val overspentMap = mutableMapOf<String, Float>()

        for ((category, lastMonthAmount) in expensesByCategory) {
            val currentAmount = currentMonthExpenses[category] ?: 0f
            val diff = lastMonthAmount - currentAmount
            if (diff > 0) {
                savingsMap[category] = diff.toFloat()
            } else if (diff < 0) {
                overspentMap[category] = (-diff).toFloat()
            }
        }

        val savedMost = savingsMap.maxByOrNull { it.value }?.key ?: "N/A"
      //  val overspentMost = overspentMap.maxByOrNull { it.value }?.key ?: "N/A"

        // Display results (replace with actual TextViews)
        Log.d("Analysis", "Total Expenses: R $totalExpenses")
        Log.d("Analysis", "Saved Most In: $savedMost")
       // Log.d("Analysis", "Overspent Most In: $overspentMost")

        // Example usage in UI
        view?.findViewById<TextView>(R.id.txtTotalExpensesData)?.text = "R $totalExpenses"
        view?.findViewById<TextView>(R.id.txtSavedMostData)?.text = savedMost
       // view?.findViewById<TextView>(R.id.txtOverspentData)?.text = overspentMost
    }

}
