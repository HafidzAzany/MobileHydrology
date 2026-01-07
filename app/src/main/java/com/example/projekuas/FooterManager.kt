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

        // Reset state & Highlight current page
        highlightCurrentPage(activity)

        navHome?.setOnClickListener {
            if (activity !is MainActivity) {
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                activity.startActivity(intent)
            }
        }

        navHistory?.setOnClickListener {
            // Uncomment when HistoryActivity is ready
            /*
            if (activity !is HistoryActivity) {
                val intent = Intent(activity, HistoryActivity::class.java)
                activity.startActivity(intent)
            }
            */
        }

        navTips?.setOnClickListener {
            // Uncomment when TipsActivity is ready
            /*
            if (activity !is TipsActivity) {
                val intent = Intent(activity, TipsActivity::class.java)
                activity.startActivity(intent)
            }
            */
        }

        navSettings?.setOnClickListener {
            if (activity !is SettingsActivity) {
                val intent = Intent(activity, SettingsActivity::class.java)
                activity.startActivity(intent)
            }
        }
    }

    private fun highlightCurrentPage(activity: Activity) {
        val accentColor = ContextCompat.getColor(activity, R.color.cyan_accent)
        val defaultColor = ContextCompat.getColor(activity, R.color.text_color_secondary)

        when (activity) {
            is MainActivity -> setItemActive(activity, R.id.imgHome, R.id.tvHome, accentColor)
            is SettingsActivity -> setItemActive(activity, R.id.imgSettings, R.id.tvSettings, accentColor)
            // is HistoryActivity -> setItemActive(activity, R.id.imgHistory, R.id.tvHistory, accentColor)
            // is TipsActivity -> setItemActive(activity, R.id.imgTips, R.id.tvTips, accentColor)
        }
    }

    private fun setItemActive(activity: Activity, iconId: Int, textId: Int, color: Int) {
        activity.findViewById<ImageView>(iconId)?.setColorFilter(color)
        activity.findViewById<TextView>(textId)?.setTextColor(color)
    }
}