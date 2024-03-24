package com.omarea.vtools.addin

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Toast
import com.omarea.common.ui.DialogHelper
import com.omarea.vtools.R
import com.omarea.vtools.activities.ActivityBase
import com.omarea.vtools.services.CompileService

/**
 * Created by Hello on 2018/02/20.
 */

class DexCompileAddin(private var context: ActivityBase) : AddinBase(context) {
    fun isSupport(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(context, "系统版本过低，至少需要Android 7.0！", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun triggerCompile (action: String) {
        if (CompileService.compiling) {
            Toast.makeText(context, "有一个后台编译过程正在进行，不能重复开启", Toast.LENGTH_SHORT).show()
        } else {
            try {
                val service = Intent(context, CompileService::class.java)
                service.action = action
                context.startService(service)
                Toast.makeText(context, "开始后台编译，请查看通知了解进度", Toast.LENGTH_SHORT).show()
            } catch (ex: java.lang.Exception) {
                Toast.makeText(context, "启动后台过程失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //增加进度显示，而且不再出现因为编译应用自身而退出
    private fun run2() {
        if (!isSupport()) {
            return
        }

        if (CompileService.compiling) {
            Toast.makeText(context, "有一个后台编译过程正在进行~", Toast.LENGTH_SHORT).show()
            return
        }

        val view = context.layoutInflater.inflate(R.layout.dialog_addin_compile, null)
        val dialog = DialogHelper.customDialog(context, view)
        view.findViewById<View>(R.id.mode_speed_profile).setOnClickListener {
            dialog.dismiss()
            triggerCompile(context.getString(R.string.scene_speed_profile_compile))
        }
        view.findViewById<View>(R.id.mode_speed).setOnClickListener {
            dialog.dismiss()
            triggerCompile(context.getString(R.string.scene_speed_compile))
        }
        view.findViewById<View>(R.id.mode_everything).setOnClickListener {
            dialog.dismiss()
            triggerCompile(context.getString(R.string.scene_everything_compile))
        }
        view.findViewById<View>(R.id.mode_reset).setOnClickListener {
            dialog.dismiss()
            triggerCompile(context.getString(R.string.scene_reset_compile))
        }
        view.findViewById<View>(R.id.faq).setOnClickListener {
            dialog.dismiss()
            Toast.makeText(context, "此页面，在国内(CN)可能需要“虚拟专用网络”才能正常访问", Toast.LENGTH_LONG).show()

            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.addin_dex2oat_helplink))))
        }
    }

    override fun run() {
        run2()
    }

}
