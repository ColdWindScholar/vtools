package com.projectkr.shell

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.omarea.krscript.model.PageNode
import com.omarea.vtools.activities.ActionPage
import com.omarea.vtools.activities.ActivityAddinOnline

class OpenPageHelper(private var activity: Activity) {

    fun openPage(pageNode: PageNode) {
        try {
            var intent: Intent? = null
            if (!pageNode.onlineHtmlPage.isEmpty()) {
                intent = Intent(activity, ActivityAddinOnline::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("url", pageNode.onlineHtmlPage)
            }

            if (!pageNode.pageConfigSh.isEmpty()) {
                if (intent == null) {
                    intent = Intent(activity, ActionPage::class.java)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (!pageNode.pageConfigPath.isEmpty()) {
                if (intent == null) {
                    intent = Intent(activity, ActionPage::class.java)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            intent?.run {
                intent.putExtra("page", pageNode)
                activity.startActivity(intent)
            }
        } catch (ex: Exception) {
            Toast.makeText(activity, "" + ex.message, Toast.LENGTH_SHORT).show()
        }
    }
}
