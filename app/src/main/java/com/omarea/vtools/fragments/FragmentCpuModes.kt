package com.omarea.vtools.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.omarea.common.ui.DialogHelper
import com.omarea.scene_mode.CpuConfigInstaller
import com.omarea.scene_mode.ModeSwitcher
import com.omarea.store.SpfConfig
import com.omarea.utils.AccessibleServiceHelper
import com.omarea.vtools.R
import com.omarea.vtools.activities.ActivityAddinOnline
import com.omarea.vtools.activities.ActivityFileSelector
import kotlinx.android.synthetic.main.fragment_cpu_modes.*
import java.io.File
import java.nio.charset.Charset


class FragmentCpuModes : Fragment() {
    private lateinit var author: String
    private var configInstalled: Boolean = false
    private lateinit var modeSwitcher: ModeSwitcher
    private lateinit var globalSPF: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_cpu_modes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        globalSPF = context!!.getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)
        configInstalled = CpuConfigInstaller().configInstalled()
        modeSwitcher = ModeSwitcher()

        if (configInstalled) {
            author = globalSPF.getString(SpfConfig.GLOBAL_SPF_CPU_CONFIG_AUTHOR, "unknown")!!
        } else {
            author = "none"
        }

        cpu_config_p4.setOnClickListener {
            Toast.makeText(context!!, "该模式暂未开放使用", Toast.LENGTH_SHORT).show()
        }
        bindMode(cpu_config_p0, ModeSwitcher.POWERSAVE)
        bindMode(cpu_config_p1, ModeSwitcher.BALANCE)
        bindMode(cpu_config_p2, ModeSwitcher.PERFORMANCE)
        bindMode(cpu_config_p3, ModeSwitcher.FAST)

        config_customer_powercfg.setOnClickListener {
            chooseLocalConfig()
        }
        config_customer_powercfg_online.setOnClickListener {
            getOnlineConfig()
        }
        checkConfig()
    }

    private fun bindMode(button: View, mode: String) {
        button.setOnClickListener {
            modifyCpuConfig(mode)
        }
    }

    private fun updateState(button: View, mode: String) {
        val authorView = button.findViewWithTag<TextView>("author")
        val replaced = modeSwitcher.modeReplaced(context!!, mode) != null
        authorView.setText("Author : " + (if (replaced) "custom" else author))
        button.alpha = if (configInstalled || replaced) 1f else 0.5f
    }

    override fun onResume() {
        super.onResume()
        activity!!.title = getString(R.string.menu_cpu_modes)

        updateState(cpu_config_p0, ModeSwitcher.POWERSAVE)
        updateState(cpu_config_p1, ModeSwitcher.BALANCE)
        updateState(cpu_config_p2, ModeSwitcher.PERFORMANCE)
        updateState(cpu_config_p3, ModeSwitcher.FAST)
    }

    private fun modifyCpuConfig(mode: String) {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        // transaction.setCustomAnimations(R.animator.fragment_enter, R.animator.fragment_exit)
        val fragment = FragmentCpuControl.newInstance(mode)

        val pageTitle = ModeSwitcher.getModName(mode)
        // transaction.disallowAddToBackStack()
        transaction.replace(R.id.app_more, fragment)
        transaction.addToBackStack(pageTitle);
        transaction.commitAllowingStateLoss()
        activity!!.title = pageTitle
    }

    private val REQUEST_POWERCFG_FILE = 1
    private val REQUEST_POWERCFG_ONLINE = 2
    private val configInstaller = CpuConfigInstaller()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (this.isDetached) {
            return
        }
        if (requestCode == REQUEST_POWERCFG_FILE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.extras == null || !data.extras.containsKey("file")) {
                    return
                }
                val path = data.extras.getString("file")
                val file = File(path)
                if (file.exists()) {
                    if (file.length() > 200 * 1024) {
                        Toast.makeText(context, "这个文件也太大了，配置脚本大小不能超过200KB！", Toast.LENGTH_LONG).show()
                        return
                    }
                    val lines = file.readText(Charset.defaultCharset()).replace("\r", "")
                    val configStar = lines.split("\n").firstOrNull()
                    if (configStar != null && configStar.startsWith("#!/") && configStar.endsWith("sh")) {
                        if (configInstaller.installCustomConfig(context!!, lines, "local file")) {
                            configInstalled()
                        } else {
                            Toast.makeText(context, "由于某些原因，安装配置脚本失败，请重试！", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "这似乎是个无效的脚本文件！", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context!!, "所选的文件没找到！", Toast.LENGTH_LONG).show()
                }
            }
            return
        } else if (requestCode == REQUEST_POWERCFG_ONLINE) {
            if (resultCode == Activity.RESULT_OK) {
                configInstalled()
            }
        }
    }

    //检查配置脚本是否已经安装
    private fun checkConfig() {
        val support = configInstaller.dynamicSupport(context!!)
        if (support) {
            config_cfg_select.visibility = View.VISIBLE
            config_cfg_select_0.setOnClickListener {
                installConfig(false)
            }
            config_cfg_select_1.setOnClickListener {
                installConfig(true)
            }
        }
        when {
            configInstaller.configInstalled() -> {
                //TODO：检查是否更新
            }
            support -> {
                var i = 0
                DialogHelper.animDialog(AlertDialog.Builder(context)
                        .setTitle(getString(R.string.first_start_select_config))
                        .setCancelable(false)
                        .setSingleChoiceItems(
                                arrayOf(
                                        getString(R.string.conservative),
                                        getString(R.string.radicalness),
                                        getString(R.string.get_online_config),
                                        getString(R.string.choose_local_config),
                                        getString(R.string.skipnow)
                                ), 0) { _, which ->
                            i = which
                        }
                        .setNegativeButton(R.string.btn_confirm) { _, _ ->
                            if (i == 4) {
                                // 跳过配置安装时，关闭性能调节
                                globalSPF.edit().putBoolean(SpfConfig.GLOBAL_SPF_DYNAMIC_CONTROL, false).apply()
                            } else if (i == 3) {
                                chooseLocalConfig()
                            } else if (i == 2) {
                                getOnlineConfig()
                            } else if (i == 1) {
                                installConfig(true)
                            } else if (i == 0) {
                                installConfig(false)
                            }
                        })
            }
            else -> {
                DialogHelper.animDialog(AlertDialog.Builder(context)
                        .setTitle(getString(R.string.not_support_config))
                        .setMessage(R.string.not_support_config_desc)
                        .setPositiveButton(getString(R.string.get_online_config)) { _, _ ->
                            getOnlineConfig()
                        }
                        .setNegativeButton(getString(R.string.more)) { _, _ ->
                            val intent = Intent()
                            //Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                            intent.action = "android.intent.action.VIEW"
                            val content_url = Uri.parse("https://github.com/helloklf/vtools")
                            intent.data = content_url
                            startActivity(intent)
                        })
            }
        }
    }

    private fun chooseLocalConfig() {
        try {
            val intent = Intent(this.context, ActivityFileSelector::class.java)
            intent.putExtra("extension", "sh")
            startActivityForResult(intent, REQUEST_POWERCFG_FILE)
        } catch (ex: Exception) {
            Toast.makeText(context!!, "启动内置文件选择器失败！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOnlineConfig() {
        var i = 0
        DialogHelper.animDialog(AlertDialog.Builder(context)
                .setTitle(getString(R.string.config_online_options))
                .setCancelable(true)
                .setSingleChoiceItems(
                        arrayOf(
                                getString(R.string.online_config_v1),
                                getString(R.string.online_config_v2)
                        ), 0) { _, which ->
                    i = which
                }
                .setNegativeButton(R.string.btn_confirm) { _, _ ->
                    if (i == 0) {
                        getOnlineConfigV1()
                    } else if (i == 1) {
                        getOnlineConfigV2()
                    }
                })
    }

    private fun getOnlineConfigV1() {
        try {
            val intent = Intent(this.context, ActivityAddinOnline::class.java)
            intent.putExtra("url", "https://github.com/yc9559/cpufreq-interactive-opt/tree/master/vtools-powercfg")
            startActivityForResult(intent, REQUEST_POWERCFG_ONLINE)
        } catch (ex: Exception) {
            Toast.makeText(context!!, "启动在线页面失败！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOnlineConfigV2() {
        try {
            val intent = Intent(this.context, ActivityAddinOnline::class.java)
            intent.putExtra("url", "https://github.com/yc9559/wipe-v2/releases")
            startActivityForResult(intent, REQUEST_POWERCFG_ONLINE)
        } catch (ex: Exception) {
            Toast.makeText(context!!, "启动在线页面失败！", Toast.LENGTH_SHORT).show()
        }
    }

    //安装调频文件
    private fun installConfig(useBigCore: Boolean) {
        if (context == null) return

        if (!configInstaller.dynamicSupport(context!!)) {
            Snackbar.make(view!!, R.string.not_support_config, Snackbar.LENGTH_LONG).show()
            return
        }

        configInstaller.installOfficialConfig(context!!, "", useBigCore)
        configInstalled()
    }

    private fun configInstalled() {
        if (globalSPF.getBoolean(SpfConfig.GLOBAL_SPF_DYNAMIC_CONTROL, true)) {
            Snackbar.make(view!!, getString(R.string.config_installed), Snackbar.LENGTH_LONG).show()
            reStartService()
        } else {
            DialogHelper.animDialog(AlertDialog.Builder(context)
                    .setMessage("配置脚本已安装，是否开启性能调节？")
                    .setPositiveButton(R.string.btn_confirm) { _, _ ->
                        globalSPF.edit().putBoolean(SpfConfig.GLOBAL_SPF_DYNAMIC_CONTROL, true).apply()
                        reStartService()
                    }
                    .setNegativeButton(R.string.btn_cancel) { _, _ ->
                    })
        }
    }

    /**
     * 重启辅助服务
     */
    private fun reStartService() {
        if (AccessibleServiceHelper().serviceRunning(context!!)) {
            context!!.sendBroadcast(Intent(context!!.getString(R.string.scene_change_action)))
        }
    }

    companion object {
        fun createPage(): Fragment {
            val fragment = FragmentCpuModes()
            return fragment
        }
    }
}
