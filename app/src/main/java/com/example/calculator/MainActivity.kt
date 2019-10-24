package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

import net.objecthunter.exp4j.ExpressionBuilder


class MainActivity : AppCompatActivity() {
    //* ! Dynamically append the input and output text value in the TextView
    lateinit var textInput: TextView

    //* ! Indicate the last input is a number or not
    var  lastNumeric: Boolean = false

    //* ! Indicate if the current state is error or not
    var stateError: Boolean = false

    //* ! Only allow a single dot (when lastDot is true)
    var lastDot: Boolean = false

    //* ! Flag for the sign, indicate if current sign is negative
    var isNegative = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textInput = findViewById(R.id.textInput)
    }


    /**
     * ! Append the Button.text to the TextView
     */
    fun onDigit(view: View) {
        if (stateError) {
            // If current state is Error, replace the error message
            textInput.text = (view as Button).text
            stateError = false
        } else {
            val txt = textInput.text.toString()
            if (txt == "0") {
                textInput.text = (view as Button).text
            } else {
                // If not, already there is a valid expression so append to it
                textInput.append((view as Button).text)
            }
        }
        // Set the flag
        lastNumeric = true
    }

    /**
     * ! Append . to the TextView
     */
    fun onDecimalPoint(view: View) {
        if (lastNumeric && !stateError && !lastDot) {
            textInput.append(".")
            lastNumeric = false
            lastDot = true
        }
    }


    /**
     * ! Append +,-,*,/ operators to the TextView
     */
    fun onOperator(view: View) {
        if (lastNumeric && !stateError) {
            textInput.append((view as Button).text)
            lastNumeric = false
            lastDot = false    // Reset the DOT flag
        }
    }


    /**
     * ! Clear the TextView
     */
    fun onClear(view: View) {
        this.textInput.text = ""
        lastNumeric = false
        stateError = false
        lastDot = false

    }


    /**
     * !  A "backspace" and will remove the last character on the screen
     */
    fun onDelete(view: View) {
        // If the current state is error, nothing to do.
        if(!stateError){
            // Read the expression
            val txt = textInput.text.toString()
            if (txt.length > 1 ) {
                textInput.text = txt.substring(0, txt.length - 1)
                val isLastCharDigit = textInput.text.last().isDigit()
                lastNumeric = isLastCharDigit
                val lastChar = textInput.text.last()
                lastDot = lastChar == '.'
            }
            if (txt.length <= 1 ) {
                textInput.text = "0"
                lastNumeric = true
                lastDot = false
                isNegative = false
            }
        }

    }

    /**
     * ! Add a sign on the current input number
     */
    fun onSign(view: View) {
        // If the current state is error, nothing to do.
        if(!stateError) {
            val digitRegex = Regex("([0-9]+\\.)?[0-9]+")
            val negativeDigitRegex = Regex("\\(-([0-9]+\\.)?[0-9]+\\)")

            val txt = textInput.text.toString()
            val digitSq = digitRegex.findAll(txt)
            val digitList = digitSq.map { it.value }.toList()
            if (digitList.size == 1 && digitList[0] != "0") {
                if (!isNegative) {
                    val oldValue = digitList[0]
                    val newValue = "-" + digitList[0]
                    textInput.text = txt.replace(oldValue, newValue)
                    isNegative = true
                } else {
                    val oldValue = "-" + digitList[0]
                    val newValue = digitList[0]
                    textInput.text = txt.replace(oldValue, newValue)
                    isNegative = false
                }
            }

            if (digitList.size > 1) {
                textInput.text = digitList[digitList.size-1]
            }

        }
    }

    /**
     * ! Calculate the output using Exp4j
     */
    fun onEqual(view: View) {
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        if (lastNumeric && !stateError) {
            // Read the expression
            val txt = textInput.text.toString()

            // Create an Expression (A class from exp4j library)
            val expression = ExpressionBuilder(txt).build()
            try {
                // Calculate the result and display
                val result = expression.evaluate()
                textInput.text = result.toString()
                lastDot = true // Result contains a dot
            } catch (ex: ArithmeticException) {
                // Display an error message
                textInput.text = "Error"
                stateError = true
                lastNumeric = false
            }
        }
    }

}
