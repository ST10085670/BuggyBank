package vcmsa.projects.buggybank

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorFragment : Fragment() {

    private lateinit var calcInput: EditText
    private var currentExpression = StringBuilder()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calculator, container, false)

        calcInput = view.findViewById(R.id.etCalcInput)

        val buttons = listOf(
            R.id.btnZero to "0",
            R.id.btnOne to "1",
            R.id.btnTwo to "2",
            R.id.btnThree to "3",
            R.id.btnFour to "4",
            R.id.btnFive to "5",
            R.id.btnSix to "6",
            R.id.btnSeven to "7",
            R.id.btnEight to "8",
            R.id.btnNine to "9",
            R.id.btnPlus to "+",
            R.id.btnMinus to "-",
            R.id.btnMultiply to "X",
            R.id.btnDivide to "/",
            R.id.btnOpenBracket to "(",
            R.id.btnCloseBracket to ")",
            R.id.btnPoint to ".",
        )

        buttons.forEach { (id, value) ->
            view.findViewById<Button>(id).setOnClickListener { appendToInput(value) }
        }

        view.findViewById<Button>(R.id.btnEquals).setOnClickListener { evaluateExpression() }

        view.findViewById<Button>(R.id.btnAC).setOnClickListener {
            currentExpression.clear()
            calcInput.setText("")
        }

        return view
    }

    private fun appendToInput(value: String) {
        currentExpression.append(value)
        calcInput.setText(currentExpression.toString())
    }

    private fun evaluateExpression() {
        try {
            val expression = ExpressionBuilder(currentExpression.toString()).build()
            val result = expression.evaluate()
            calcInput.setText(result.toString())
            currentExpression.clear()
            currentExpression.append(result)
        } catch (e: Exception) {
            calcInput.setText("Error")
        }
    }
}
