package com.kej.stopwatch

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
        binding.startButton.setOnClickListener {
            if (!isActive) {
                start()
            } else {
                pause()
            }
        }

        binding.countDownTextView.setOnClickListener {
            showTimePickerAlertDialog()
        }

        binding.stopButton.setOnClickListener {
            showStopAlertDialog()
        }

        binding.countDownProgressBar.progress = 100
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

                val minute = currentDeciSecond.div(10) / 60
                val second = currentDeciSecond.div(10) % 60
                val tick = currentDeciSecond % 10

                val timeText = String.format("%02d:%02d", minute, second)

                runOnUiThread {
                    binding.mainSecondTextView.text = timeText
                    binding.tickTextView.text = tick.toString()
                }
            } else {
                countDownDeciSecond -= 1
                val second = countDownDeciSecond / 10
                val progress = (second / countDownSecond.toFloat()) * 100
                runOnUiThread {
                    binding.countDownTextView.text = String.format("%02d", second)
                    binding.countDownProgressBar.progress = progress.toInt()
                }
            }

        }
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