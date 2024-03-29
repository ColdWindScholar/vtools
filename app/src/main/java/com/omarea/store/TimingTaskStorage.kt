package com.omarea.store

import android.content.Context
import com.omarea.common.shared.ObjectStorage
import com.omarea.model.TimingTaskInfo

class TimingTaskStorage(private val context: Context) : ObjectStorage<TimingTaskInfo>(context) {

    fun save(obj: TimingTaskInfo): Boolean {
        return super.save(obj, obj.taskId)
    }

}
