package com.example.projekuas

import android.app.Activity
import android.content.Intent
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

object FooterManager {

    fun setupFooter(activity: Activity) {
        val navHome = activity.findViewById<LinearLayout>(R.id.navHome)
        val navHistory = activity.findViewById<LinearLayout>(R.id.navHistory)
        val navTips = activity.findViewById<LinearLayout>(R.id.navTips)
        val navSettings = activity.findViewById<LinearLayout>(R.id.navSettings)

        // highlight current page
        highlightCurrentPage(activity)

        navHome?.setOnClickListener {
            if (activity !is MainActivity) {
                activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                })
            }
        }

        navHistory?.setOnClickListener {
            if (activity !is HistoryActivity) {
                activity.startActivity(Intent(activity, HistoryActivity::class.java))
            }
        }

        navTips?.setOnClickListener {
            if (activity !is TipsActivity) {
                activity.startActivity(Intent(activity, TipsActivity::class.java))
            }
        }

        navSettings?.setOnClickListener {
            if (activity !is SettingsActivity) {
                activity.startActivity(Intent(activity, SettingsActivity::class.java))
            }
        }
    }

    private fun highlightCurrentPage(activity: Activity) {
        val accent = ContextCompat.getColor(activity, R.color.cyan_accent)
        val gray = ContextCompat.getColor(activity, R.color.text_color_secondary)

        // reset all to gray dulu
        setItemColor(activity, R.id.imgHome, R.id.tvHome, gray)
        setItemColor(activity, R.id.imgHistory, R.id.tvHistory, gray)
        setItemColor(activity, R.id.imgTips, R.id.tvTips, gray)
        setItemColor(activity, R.id.imgSettings, R.id.tvSettings, gray)

        when (activity) {
            is MainActivity -> setItemColor(activity, R.id.imgHome, R.id.tvHome, accent)
            is HistoryActivity -> setItemColor(activity, R.id.imgHistory, R.id.tvHistory, accent)
            is TipsActivity -> setItemColor(activity, R.id.imgTips, R.id.tvTips, accent)
            is SettingsActivity -> setItemColor(activity, R.id.imgSettings, R.id.tvSettings, accent)
        }
    }

    private fun setItemColor(activity: Activity, iconId: Int, textId: Int, color: Int) {
        activity.findViewById<ImageView>(iconId)?.setColorFilter(color)
        activity.findViewById<TextView>(textId)?.setTextColor(color)
    }
}
