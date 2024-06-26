package com.omarea.store

import android.content.Context
import com.omarea.common.shared.ObjectStorage
import com.omarea.model.TriggerInfo

class TriggerStorage(private val context: Context) : ObjectStorage<TriggerInfo>(context) {
    override fun load(configFile: String): TriggerInfo? {
        return super.load(configFile)
    }

    fun save(obj: TriggerInfo): Boolean {
        return super.save(obj, obj.id)
    }

    override fun remove(configFile: String) {
        super.remove(configFile)
    }
}
