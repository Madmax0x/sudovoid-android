package com.aryan.sudovoid

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private enum class State { ASK_N, ASK_REQ, ASK_HEAD, ASK_DISKEND, DONE }

    private lateinit var outputContainer: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var stdinInput: EditText

    private var state = State.ASK_N
    private var n = 0
    private val requests = mutableListOf<Int>()
    private var head = 0
    private var diskEnd = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        outputContainer = findViewById(R.id.outputContainer)
        scroll = findViewById(R.id.scroll)
        stdinInput = findViewById(R.id.stdinInput)

        findViewById<TextView>(R.id.restartBtn).setOnClickListener { restart() }

        stdinInput.setOnEditorActionListener { _, actionId, event ->
            val isEnter = actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            if (isEnter) {
                submit()
                true
            } else {
                false
            }
        }

        startProgram()
    }

    private fun restart() {
        outputContainer.removeAllViews()
        state = State.ASK_N
        n = 0
        requests.clear()
        head = 0
        diskEnd = 0
        startProgram()
    }

    private fun startProgram() {
        addLine("[ RUN: DCscan.cpp — C-SCAN DISK SCHEDULING ]", LineType.META)
        addLine("", LineType.META)
        addLine("Enter number of requests: ", LineType.STDOUT)
        state = State.ASK_N
    }

    private fun submit() {
        val text = stdinInput.text.toString().trim()
        if (text.isEmpty()) return
        stdinInput.text.clear()
        addLine("> $text", LineType.ECHO)

        when (state) {
            State.ASK_N -> {
                val value = text.toIntOrNull()
                if (value == null || value <= 0 || value > 20) {
                    addLine("Invalid — enter a number between 1 and 20.", LineType.STDERR)
                    addLine("Enter number of requests: ", LineType.STDOUT)
                    return
                }
                n = value
                requests.clear()
                addLine("Enter request sequence:", LineType.STDOUT)
                state = State.ASK_REQ
            }

            State.ASK_REQ -> {
                val value = text.toIntOrNull()
                if (value == null) {
                    addLine("Invalid — enter a whole number.", LineType.STDERR)
                    return
                }
                requests.add(value)
                if (requests.size < n) {
                    // waiting for next request, no extra prompt needed (matches cin >> loop)
                } else {
                    addLine("Enter initial head position: ", LineType.STDOUT)
                    state = State.ASK_HEAD
                }
            }

            State.ASK_HEAD -> {
                val value = text.toIntOrNull()
                if (value == null) {
                    addLine("Invalid — enter a whole number.", LineType.STDERR)
                    return
                }
                head = value
                addLine("Enter disk extremity: ", LineType.STDOUT)
                state = State.ASK_DISKEND
            }

            State.ASK_DISKEND -> {
                val value = text.toIntOrNull()
                if (value == null) {
                    addLine("Invalid — enter a whole number.", LineType.STDERR)
                    return
                }
                diskEnd = value
                runCScan()
                state = State.DONE
            }

            State.DONE -> {
                addLine("[ PROCESS ALREADY EXITED — tap RESTART to run again ]", LineType.META)
            }
        }
    }

    /** Same logic as DCscan.cpp: sort requests, sweep right to disk_end, jump to 0, sweep right to remaining. */
    private fun runCScan() {
        val sorted = requests.sorted()
        var total = 0
        val seek = mutableListOf(head)

        var pos = sorted.size
        for (i in sorted.indices) {
            if (sorted[i] > head) {
                pos = i
                break
            }
        }

        var current = head
        for (i in pos until sorted.size) {
            total += abs(sorted[i] - current)
            current = sorted[i]
            seek.add(current)
        }

        total += abs(diskEnd - current)
        current = diskEnd
        seek.add(current)

        total += abs(diskEnd - 0)
        current = 0
        seek.add(current)

        for (i in 0 until pos) {
            total += abs(sorted[i] - current)
            current = sorted[i]
            seek.add(current)
        }

        addLine("", LineType.META)
        addLine("Seek Sequence:", LineType.META)
        addLine(seek.joinToString(" -> "), LineType.STDOUT)
        addLine("", LineType.META)
        addLine("Total Head Movement = $total cylinders", LineType.EXIT_OK)
        addLine("", LineType.META)
        addLine("[ PROCESS EXITED — CODE 0 — OK ]", LineType.EXIT_OK)
    }

    private enum class LineType { STDOUT, STDERR, META, ECHO, EXIT_OK }

    private fun addLine(text: String, type: LineType) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 13.5f
        tv.setTypeface(android.graphics.Typeface.MONOSPACE)
        tv.letterSpacing = 0.03f
        tv.setPadding(0, 1, 0, 1)

        val (color, glow) = when (type) {
            LineType.STDOUT -> Color.parseColor("#A8E8F8") to Color.parseColor("#8029B8E8")
            LineType.STDERR -> Color.parseColor("#FFF0A0") to Color.parseColor("#80FFC200")
            LineType.META -> Color.parseColor("#754DA0DC") to Color.TRANSPARENT
            LineType.ECHO -> Color.parseColor("#8F8FFF") to Color.parseColor("#608F8FFF")
            LineType.EXIT_OK -> Color.parseColor("#50DCA0") to Color.parseColor("#8000DC78")
        }

        tv.setTextColor(color)
        if (glow != Color.TRANSPARENT) {
            tv.setShadowLayer(8f, 0f, 0f, glow)
        }

        outputContainer.addView(tv)
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
