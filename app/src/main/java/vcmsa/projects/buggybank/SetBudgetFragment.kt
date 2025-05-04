package vcmsa.projects.buggybank

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

class SetBudgetFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_set_budget, container, false)

        val seekBar = view.findViewById<SeekBar>(R.id.seekBarMin)
        val txtSeekValue = view.findViewById<TextView>(R.id.txtSeekValue)
        // Now safely access views from the inflated layout
        val changingHeading = view.findViewById<TextView>(R.id.txtHeadingChange)

        val btnEntertainment = view.findViewById<Button>(R.id.btnEntertainment)
        val btnHealth = view.findViewById<Button>(R.id.btnHealth)
        val btnHousing = view.findViewById<Button>(R.id.btnHousing)
        val btnClothing = view.findViewById<Button>(R.id.btnClothing)
        val btnFood = view.findViewById<Button>(R.id.btnFood)
        val btnFuel = view.findViewById<Button>(R.id.btnFuel)
        val btnGroceries = view.findViewById<Button>(R.id.btnGroceries)
        val btnInsurance = view.findViewById<Button>(R.id.btnInsurance)
        val btnInternet = view.findViewById<Button>(R.id.btnInternet)
        val etMaxValue = view.findViewById<EditText>(R.id.etMaxValue)
        val btnSetMax = view.findViewById<Button>(R.id.btnSetMax)


        val btnSetCatMinimum = view.findViewById<Button>(R.id.btnSet)

        // Set heading text when buttons are clicked
        btnEntertainment.setOnClickListener { changingHeading.text = "Entertainment" }
        btnHealth.setOnClickListener { changingHeading.text = "Health" }
        btnHousing.setOnClickListener { changingHeading.text = "Housing" }
        btnClothing.setOnClickListener { changingHeading.text = "Clothing" }
        btnFood.setOnClickListener { changingHeading.text = "Food" }
        btnFuel.setOnClickListener { changingHeading.text = "Fuel" }
        btnGroceries.setOnClickListener { changingHeading.text = "Groceries" }
        btnInsurance.setOnClickListener { changingHeading.text = "Insurance" }
        btnInternet.setOnClickListener { changingHeading.text = "Internet" }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtSeekValue.text = "R$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSetCatMinimum.setOnClickListener {
            val selectedCategory = changingHeading.text.toString()
            val maxValue = seekBar.progress

            if (selectedCategory.isNotEmpty()) {
                val budget = Budget(category = selectedCategory, maximumValue = maxValue)

                val dbRef = FirebaseDatabase.getInstance().getReference("budgets")
                dbRef.push().setValue(budget)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Budget saved!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT).show()
                        Log.e("FIREBASE_ERROR", "Save failed", e)
                    }
            }
            else {
                Toast.makeText(requireContext(), "Select a category first", Toast.LENGTH_SHORT).show()
            }
        }

        btnSetMax.setOnClickListener {
            val maxInput = etMaxValue.text.toString().toIntOrNull()
            if (maxInput != null && maxInput > 0) {
                seekBar.max = maxInput
                Toast.makeText(requireContext(), "SeekBar max set to $maxInput", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a valid number > 0", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }


}