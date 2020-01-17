package com.omarea.vtools.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.omarea.common.shared.MagiskExtend
import com.omarea.common.shell.KeepShellPublic
import com.omarea.common.shell.KernelProrp
import com.omarea.common.shell.RootFile
import com.omarea.common.ui.DialogHelper
import com.omarea.common.ui.ThemeMode
import com.omarea.permissions.CheckRootStatus
import com.omarea.scene_mode.CpuConfigInstaller
import com.omarea.store.SpfConfig
import com.omarea.ui.TabIconHelper
import com.omarea.utils.Update
import com.omarea.vtools.R
import com.omarea.vtools.dialogs.DialogPower
import com.omarea.vtools.fragments.FragmentDonate
import com.omarea.vtools.fragments.FragmentHome
import com.omarea.vtools.fragments.FragmentNav
import com.omarea.vtools.fragments.FragmentNotRoot
import com.omarea.vtools.popup.FloatMonitor
import com.omarea.vtools.services.BatteryService
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class ActivityMain : AppCompatActivity() {
    private var globalSPF: SharedPreferences? = null
    private lateinit var themeMode: ThemeMode

    private fun setExcludeFromRecents(exclude: Boolean? = null) {
        try {
            val service = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (task in service.appTasks) {
                if (task.taskInfo.id == this.taskId) {
                    val b = exclude
                            ?: globalSPF!!.getBoolean(SpfConfig.GLOBAL_SPF_AUTO_REMOVE_RECENT, false)
                    task.setExcludeFromRecents(b)
                }
            }
        } catch (ex: Exception) {
        }
    }

    private class ServiceCreateThread(context: Context) : Runnable {
        private var context: WeakReference<Context> = WeakReference(context)
        override fun run() {
            //判断是否开启了充电加速和充电保护，如果开启了，自动启动后台服务
            val chargeConfig = context.get()!!.getSharedPreferences(SpfConfig.CHARGE_SPF, Context.MODE_PRIVATE)
            if (chargeConfig.getBoolean(SpfConfig.CHARGE_SPF_QC_BOOSTER, false) || chargeConfig!!.getBoolean(SpfConfig.CHARGE_SPF_BP, false)) {
                BatteryService.startBatteryService(context.get()!!)
            }
        }

    }

    private class ConfigInstallerThread : Thread() {
        override fun run() {
            super.run()
            CpuConfigInstaller().configCodeVerify()
        }
    }

    private class ThermalCheckThread(private var context: Context) : Thread() {
        override fun run() {
            super.run()

            if (
                    MagiskExtend.magiskSupported() &&
                    KernelProrp.getProp("${MagiskExtend.MAGISK_PATH}system/vendor/etc/thermal-engine.current.ini") != ""
            ) {
                DialogHelper.animDialog(AlertDialog.Builder(context)
                        .setTitle("注意事项")
                        .setMessage("附加功 - MIUI专属 - 温控模式切换，现已全面升级。因为此前设计的模式文件，已经无法很好的兼容最新系统，建议尽快切换到新的配置模式！")
                        .setCancelable(true)
                        .setPositiveButton(R.string.btn_iknow) { _, _ ->
                        })
            }

            if (
                    MagiskExtend.magiskSupported() &&
                    KernelProrp.getProp("${MagiskExtend.MAGISK_PATH}system/vendor/etc/thermal.current.ini") != ""
            ) {
                when {
                    RootFile.list("/data/thermal/config").size > 0 -> KeepShellPublic.doCmdSync(
                            "chattr -R -i /data/thermal 2> /dev/null\n" +
                                    "rm -rf /data/thermal 2> /dev/null\n")
                    RootFile.list("/data/vendor/thermal/config").size > 0 -> KeepShellPublic.doCmdSync(
                            "chattr -R -i /data/vendor/thermal 2> /dev/null\n" +
                                    "rm -rf /data/vendor/thermal 2> /dev/null\n")
                    else -> return
                }
                DialogHelper.helpInfo(
                        context,
                        "",
                        "检测到系统自动创建了温控副本，这会导致在附加功能中切换的温控失效。\n\nScene已自动将副本删除，但可能需要重启手机才能解决问题")
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .detectAll()
                .build());
        */

        if (globalSPF == null) {
            globalSPF = getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)
        }

        themeMode = ThemeSwitch.switchTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 显示返回按钮
        toolbar.setNavigationOnClickListener { _ ->
            // finish()
            this.onBackPressed()
        }

        val tabIconHelper = TabIconHelper(configlist_tabhost, this, R.layout.list_item_tab2)
        configlist_tabhost.setup()

        tabIconHelper.newTabSpec(getString(R.string.app_nav), getDrawable(R.drawable.app_more)!!, R.id.app_more)
        tabIconHelper.newTabSpec(getString(R.string.app_home), getDrawable(R.drawable.app_home)!!, R.id.tab_home)
        tabIconHelper.newTabSpec(getString(R.string.app_donate), getDrawable(R.drawable.app_donate)!!, R.id.app_donate)
        configlist_tabhost.setOnTabChangedListener { tabId ->
            tabIconHelper.updateHighlight()

            updateBackArrow()
        }
        configlist_tabhost.currentTab = 1
        supportFragmentManager.addOnBackStackChangedListener {
            updateBackArrow()
        }

        if (CheckRootStatus.lastCheckResult) {
            setHomePage()
        }
        setDonatePage()
        setNavPage()

        if (CheckRootStatus.lastCheckResult) {
            try {

                if (MagiskExtend.magiskSupported() &&
                        !(MagiskExtend.moduleInstalled() || globalSPF!!.getBoolean("magisk_dot_show", false))
                ) {
                    DialogHelper.animDialog(
                            AlertDialog.Builder(this)
                                    .setTitle(getString(R.string.magisk_install_title))
                                    .setMessage(getString(R.string.magisk_install_desc))
                                    .setPositiveButton(R.string.btn_confirm) { _, _ ->
                                        MagiskExtend.magiskModuleInstall(this)
                                    }.setNegativeButton(R.string.btn_cancel) { _, _ ->
                                    }.setNeutralButton(R.string.btn_dontshow) { _, _ ->
                                        globalSPF!!.edit().putBoolean("magisk_dot_show", true).apply()
                                    })
                }
            } catch (ex: Exception) {
                DialogHelper.animDialog(AlertDialog.Builder(this).setTitle(getString(R.string.sorry))
                        .setMessage("启动应用失败\n" + ex.message).setNegativeButton(getString(R.string.btn_retry)) { _, _ ->
                            recreate()
                        })
            }
            ConfigInstallerThread().start()
            ServiceCreateThread(this).run()
            ThermalCheckThread(this).run()
        } else {
            try {
                setNotRootPage()
            } catch (ex: java.lang.Exception) {
            }
        }

    }

    private fun updateBackArrow() {
        val isDetailPage = (configlist_tabhost.currentTab == 0) && supportFragmentManager.backStackEntryCount > 0
        supportActionBar!!.setHomeButtonEnabled(isDetailPage)
        supportActionBar!!.setDisplayHomeAsUpEnabled(isDetailPage)

        configlist_tabhost.tabWidget.visibility = if (isDetailPage) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()

        // 如果距离上次检查更新超过 24 小时
        if (globalSPF!!.getLong(SpfConfig.GLOBAL_SPF_LAST_UPDATE, 0) + (3600 * 24 * 1000) < System.currentTimeMillis()) {
            Update().checkUpdate(this)
            globalSPF!!.edit().putLong(SpfConfig.GLOBAL_SPF_LAST_UPDATE, System.currentTimeMillis()).apply()
        }
    }

    private fun setHomePage() {
        val fragmentManager = supportFragmentManager
        fragmentManager.fragments.clear()
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.tab_home, FragmentHome())
        // transaction.addToBackStack(getString(R.string.app_name))
        transaction.commitAllowingStateLoss()
    }

    private fun setNavPage() {
        val fragmentManager = supportFragmentManager

        fragmentManager.fragments.clear()
        val transaction2 = fragmentManager.beginTransaction()
        transaction2.replace(R.id.app_more, FragmentNav.createPage(themeMode))
        // transaction.addToBackStack(getString(R.string.app_name))
        transaction2.commitAllowingStateLoss()
    }

    private fun setDonatePage() {
        val fragmentManager = supportFragmentManager

        fragmentManager.fragments.clear()
        val transaction2 = fragmentManager.beginTransaction()
        transaction2.replace(R.id.app_donate, FragmentDonate.createPage())
        // transaction.addToBackStack(getString(R.string.app_name))
        transaction2.commitAllowingStateLoss()
    }

    private fun setNotRootPage() {
        val fragmentManager = supportFragmentManager
        fragmentManager.fragments.clear()
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.tab_home, FragmentNotRoot())
        transaction.commitAllowingStateLoss()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    }

    //返回键事件
    override fun onBackPressed() {
        try {
            when {
                supportFragmentManager.backStackEntryCount > 0 -> {
                    supportFragmentManager.popBackStack()
                }
                configlist_tabhost.currentTab != 1 -> {
                    configlist_tabhost.currentTab = 1
                    setNavPage()
                    title = getString(R.string.app_name)
                }
                else -> {
                    setExcludeFromRecents(true)
                    super.onBackPressed()
                    this.finishActivity(0)
                }
            }
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    //右上角菜单
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> startActivity(Intent(this.applicationContext, ActivityOtherSettings::class.java))
            R.id.action_power -> DialogPower(this).showPowerMenu()
            R.id.action_graph -> {
                if (!CheckRootStatus.lastCheckResult) {
                    Toast.makeText(this, "没有获得ROOT权限，不能使用本功能", Toast.LENGTH_SHORT).show()
                    return false
                }
                if (FloatMonitor.isShown == true) {
                    FloatMonitor(this).hidePopupWindow()
                    return false
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        showFloatMonitor()
                    } else {
                        //若没有权限，提示获取
                        //val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        //startActivity(intent);
                        val intent = Intent()
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                        intent.data = Uri.fromParts("package", this.packageName, null)
                        Toast.makeText(applicationContext, getString(R.string.permission_float), Toast.LENGTH_LONG).show()
                    }
                } else {
                    showFloatMonitor()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFloatMonitor() {
        DialogHelper.animDialog(AlertDialog.Builder(this)
                .setMessage(getString(R.string.float_monitor_tips))
                .setPositiveButton(R.string.btn_confirm) { _, _ ->
                    FloatMonitor(this).showPopupWindow()
                }
                .setNegativeButton(R.string.btn_cancel) { _, _ ->
                })
    }

    public override fun onPause() {
        super.onPause()
        if (!CheckRootStatus.lastCheckResult) {
            finish()
        }
    }

    override fun onDestroy() {
        val fragmentManager = supportFragmentManager
        fragmentManager.fragments.clear()
        super.onDestroy()
    }
}
