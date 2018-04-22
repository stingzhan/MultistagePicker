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

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.zhanjiashu.library.R


internal class OptionAdapter constructor(val data: MutableList<String>) : RadioRcvAdapter<OptionVH>() {

    constructor() : this(mutableListOf<String>())

    private lateinit var mContext: Context
    private lateinit var mLayoutInflater: LayoutInflater

    private var mItemViewClickListener: ((view: View, position: Int, option: String) -> Unit)? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mContext = recyclerView.context
        mLayoutInflater = LayoutInflater.from(mContext)

    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolderWrapper(parent: ViewGroup, viewType: Int): OptionVH {
        return OptionVH(mLayoutInflater.inflate(R.layout.mp_option_view, parent, false))
    }

    override fun onBindViewHolderWrapper(holder: OptionVH, position: Int, selected: Boolean) {
        holder.textView.text = data[position]
        holder.textView.tag = position
        if (selected) {
            holder.textView.apply {
                setTextColor(ContextCompat.getColor(context, R.color.mp_red))
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mp_option_selected_icon, 0)
            }
        } else {
            holder.textView.apply {
                setTextColor(ContextCompat.getColor(context, R.color.mp_black))
                setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }
    }

    override fun onSelectedItemChanged(itemView: View, position: Int) {
        mItemViewClickListener?.invoke(itemView, position, data[position])
    }

    fun setOnItemViewClickListener(l: (v: View, position: Int, option: String) -> Unit) {
        mItemViewClickListener = l
    }

}

internal class OptionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.optionText)
}