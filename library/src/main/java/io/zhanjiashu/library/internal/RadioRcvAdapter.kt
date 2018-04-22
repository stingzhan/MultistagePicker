/*
 * Copyright 2018 stingzhan. https://github.com/stingzhan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.zhanjiashu.library.internal

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import io.zhanjiashu.library.R

// 支持单选功能的RecyclerView Adapter
abstract class RadioRcvAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    private val tagKey: Int = R.id.mp_tag_key

    private var selectedItemPosition = -1

    private val internalListener = View.OnClickListener {
        it.getTag(tagKey).safeCast<Int> {
            selectItem(this@safeCast, true)
            onSelectedItemChanged(it, this@safeCast)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vh = onCreateViewHolderWrapper(parent, viewType)
        vh.itemView.setOnClickListener(internalListener)
        return vh
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.setTag(tagKey, position)
        onBindViewHolderWrapper(holder, position, position == selectedItemPosition)
    }

    abstract fun onCreateViewHolderWrapper(parent: ViewGroup, viewType: Int) : VH
    abstract fun onBindViewHolderWrapper(holder: VH, position: Int, selected: Boolean)

    open fun onSelectedItemChanged(itemView: View, position: Int) {

    }

    fun selectItem(position: Int, refresh: Boolean) {
        if (position == selectedItemPosition) {
            return
        }

        val previousItemPosition = selectedItemPosition
        selectedItemPosition = position

        if (refresh) {
            notifyItemChanged(selectedItemPosition)
            if (previousItemPosition >= 0) {
                notifyItemChanged(previousItemPosition)
            }
        }
    }
}