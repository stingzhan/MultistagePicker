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
import android.support.design.widget.BottomSheetDialog
import android.view.View
import io.zhanjiashu.library.internal.PickerDialogInterface
import io.zhanjiashu.library.internal.PickerInterface
import io.zhanjiashu.library.provider.MultistagePickerDataProvider


// Note: 此时利用了 Kotlin 的类委托特性 来实现 装饰器模式，或者说代理模式
open class MultistagePickerDialog private constructor(context: Context, private val picker: MultistagePicker) : PickerDialogInterface, PickerInterface by picker {

    constructor(context: Context, dataProvider: MultistagePickerDataProvider) : this(context, MultistagePicker(context, dataProvider))

    private val dialog = BottomSheetDialog(context)

    init {
        picker.showConfirmButton(true)

        dialog.setContentView(picker.getView())
        dialog.apply {
            // 屏蔽BottomDialog的下滑隐藏
            setCancelable(false)
            // 点击外部区域隐藏选择器
            window.findViewById<View>(android.support.design.R.id.touch_outside).setOnClickListener {
                if (dialog.isShowing) {
                    this@MultistagePickerDialog.hide()
                }
            }

            // 限制选择器的视图高度为 屏幕高度 的一半
            val container = window.findViewById<View>(android.support.design.R.id.design_bottom_sheet)
            val lp = container.layoutParams
            lp.height = (context.resources.displayMetrics.heightPixels / 2.0F).toInt()
            container.layoutParams = lp
        }
    }

    override fun setOnPickCompletedListener(l: (selectedOptions: Map<String, String>) -> Unit) {
        picker.setOnPickCompletedListener {
            l.invoke(it)
            hide()
        }
    }

    override fun show() {
        dialog.show()
    }

    override fun hide() {
        picker.getView().postDelayed( { dialog.dismiss() }, 200)
    }
}