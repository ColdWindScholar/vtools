package com.omarea.utils


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.omarea.common.ui.DialogHelper
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


class Update {
    private fun currentVersionCode(context: Context): Int {
        val manager = context.packageManager
        var code = 0
        try {
            val info = manager.getPackageInfo(context.packageName, 0)
            code = info.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return code
    }

    fun checkUpdate(context: Context) {
        val handler = Handler(Looper.getMainLooper())
        Thread(Runnable {
            //http://47.106.224.127/
            try {
                val url = URL("https://vtools.oss-cn-beijing.aliyuncs.com/vi/Scene5.json")
                val connection = url.openConnection()
                // 设置连接方式：get
                // connection.setRequestMethod("GET");
                // 设置连接主机服务器的超时时间：15000毫秒
                connection.connectTimeout = 15000
                // 设置读取远程返回的数据时间：60000毫秒
                connection.readTimeout = 60000
                val bufferedReader = BufferedReader(InputStreamReader(connection.getInputStream()))
                val stringBuilder = StringBuilder()
                while (true) {
                    val line = bufferedReader.readLine()
                    if (line != null) {
                        stringBuilder.append(line)
                        stringBuilder.append("\n")
                    } else {
                        break
                    }
                }
                val jsonObject = JSONObject(stringBuilder.toString().trim { it <= ' ' })

                if (jsonObject.has("versionCode")) {
                    val currentVersion = currentVersionCode(context)
                    if (currentVersion < jsonObject.getInt("versionCode")) {
                        handler.post {
                            try {
                                update(context, jsonObject)
                            } catch (ex: java.lang.Exception) {

                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                /*
                handler.post {
                    Toast.makeText(context, "检查更新失败！\n" + ex.message, Toast.LENGTH_SHORT).show()
                }
                */
            }
        }).start()
    }

    private fun update(context: Context, jsonObject: JSONObject) {
        DialogHelper.confirm(context,
                "下载新版本" + jsonObject.getString("versionName") + " ？",
                "更新内容：" + "\n\n" + jsonObject.getString("message"),
                {
                    var downloadUrl = "http://vtools.oss-cn-beijing.aliyuncs.com/app-release${jsonObject.getInt("versionCode")}.apk"// "http://47.106.224.127/publish/app-release.apk"
                    if (jsonObject.has("downloadUrl")) {
                        downloadUrl = jsonObject.getString("downloadUrl")
                    }
                    try {
                        val intent = Intent()
                        intent.setAction(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(downloadUrl)
                        context.startActivity(intent)
                    } catch (ex: java.lang.Exception) {
                        Toast.makeText(context, "启动下载失败！", Toast.LENGTH_SHORT).show()
                    }
                    /*
                    //创建下载任务,downloadUrl就是下载链接
                    val request = DownloadManager.Request(Uri.parse(downloadUrl));
                    //指定下载路径和下载文件名
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Scene_" + jsonObject.getString("versionName") + ".apk");
                    //在通知栏显示下载进度
                    request.allowScanningByMediaScanner();
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    //获取下载管理器
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    //将下载任务加入下载队列，否则不会进行下载
                    val taskId = downloadManager.enqueue(request)

                    val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                    context.registerReceiver(object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            val id = intent!!.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                            if (id == taskId) {
                                val path = getRealFilePath(context!!, downloadManager.getUriForDownloadedFile(taskId))
                                Toast.makeText(context, "下载完成，请手动点击下载通知安装更新！", Toast.LENGTH_LONG).show()
                            }
                        }
                    }, intentFilter)
                    */
                })
                .setCancelable(false)
    }


}
