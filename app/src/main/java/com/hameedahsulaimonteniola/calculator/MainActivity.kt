@file:Suppress("SpellCheckingInspection")

package com.hameedahsulaimonteniola.calculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.hameedahsulaimonteniola.calculator.databinding.ActivityMainBinding
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var expression = ""
    private var lastResult = ""
    private var isSecond = false
    private var isDeg = true
    private var shouldClear = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val buttons = listOf(
            binding.btn0Basic, binding.btn1Basic, binding.btn2Basic, binding.btn3Basic,
            binding.btn4Basic, binding.btn5Basic, binding.btn6Basic, binding.btn7Basic,
            binding.btn8Basic, binding.btn9Basic, binding.btn0Adv, binding.btn1Adv,
            binding.btn2Adv, binding.btn3Adv, binding.btn4Adv, binding.btn5Adv,
            binding.btn6Adv, binding.btn7Adv, binding.btn8Adv, binding.btn9Adv,
            binding.btnDotBasic, binding.btnDotAdv, binding.btnPlusBasic, binding.btnPlusAdv,
            binding.btnMinusBasic, binding.btnMinusAdv, binding.btnMultiplyBasic, binding.btnMultiplyAdv,
            binding.btnDivideBasic, binding.btnDivideAdv, binding.btnOpenBracket, binding.btnCloseBracket,
            binding.btnPower, binding.btnLg, binding.btnLn, binding.btnSqrt, binding.btnFactorial,
            binding.btnInv, binding.btnPi, binding.btnE, binding.btnSin, binding.btnCos, binding.btnTan
        )

        buttons.forEach { it.setOnClickListener { view -> onButtonClick(view as Button) } }

        binding.btnAC.setOnClickListener { clear() }
        binding.btnACAdv.setOnClickListener { clear() }
        binding.btnBackspace.setOnClickListener { backspace() }
        binding.btnBackspaceAdv.setOnClickListener { backspace() }
        binding.btnAnsBasic.setOnClickListener { useAns() }
        binding.btnAnsAdv.setOnClickListener { useAns() }
        binding.btnEqualsBasic.setOnClickListener { evaluate() }
        binding.btnEqualsAdv.setOnClickListener { evaluate() }

        binding.btnSwitchToAdv.setOnClickListener {
            binding.basicPad.visibility = View.GONE
            binding.advPad.visibility = View.VISIBLE
        }

        binding.btnSwitchToBasic.setOnClickListener {
            binding.advPad.visibility = View.GONE
            binding.basicPad.visibility = View.VISIBLE
        }

        binding.btn2nd.setOnClickListener { toggleSecond() }
        binding.btnDegRad.setOnClickListener { toggleDegRad() }
    }

    private fun onButtonClick(button: Button) {
        if (shouldClear) {
            expression = ""
            shouldClear = false
        }
        val text = button.text.toString()
        expression += when (text) {
            "sin", "cos", "tan", "asin", "acos", "atan", "lg", "ln", "√x" -> "$text("
            "xʸ" -> "^"
            "x!" -> "!"
            "1/x" -> "1/("
            "π" -> "π"
            "e" -> "e"
            else -> text
        }
        updateDisplay()
    }

    private fun clear() {
        expression = ""
        binding.tvResult.text = getString(R.string.zero)
        updateDisplay()
    }

    private fun backspace() {
        if (expression.isNotEmpty()) {
            expression = expression.substring(0, expression.length - 1)
            updateDisplay()
        }
    }

    private fun useAns() {
        if (lastResult.isNotEmpty()) {
            if (shouldClear) {
                expression = lastResult
                shouldClear = false
            } else {
                expression += lastResult
            }
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        binding.tvExpression.text = expression
    }

    private fun toggleSecond() {
        if (!isDeg) return // When rad functions is being used, the "2nd" button doesn't work

        isSecond = !isSecond
        if (isSecond) {
            binding.btnSin.text = getString(R.string.asin)
            binding.btnCos.text = getString(R.string.acos)
            binding.btnTan.text = getString(R.string.atan)
            binding.btnDegRad.isEnabled = false // When showing inverse functions, deg doesn't work
            binding.btnDegRad.alpha = 0.5f
        } else {
            binding.btnSin.text = getString(R.string.sin)
            binding.btnCos.text = getString(R.string.cos)
            binding.btnTan.text = getString(R.string.tan)
            binding.btnDegRad.isEnabled = true
            binding.btnDegRad.alpha = 1.0f
        }
    }

    private fun toggleDegRad() {
        if (isSecond) return // When 2nd is active, deg doesn't work

        isDeg = !isDeg
        binding.btnDegRad.text = if (isDeg) getString(R.string.deg) else getString(R.string.rad)
        
        // When the rad functions is being used, the "2nd" button doesn't work.
        binding.btn2nd.isEnabled = isDeg
        binding.btn2nd.alpha = if (isDeg) 1.0f else 0.5f
    }

    @SuppressLint("SetTextI18n")
    private fun evaluate() {
        try {
            val result = calculate(expression)
            lastResult = formatResult(result)
            binding.tvResult.text = lastResult
            shouldClear = true
        } catch (e: Exception) {
            binding.tvResult.text = "Error"
        }
    }

    private fun formatResult(d: Double): String {
        return if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()
    }

    // Simple evaluator
    private fun calculate(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.toInt())) x += parseTerm()
                    else if (eat('−'.toInt()) || eat('-'.toInt())) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('×'.toInt()) || eat('*'.toInt())) x *= parseFactor()
                    else if (eat('÷'.toInt()) || eat('/'.toInt())) x /= parseFactor()
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor()
                if (eat('−'.toInt()) || eat('-'.toInt())) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.toInt())) {
                    x = parseExpression()
                    eat(')'.toInt())
                } else if ((ch >= '0'.toInt() && ch <= '9'.toInt()) || ch == '.'.toInt()) {
                    while ((ch >= '0'.toInt() && ch <= '9'.toInt()) || ch == '.'.toInt()) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt() || ch == '√'.toInt() || ch == 'π'.toInt() || ch == 'e'.toInt()) {
                    while (ch >= 'a'.toInt() && ch <= 'z'.toInt() || ch == '√'.toInt() || ch == 'π'.toInt() || ch == 'e'.toInt()) nextChar()
                    when (val func = str.substring(startPos, pos)) {
                        "π" -> x = PI
                        "e" -> x = E
                        else -> {
                            x = parseFactor()
                            x = when (func) {
                                "sin" -> if (isDeg) sin(Math.toRadians(x)) else sin(x)
                                "cos" -> if (isDeg) cos(Math.toRadians(x)) else cos(x)
                                "tan" -> if (isDeg) tan(Math.toRadians(x)) else tan(x)
                                "asin" -> if (isDeg) Math.toDegrees(asin(x)) else asin(x)
                                "acos" -> if (isDeg) Math.toDegrees(acos(x)) else acos(x)
                                "atan" -> if (isDeg) Math.toDegrees(atan(x)) else atan(x)
                                "lg" -> log10(x)
                                "ln" -> ln(x)
                                "√" -> sqrt(x)
                                else -> throw RuntimeException("Unknown function: $func")
                            }
                        }
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.toInt())) x = x.pow(parseFactor())
                if (eat('!'.toInt())) {
                    var fact = 1.0
                    for (i in 1..x.toInt()) fact *= i
                    x = fact
                }

                return x
            }
        }.parse()
    }
}
