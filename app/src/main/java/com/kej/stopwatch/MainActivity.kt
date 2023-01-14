package com.kej.stopwatch

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import com.kej.stopwatch.databinding.ActivityMainBinding
import com.kej.stopwatch.databinding.DialogTimePickerBinding
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var countDownSecond = 0
    private var countDownDeciSecond = 0
    private var currentDeciSecond = 0
    private var timer: Timer? = null
    private var isActive = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        buttonInit()
        binding.countDownTextView.setOnClickListener {
            showTimePickerAlertDialog()
        }
        binding.countDownProgressBar.progress = 100
    }

    private fun buttonInit() {
        binding.startButton.setOnClickListener {
            if (!isActive) {
                start()
            } else {
                pause()
            }
            buttonChange()
        }

        binding.stopButton.setOnClickListener {
            if (!isActive) {
                showStopAlertDialog()
            } else {
                lap()
            }
            buttonChange()
        }
    }


    private fun buttonChange() {
        binding.startButton.apply {
            if (!isActive) {
                setImageResource(R.drawable.ic_baseline_play_arrow_24)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.green))

            } else {
                setImageResource(R.drawable.ic_baseline_pause_24)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.yellow))
            }
        }

        binding.stopButton.apply {
            if (!isActive) {
                setImageResource(R.drawable.ic_baseline_stop_24)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.red))
            } else {
                setImageResource(R.drawable.ic_baseline_check_24)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.blue))
            }
        }

    }

    private fun showTimePickerAlertDialog() {
        val dialogBinding = DialogTimePickerBinding.inflate(layoutInflater)
        dialogBinding.numberPicker.apply {
            maxValue = 30
            minValue = 0
            value = countDownSecond
        }

        AlertDialog.Builder(this).apply {
            setTitle("카운트 다운 설정")
            setPositiveButton("확인") { _, _ ->
                countDownSecond = dialogBinding.numberPicker.value
                countDownDeciSecond = countDownSecond * 10
                binding.countDownTextView.text = String.format("%02d", countDownSecond)
            }
            setNegativeButton("취소", null)
            setView(dialogBinding.root)
        }.show()
    }

    private fun start() {
        isActive = true
        timer = timer(initialDelay = 0, period = 100) {
            if (countDownDeciSecond == 0) {
                currentDeciSecond += 1
                val (tick, timeText) = getTimeText()
                runOnUiThread {
                    binding.group.visibility = View.GONE
                    binding.mainSecondTextView.text = timeText
                    binding.tickTextView.text = tick.toString()
                }
            } else {
                countDownDeciSecond -= 1
                val second = countDownDeciSecond / 10
                val progress = (countDownDeciSecond / (countDownSecond * 10f)) * 100
                binding.root.post {
                    binding.countDownTextView.text = String.format("%02d", second)
                    binding.countDownProgressBar.progress = progress.toInt()
                }
            }

            if (currentDeciSecond == 0 && countDownDeciSecond < 31 && countDownDeciSecond % 10 == 0) {
                val toneType = if (countDownDeciSecond == 0) {
                    ToneGenerator.TONE_CDMA_HIGH_L
                } else {
                    ToneGenerator.TONE_CDMA_ANSWER
                }
                ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME).startTone(toneType, 100)
            }

        }
    }

    private fun getTimeText(): Pair<Int, String> {
        val minute = currentDeciSecond.div(10) / 60
        val second = currentDeciSecond.div(10) % 60
        val tick = currentDeciSecond % 10
        val timeText = String.format("%02d:%02d", minute, second)
        return Pair(tick, timeText)
    }

    private fun pause() {
        isActive = false
        timer?.cancel()
        timer = null
    }

    private fun stop() {
        timer?.cancel()
        timer = null
        currentDeciSecond = 0
        binding.mainSecondTextView.text = "00:00"
        binding.tickTextView.text = "0"
        binding.group.visibility = View.VISIBLE
        binding.containerLayout.removeAllViews()
    }


    @SuppressLint("SetTextI18n")
    private fun lap() {
        if (!isActive) {
            return
        }

        binding.containerLayout.apply {
            TextView(this@MainActivity).apply {
                gravity = Gravity.CENTER
                val (tick, timeText) = getTimeText()
                text = "${childCount.inc()}.  $timeText $tick"
                setPadding(30)
                textSize = 20f
            }.let {
                addView(it)
            }
        }


    }

    private fun showStopAlertDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("타이머를 종료하시겠습니까?")
            setPositiveButton("네") { _, _ ->
                stop()
            }
            setNegativeButton("아니요") { _, _ -> }
        }.show()
    }
}