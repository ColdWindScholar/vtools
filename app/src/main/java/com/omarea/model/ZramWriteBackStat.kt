package com.omarea.model

class ZramWriteBackStat {
    var backingDev: String? = null
    // 已写入备份设备 KB
    var backed: Int = 0
    // 历史读取(从备份设备) KB
    var backReads: Int = 0
    // 历史回写(到备份设备) KB
    var backWrites: Int = 0
}