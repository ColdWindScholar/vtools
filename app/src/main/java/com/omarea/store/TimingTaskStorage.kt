package com.omarea.store

import android.content.Context
import com.omarea.common.shared.ObjectStorage
import com.omarea.model.TimingTaskInfo

class TimingTaskStorage(private val context: Context) : ObjectStorage<TimingTaskInfo>(context) {
    override fun load(configFile: String): TimingTaskInfo? {
        return super.load(configFile)
    }

    fun save(obj: TimingTaskInfo): Boolean {
        return super.save(obj, obj.taskId)
    }

    override fun remove(configFile: String) {
        super.remove(configFile)
    }
}
