package dev.pankaj.launchernow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class StartLauncher : ComponentActivity() {

    private lateinit var timeBatteryTextView: TextView
    private var handler = Handler(Looper.getMainLooper())

    private var popupWindow : PopupWindow?=null
    private var apps: List<AppInfo> = mutableListOf()
    private var adapter : AppAdapter?=null
    private var searchEditText :EditText ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false


        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            fitsSystemWindows = true
        }
        setContentView(layout)

        val packageManager = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        timeBatteryTextView = TextView(this).apply {
            gravity = Gravity.CENTER
            textSize = 20f
            val maxWidthHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics).toInt()
            maxWidth = maxWidthHeight
            maxHeight = maxWidthHeight
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 80, 0, 0)
            layoutParams = params
        }
        layout.addView(timeBatteryTextView)
        adapter = AppAdapter({ packageName ->
            searchEditText?.setText("")
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }, { packageName ->
            showPopupMenu(layout, packageName)
        })

        val searchIcon = ContextCompat.getDrawable(this, R.drawable.ic_search_24)
        searchIcon?.setBounds(0, 0, searchIcon.intrinsicWidth, searchIcon.intrinsicHeight)

        searchEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20.dpToPx(), 10.dpToPx(), 20.dpToPx(), 10.dpToPx())
            }
            hint = "Search..."
            gravity = Gravity.START
            setTextColor(Color.WHITE)
            setHintTextColor(Color.WHITE)
            setBackgroundResource(android.R.color.transparent)
            setCompoundDrawables(searchIcon, null, null, null)
            maxLines = 1
            compoundDrawablePadding = 16.dpToPx()
            setBackgroundResource(R.drawable.edit_text_bg)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val filteredApps = apps.filter { appInfo ->
                        appInfo.name.contains(s.toString(), ignoreCase = true)
                    }
                    adapter?.updateData(filteredApps)
                }

                override fun afterTextChanged(s: Editable?) {
                    s?.let { if (s.toString().isEmpty()){
                        this@apply.clearFocus()
                    } }
                }
            })
        }
        searchEditText?.let {
            layout.addView(it)
        }

        val recyclerView = RecyclerView(this).apply {
            isHorizontalFadingEdgeEnabled = true
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength(50.dpToPx())
        }
        recyclerView.layoutManager = LinearLayoutManager(this)

        val recyclerViewContainer = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        layout.addView(recyclerView, recyclerViewContainer)
        recyclerView.adapter = adapter
        val shortcut = RelativeLayout(this).apply {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(40, 20, 40, 20)
            layoutParams = params
        }
        val phoneLayout = ImageView(this).apply {
            id = View.generateViewId()
            setImageResource(R.drawable.ic_call_24)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.CENTER_VERTICAL)
                setMargins(16, 16, 16, 16)
            }
        }
        phoneLayout.setOnClickListener {
            getPhoneCallerPackageName()?.let {pName->
                packageManager.getLaunchIntentForPackage(pName)?.let {mIntent->
                    startActivity(mIntent)
                }
            }
        }

        val cameraLayout = ImageView(this).apply {
            id = View.generateViewId()
            setImageResource(R.drawable.ic_camera_24)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                addRule(RelativeLayout.CENTER_VERTICAL)
                setMargins(16, 16, 16, 16)
            }
        }

        cameraLayout.setOnClickListener {
            getCameraPackageName()?.let {pName->
                packageManager.getLaunchIntentForPackage(pName)?.let {mIntent->
                    startActivity(mIntent)
                }
            }
        }
        shortcut.addView(phoneLayout)
        shortcut.addView(cameraLayout)

        layout.addView(shortcut)
        startUpdatingTimeAndBattery()
        val batteryIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, batteryIntentFilter)
    }

    private fun getPhoneCallerPackageName(): String? {
        val intent = Intent(Intent.ACTION_DIAL)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (resolveInfo?.activityInfo != null) {
            resolveInfo.activityInfo.packageName
        } else null
    }

    private fun getCameraPackageName(): String? {
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (resolveInfo?.activityInfo != null) {
            resolveInfo.activityInfo.packageName
        } else null
    }

    override fun onResume() {
        super.onResume()
        if (apps.isNotEmpty()){
            adapter?.updateData(apps)
        }
        CoroutineScope(Dispatchers.IO).launch {
            apps =  loadApps(packageManager)
            handler.post {
                adapter?.updateData(apps)
            }
        }
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
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(currentDate)
        val formattedDateTime = "$formattedDate, ${formattedTime.uppercase()}"

        val batteryPercentage = getBatteryPercentage()
        val timeSpannable = SpannableString(formattedDateTime)
        val batterySpannable = SpannableString("$batteryPercentage%")
        val timeSize = resources.getDimensionPixelSize(R.dimen.time_text_size)
        val batterySize = resources.getDimensionPixelSize(R.dimen.battery_text_size)
        timeSpannable.setSpan(AbsoluteSizeSpan(timeSize), 0, formattedDateTime.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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


    private suspend fun loadApps(packageManager: PackageManager): List<AppInfo> = coroutineScope {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val appsDeferred = packageManager.queryIntentActivities(intent, 0)
            .map { app ->
                async(Dispatchers.Default) {
                    AppInfo(
                        app.loadLabel(packageManager).toString(),
                        app.loadIcon(packageManager),
                        app.activityInfo.packageName
                    )
                }
            }

        appsDeferred.awaitAll()
    }


    private fun createPopupMenuView(context: Context, info: () -> Unit, uninstall: () -> Unit): LinearLayout {
        val container = LinearLayout(context)
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        container.orientation = LinearLayout.VERTICAL
        container.layoutParams = params
        container.setBackgroundColor(Color.TRANSPARENT)
        container.setPadding(40, 20, 40, 20)

        fun addMenuItem(text: String, onClick: () -> Unit) {
            val menuItem = TextView(context)
            menuItem.text = text
            menuItem.setTextColor(Color.WHITE)
            menuItem.setPadding(20, 20, 20, 20)

            menuItem.setOnClickListener { onClick() }
            container.addView(menuItem)
        }

        addMenuItem("App Info") {
            info()
        }

        addMenuItem("Uninstall") {
            uninstall()
        }
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(ContextCompat.getColor(this, R.color.black_transparent))
        shape.cornerRadius = 8f
        container.background = shape
        return container
    }

    private fun showPopupMenu(rootView: View, packageName: String) {
        val popupView = createPopupMenuView(rootView.context,
        {
            showAppInfo(rootView.context, packageName)
            popupWindow?.dismiss()
        }, {
            openUninstallWindow(rootView.context, packageName)
            popupWindow?.dismiss()
        })
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow?.isOutsideTouchable = true
        popupWindow?.isFocusable = true
        popupWindow?.showAtLocation(rootView, Gravity.START, 20, 0)
    }

    private fun showAppInfo(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun openUninstallWindow(context: Context, packageName: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:$packageName")
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            val intent = Intent("android.intent.action.UNINSTALL_PACKAGE")
            intent.putExtra("android.intent.extra.PACKAGE_NAME", packageName)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}


