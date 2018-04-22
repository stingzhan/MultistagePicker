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


package io.zhanjiashu.library

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.zhanjiashu.library.internal.PickerInterface
import io.zhanjiashu.library.provider.MultistagePickerDataProvider
import io.zhanjiashu.library.internal.OptionAdapter
import io.zhanjiashu.library.internal.PickerConfigInterface
import io.zhanjiashu.library.internal.scrollToPositionAtTop

class MultistagePicker(context: Context, private val dataProvider: MultistagePickerDataProvider) : PickerInterface, PickerConfigInterface {

    private val pickerView: View = View.inflate(context, R.layout.mp_core_view, null)

    private val titleView: TextView
    private val recyclerView: RecyclerView
    private val tabLayout: TabLayout
    private val okBtn: View

    private val optionAdapter: OptionAdapter

    private var showConfirmButton = true
    private var completedListener: ((selectedOptions: Map<String, String>) -> Unit)? = null

    private var curStagePosition = 0

    private val curStageKey: String
        get() = dataProvider.stageKeys()[curStagePosition]

    private val curStageOptions: List<String>?
        get() = dataProvider.stageData(curStageKey, mSelectedOptions)

    private val isLowestStage: Boolean
        get() = curStagePosition == stageCount - 1

    private val stageCount: Int
        get() = dataProvider.stageKeys().size

    private val mSelectedOptions = mutableMapOf<String, String>()   // 已选值

    init {
        titleView = pickerView.findViewById(R.id.titleView)
        recyclerView = pickerView.findViewById(R.id.rcv)
        tabLayout = pickerView.findViewById(R.id.tab_layout)
        okBtn = pickerView.findViewById(R.id.tv_btn_ok)

        optionAdapter = OptionAdapter()
        optionAdapter.setOnItemViewClickListener { _, _, option ->
            onOptionSelected(option)
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = optionAdapter


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tabLayout.selectedTabPosition
                if (position != curStagePosition) {
                    onStageSelected(position, tab,  position < curStagePosition)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }

        })

        if (showConfirmButton) {
            okBtn.visibility = View.GONE
        } else {
            okBtn.visibility = View.VISIBLE
            okBtn.setOnClickListener { notifyPickCompleted() }
        }

        refreshOptions()
    }

    // 刷新选项列表视图
    private fun refreshOptions() {
        optionAdapter.apply {
            data.clear()
            selectItem(-1, false)
            curStageOptions?.let {
                data.addAll(it)
            }
            notifyDataSetChanged()
        }
    }

    /**
     * 层级切换事件处理。
     * @param upwards true - 从低层级切换到高层级， false - 从高层级切换到低层级
     */
    private fun onStageSelected(stagePosition: Int, tab: TabLayout.Tab, upwards: Boolean) {
        curStagePosition = stagePosition
        refreshOptions()
        val selectedOption = mSelectedOptions[curStageKey]
        tab.text = selectedOption ?: dataProvider.stageTabText(curStageKey)
        if (upwards) {
            resetLowerStages()
            okBtn.isEnabled = false
            selectOption(selectedOption)
        } else {
            recyclerView.scrollToPosition(0)
        }
    }

    // 当前层级选中事件处理
    private fun onOptionSelected(option: String) {
        tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text = option
        mSelectedOptions[curStageKey] = option
        if (isLowestStage) {
            if (showConfirmButton) {
                notifyPickCompleted()
            } else {
                okBtn.isEnabled = true
            }
        } else {
            jumpToNextStage()
        }
    }

    // 跳至下一层级，如果该选择器尚未存在，则创建并渲染；若已存在，则渲染
    private fun jumpToNextStage() {
        val nextTab = getStageTabAt(tabLayout.selectedTabPosition + 1)
        nextTab.select()
    }

    private fun getStageTabAt(stagePosition: Int): TabLayout.Tab {
        val tab: TabLayout.Tab
        if (stagePosition < tabLayout.tabCount) {
            tab = tabLayout.getTabAt(stagePosition)!!
        } else {
            tab = tabLayout.newTab()
            tabLayout.addTab(tab)
        }
        return tab
    }

    // 重置低层级选择器
    private fun resetLowerStages() {
        (curStagePosition + 1 until stageCount).forEach {
            mSelectedOptions.remove(dataProvider.stageKeys()[it])
            tabLayout.getTabAt(it)?.text = dataProvider.stageTabText(dataProvider.stageKeys()[it])
        }
    }

    // 通知 Listener 回调
    private fun notifyPickCompleted() {
        completedListener?.invoke(mSelectedOptions)
    }

    // 判断指定层级的预设值是否有效
    private fun optionInTheTargetStage(stageKey: String, selectedOption: String): Boolean {
        return dataProvider.stageData(stageKey, mSelectedOptions)?.contains(selectedOption) ?: false
    }

    private fun selectOption(option: String?) {
        if (option == null) {
            return
        }
        val position = curStageOptions?.indexOf(option) ?: -1
        if (position >= 0) {
            recyclerView.scrollToPositionAtTop(position)
            optionAdapter.selectItem(position, true)
        }
    }


    override fun setTitle(title: CharSequence?) {
        if (title == null) {
            titleView.visibility = View.GONE
        } else {
            titleView.visibility = View.VISIBLE
            titleView.text = title
        }
    }

    override fun setPreselectedOptions(selectedOptions: Map<String, String>) {
        mSelectedOptions.clear()
        var latestTab: TabLayout.Tab? = null
        (0 until stageCount)
                .asSequence()
                .takeWhile {
                    val key = dataProvider.stageKeys()[it]
                    return@takeWhile selectedOptions.containsKey(key) && optionInTheTargetStage(key, selectedOptions[key]!!)
                }
                .forEach {
                    curStagePosition = it
                    val key = dataProvider.stageKeys()[it]
                    val selectedOption = selectedOptions[key]!!

                    mSelectedOptions[key] = selectedOption

                    latestTab = getStageTabAt(it).apply {
                        text = selectedOption
                    }
                }

        latestTab?.select()

        okBtn.isEnabled = isLowestStage

        refreshOptions()
        selectOption(mSelectedOptions[curStageKey])
    }

    override fun setOnPickCompletedListener(l: (selectedOptions: Map<String, String>) -> Unit) {
        completedListener = l
    }

    override fun showConfirmButton(shown: Boolean) {
        okBtn.visibility = if (shown) View.GONE else View.VISIBLE
        this.showConfirmButton = shown
    }


    /* 对外暴露的方法 */

    /**
     * 返回选择器的View
     */
    fun getView(): View {
        return pickerView
    }
}