/**
 *  File Name: MainActivity.kt
 *  Project Name: Calculator
 *  Copyright @ Hanlin Hu 2019
 */

package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

// Need to import the 3rd party lib: exp4j
// URL: https://www.objecthunter.net/exp4j/index.html
import net.objecthunter.exp4j.ExpressionBuilder


class MainActivity : AppCompatActivity() {
    //* ! Dynamically append the input and output text value in the TextView
    lateinit var textInput: TextView

    //* ! Indicate the last input is a number or not
    var  isLastNumeric: Boolean = false

    //* ! Indicate if the current state is error or not
    var isStateError: Boolean = false

    //* ! Only allow a single dot (when isLastDot is true)
    var isLastDot: Boolean = false

    //* ! Flag for the sign, indicate if current sign is negative
    var isNegative: Boolean = false

    //* ! Flag indicates is if the current cursor is in the brackets
    var isInBrackets: Boolean = false

    //* ! Flag indicates if there is an existing operator
    var isLastOperator: Boolean = false

    //* ! Flag indicates if the text is a calculated result
    var isCalculated: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textInput = findViewById(R.id.textInput)
        // Default text in textView is '0'
        textInput.text = "0"
    }

    /**
     * ! Append the Button.text to the TextView
     */
    fun onDigit(view: View) {
        if (isStateError) {
            // If current state is Error, replace the error message
            textInput.text = (view as Button).text
            isStateError = false
        } else {
            val txt = textInput.text.toString()
            if (txt == "0") {
                textInput.text = (view as Button).text
            } else {
                if (isInBrackets && txt[txt.lastIndex] == ')')
                {
                    // If text length reaches the maximum, then it does not allow append
                    if (txt.length == getString(R.string.max_length).toInt())
                    {
                        textInput.text = txt
                    } else {
                        textInput.text = txt.substring(0, txt.length - 1) +
                                (view as Button).text.toString() + ")"
                    }
                } else if (isCalculated) {
                    // If the text is a calculated result.
                    // onClicking a digit will clear the textView and append the digit
                    onClear(view)
                    textInput.text = (view as Button).text
                } else textInput.append((view as Button).text)
            }
        }
        // Set the flag
        isLastNumeric = true
    }

    /**
     * ! Append . to the TextView
     */
    fun onDecimalPoint(view: View) {
        if (isLastNumeric && !isStateError && !isLastDot && !isCalculated) {
            if (isInBrackets) {
                // Read the expression
                val txt = textInput.text.toString()
                // If text length reaches the maximum, then it does not allow append
                if (txt.length == getString(R.string.max_length).toInt()) {
                    textInput.text = txt
                } else {
                    textInput.text = txt.substring(0, txt.length - 1) + "." + ")"
                }
            } else textInput.append(".")
            isLastNumeric = false
            isLastDot = true
        }
    }

    /**
     * ! Append +,-,*,/ operators to the TextView
     */
    fun onOperator(view: View) {
        if (isLastNumeric && !isStateError) {
            if (!isLastOperator) {
                textInput.append((view as Button).text)
            } else {
                onEqual(view)
                textInput.append((view as Button).text)
            }
            // Reset flags
            isLastDot = false
            isLastOperator = true
            isLastNumeric = false
            isCalculated = false
        } else if (!(textInput.text.last() != '+' && textInput.text.last() != '-'
                     && textInput.text.last() != '*' && textInput.text.last() != '/')
                  ){
                    val txt = textInput.text.toString()
                    textInput.text = txt.substring(0, txt.length-1) + (view as Button).text
                    isCalculated = false
               }
    }

    /**
     * ! Clear the TextView
     */
    fun onClear(view: View) {
        // Reset boolean flags and textView.
        textInput.text = "0"
        isLastNumeric = false
        isStateError = false
        isLastDot = false
        isNegative = false
        isInBrackets = false
        isLastOperator = false
        isCalculated = false
    }

    /**
     * !  A "backspace", and it removes the last character on the screen
     */
    fun onDelete(view: View) {
        // If the current state is error, nothing to do.
        if (!isStateError){
            // Read the expression
            val txt = textInput.text.toString()
            // If expression's length is greater than 1, do deletion.
            // Else expression's length eq or less than 1, reset boolean flags and textView
            if (txt.length > 1 ) {
                // If is in the bracket, operate the second last digit
                // Else is not in the bracket, operate the last digit
                if (isInBrackets) {
                    // Special case:  if the deletion is on the "(-)"
                    if (txt[txt.lastIndex] == ')'
                        && txt[txt.lastIndex - 1] == '-'
                        && txt[txt.lastIndex - 2] == '('
                    ) {
                        textInput.text = txt.substring(0, txt.length - 3)
                        isLastNumeric = false
                        isNegative = false
                        isInBrackets = false
                    } else {
                        if (txt[txt.lastIndex-1] == '.') { isLastDot = false }
                        val secondLastDigit = txt.substring(0, txt.length - 2)
                        textInput.text = secondLastDigit + ")"
                        isLastNumeric = true
                        isNegative = true
                    }
                } else {
                    if (textInput.text.last() == '.') { isLastDot = false }
                    if (!(textInput.text.last() != '+' && textInput.text.last() != '-'
                          && textInput.text.last() != '*' && textInput.text.last() != '/')
                       ) {
                            isLastOperator = false
                            // To decide if the last number has dot or not
                            val digitRegex = Regex("([0-9]+\\.)?[0-9]+")
                            val dotRegex = Regex("\\.")
                            val digitSq = digitRegex.findAll(txt)
                            val digitList = digitSq.map { it.value }.toList()
                            val lastNumber = digitList[digitList.size - 1]
                            isLastDot = dotRegex.containsMatchIn(lastNumber)
                    }
                    textInput.text = txt.substring(0, txt.length - 1)
                    isLastNumeric = textInput.text.last().isDigit()
                }
            } else {
                textInput.text = "0"
                isLastNumeric = true
                isLastDot = false
                isNegative = false
                isLastOperator = false
            }
            isCalculated = false
        }
    }

    /**
     * ! Process Replacement function (is used by replaceLast)
     *   Copy and Paste Kotlin's source code from original Regex class
     *   Because the Regex class has method "replaceFirst" function
     *     but no "replaceLast" function
     *   I need to use a part of its code
     *     to write a new "replaceLast" for the "+/-" button functionality
     *   URL: https://github.com/JetBrains/kotlin-native/blob/master/runtime/src/main/kotlin/
     *              kotlin/text/Regex.kt
     *   Line #: 188
     */
    fun processReplacement(match: MatchResult, replacement: String): String {
        val result = StringBuilder(replacement.length)
        var escaped = false
        var backReference = false
        for (ch in replacement) {
            when {
                escaped -> {
                    result.append(ch)
                    escaped = false
                }
                backReference -> {
                    if (ch !in '0'..'9') {
                        throw IllegalArgumentException("Incorrect back reference: $ch.")
                    }
                    val group = ch - '0'
                    result.append(match.groupValues[group])
                    // We don't catch IndexOutOfBoundException here because
                    // it's a correct exception in case of a wrong group number.
                    // TODO: But we can rethrow it with more informative message.
                    backReference = false
                }
                ch == '\\' -> escaped = true
                ch == '$' -> backReference = true
                else -> result.append(ch)
            }
        }
        if (backReference || escaped) {
            throw IllegalArgumentException("Unexpected end of replacement.")
        }
        return result.toString()
    }

    /**
     * ! Find the last occurance from input sting and a regular expression pattern
     */
    fun findLast(regex: Regex, input: CharSequence): String {
        val find_all = regex.findAll(input)
        val match = find_all.last()
        return match.value
    }

    /**
     * ! Replace last occurance
     *   This is a modification of the "replaceFirst" from Kotlin's Regex class
     *   URL: https://github.com/JetBrains/kotlin-native/blob/master/runtime/src/main/kotlin/
     *              kotlin/text/Regex.kt
     *   Line #: 260
     */
    fun replaceLast(regex: Regex, input: CharSequence, replacement: String): String {
        val find_all = regex.findAll(input)
        val match = find_all.last()

        val length = input.length
        val result = StringBuilder(length)
        result.append(input, 0, match.range.start)
        result.append(processReplacement(match, replacement))
        if (match.range.endInclusive + 1 < length) {
            result.append(input, match.range.endInclusive + 1, length)
        }
        return result.toString()
    }

    /**
     * ! Add a sign on the current input number
     */
    fun onSign(view: View) {
        // If the current state is error, nothing to do.
        if (!isStateError) {
            val digitRegex = Regex("([0-9]+\\.)?[0-9]+")
            val negativeDigitRegex = Regex("\\(-([0-9]+\\.)?[0-9]+\\)")

            val txt = textInput.text.toString()
            val digitSq = digitRegex.findAll(txt)
            val digitList = digitSq.map { it.value }.toList()

            // If expression has only one non-zero number, then toggle sign operator
            if (digitList.size == 1 && digitList[0] != "0") {
                if (!isNegative) {
                    val oldValue = digitList[0]
                    val newValue = "-" + digitList[0]
                    textInput.text = txt.replace(oldValue, newValue)
                    isNegative = true
                    isInBrackets = false
                } else {
                    val oldValue = "-" + digitList[0]
                    val newValue = digitList[0]
                    textInput.text = txt.replace(oldValue, newValue)
                    isNegative = false
                    isInBrackets = false
                }
            }

            // If expression has more than one number, then toggle sign operator and brackets
            if (digitList.size > 1) {
                if (!isInBrackets) {
                    if (txt.length <= getString(R.string.max_length).toInt()-3) {
                        val lastStr = findLast(digitRegex, txt)
                        val newStr = "(-$lastStr)"
                        textInput.text = replaceLast(digitRegex, txt, newStr)
                        isNegative = true
                        isInBrackets = true
                    } else textInput.text = txt
                } else {
                    val lastStr = findLast(negativeDigitRegex, txt)
                    val newStr = lastStr.subSequence(2, lastStr.length-1).toString()
                    textInput.text = replaceLast(negativeDigitRegex, txt, newStr)
                    isNegative = false
                    isInBrackets = false
                }
            }
        }
    }

    /**
     * ! Calculate the output using Exp4j
     */
    fun onEqual(view: View) {
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        if (isLastNumeric && !isStateError) {
            // Read the expression
            val txt = textInput.text.toString()
            // Create an Expression (A class from exp4j library)
            val expression = ExpressionBuilder(txt).build()
            try {
                // Calculate the result and display, and format to a 2-decimal string
                val result =  "%.2f".format(expression.evaluate())
                isNegative = result.toDouble() < 0.0 // If result < 0, then set to negative
                textInput.text = result
                isLastDot = true // Result has a dot
                isInBrackets = false // Result does not contain brackets
                isLastOperator = false // Reset flag
                isCalculated = true
            } catch (ex: ArithmeticException) {
                // Display an error message
                textInput.text = "Error"
                isStateError = true
                isLastNumeric = false

                // Optional: Reset flags
                isLastDot = false
                isNegative = false
                isInBrackets = false
                isLastOperator = false
                isCalculated = false
            }
        }
    }

}
