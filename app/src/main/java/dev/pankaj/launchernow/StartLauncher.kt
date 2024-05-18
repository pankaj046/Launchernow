package dev.pankaj.launchernow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StartLauncher : ComponentActivity() {

    private lateinit var timeBatteryTextView: TextView
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            fitsSystemWindows = true
        }
        setContentView(layout)

        val packageManager = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(intent, 0)
            .map { app ->
                AppInfo(
                    app.loadLabel(packageManager).toString(),
                    app.loadIcon(packageManager),
                    app.activityInfo.packageName
                )
        }

        timeBatteryTextView = TextView(this).apply {
            gravity = Gravity.CENTER
            textSize = 20f
            text = "Loading..."
            val maxWidthHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics).toInt()
            maxWidth = maxWidthHeight
            maxHeight = maxWidthHeight
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
        }
        layout.addView(timeBatteryTextView)

        val adapter = AppAdapter { packageName ->
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }

        val searchEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(40.dpToPx(), 10.dpToPx(), 40.dpToPx(), 10.dpToPx())
            }
            hint = "Search..."
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setHintTextColor(Color.WHITE)
            setBackgroundResource(android.R.color.transparent)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val filteredApps = apps.filter { appInfo ->
                        appInfo.name.contains(s.toString(), ignoreCase = true)
                    }
                    adapter.updateData(filteredApps)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
        layout.addView(searchEditText)

        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        layout.addView(recyclerView)

        recyclerView.adapter = adapter
        adapter.updateData(apps)

        handler = Handler(Looper.getMainLooper())
        startUpdatingTimeAndBattery()
        val batteryIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, batteryIntentFilter)
    }


    private fun startUpdatingTimeAndBattery() {
        handler.post(object : Runnable {
            override fun run() {
                updateTimeAndBattery()
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun updateTimeAndBattery() {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val batteryPercentage = getBatteryPercentage()
        val timeSpannable = SpannableString(currentTime)
        val batterySpannable = SpannableString("$batteryPercentage%")
        val timeSize = resources.getDimensionPixelSize(R.dimen.time_text_size)
        val batterySize = resources.getDimensionPixelSize(R.dimen.battery_text_size)
        timeSpannable.setSpan(AbsoluteSizeSpan(timeSize), 0, currentTime.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        batterySpannable.setSpan(AbsoluteSizeSpan(batterySize), 0, batterySpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val timeBatteryText = TextUtils.concat(timeSpannable, "\n", batterySpannable)
        timeBatteryTextView.text = timeBatteryText
    }


    private fun getBatteryPercentage(): Int {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return batteryIntent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level != -1 && scale != -1) {
                (level / scale.toFloat() * 100).toInt()
            } else {
                0
            }
        } ?: 0
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateTimeAndBattery()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        handler.removeCallbacksAndMessages(null)
    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

}


