package com.omarea.store

import android.content.Context
import com.omarea.common.shared.ObjectStorage
import com.omarea.model.TriggerInfo

class TriggerStorage(private val context: Context) : ObjectStorage<TriggerInfo>(context) {

    fun save(obj: TriggerInfo): Boolean {
        return super.save(obj, obj.id)
    }

}
