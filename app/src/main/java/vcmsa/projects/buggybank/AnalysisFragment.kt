package vcmsa.projects.buggybank

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar

class AnalysisFragment : Fragment() {

    private lateinit var chart: BarChart
    private lateinit var database: DatabaseReference
    private var fromDate: Long = 0L
    private var toDate: Long = System.currentTimeMillis()

    private lateinit var btnStart: Button
    private lateinit var btnEnd: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chart = view.findViewById(R.id.statusBar)
        btnStart = view.findViewById(R.id.btnSelectStartDate)
        btnEnd = view.findViewById(R.id.btnSelectEndDate)
        database = FirebaseDatabase.getInstance().reference

        setupDatePickers()
        loadChartData()
    }

    private fun setupDatePickers() {
        btnStart.setOnClickListener {
            showDatePicker { calendar ->
                fromDate = calendar.timeInMillis
                btnStart.text = "Start: ${SimpleDateFormat("dd/MM/yyyy").format(calendar.time)}"
                loadChartData()
            }
        }

        btnEnd.setOnClickListener {
            showDatePicker { calendar ->
                toDate = calendar.timeInMillis
                btnEnd.text = "End: ${SimpleDateFormat("dd/MM/yyyy").format(calendar.time)}"
                loadChartData()
            }
        }
    }

    private fun showDatePicker(onDateSet: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance()
                selected.set(year, month, dayOfMonth, 0, 0, 0)
                onDateSet(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadChartData() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = currentUser.uid
        val transactionsRef = database.child("users").child(userId).child("transactions")
        val goalsRef = database.child("users").child(userId).child("goals")

        transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactionData = mutableMapOf<String, Float>()
                for (transaction in snapshot.children) {
                    val title = transaction.child("title").getValue(String::class.java)
                    val amount = transaction.child("amount").getValue(Float::class.java)
                    val timestamp = transaction.child("timestamp").getValue(Long::class.java)

                    if (!title.isNullOrBlank() && amount != null && timestamp != null) {
                        if (timestamp in fromDate..toDate) {
                            transactionData[title] = transactionData.getOrDefault(title, 0f) + amount
                        }
                    }
                }

                goalsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(goalSnap: DataSnapshot) {
                        val goals = mutableMapOf<String, Pair<Float, Float>>()
                        for (goal in goalSnap.children) {
                            val min = goal.child("min").getValue(Float::class.java) ?: 0f
                            val max = goal.child("max").getValue(Float::class.java) ?: 0f
                            goals[goal.key ?: ""] = min to max
                        }

                        showChart(transactionData, goals)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Goals error: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Transaction error: ${error.message}")
            }
        })
    }

    private fun showChart(transactions: Map<String, Float>, goals: Map<String, Pair<Float, Float>>) {
        val entries = ArrayList<BarEntry>()
        val minEntries = ArrayList<BarEntry>()
        val maxEntries = ArrayList<BarEntry>()
        val labels = transactions.keys.toList()

        for ((index, category) in labels.withIndex()) {
            val amount = transactions[category] ?: 0f
            entries.add(BarEntry(index.toFloat(), amount))

            val (minGoal, maxGoal) = goals[category] ?: (0f to 0f)
            minEntries.add(BarEntry(index.toFloat(), minGoal))
            maxEntries.add(BarEntry(index.toFloat(), maxGoal))
        }

        val dataSet = BarDataSet(entries, "Spent").apply {
            color = Color.rgb(114,191,120)
        }
        val minSet = BarDataSet(minEntries, "Min Goal").apply {
            color = Color.rgb(255,165,0)
        }
        val maxSet = BarDataSet(maxEntries, "Max Goal").apply {
            color = Color.rgb(255,0,0)
        }

        val data = BarData(dataSet, minSet, maxSet)
        data.barWidth = 0.2f

        chart.data = data
        chart.groupBars(0f, 0.4f, 0.05f)

        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.granularity = 1f
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.setFitBars(true)
        chart.invalidate()
    }
}
