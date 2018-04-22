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
import io.zhanjiashu.library.internal.PickerConfigInterface
import io.zhanjiashu.library.internal.PickerDialogInterface
import io.zhanjiashu.library.provider.*


class RegionPicker private constructor(private val picker: MultistagePickerDialog) : PickerDialogInterface by picker, PickerConfigInterface by picker {

    constructor(context: Context) : this(MultistagePickerDialog(context, AddressPickerDataProvider(context)))

    private var addressPickSuccessListener: ((region: Region) -> Unit)? = null

    init {
        setTitle("所在地区")
        picker.setOnPickCompletedListener { selectedOptions ->
            val region = Region (
                    selectedOptions[STAGE_KEY_PROVINCE] ?: "",
                    selectedOptions[STAGE_KEY_CITY] ?: "",
                    selectedOptions[STAGE_KEY_DISTRICT] ?: ""
            )
            addressPickSuccessListener?.invoke(region)
        }
    }

    fun setDefaultRegion(region: Region?) {
        region?.let {
            val options = mutableMapOf(
                    STAGE_KEY_PROVINCE to it.province,
                    STAGE_KEY_CITY to it.city,
                    STAGE_KEY_DISTRICT to it.district
            )
            picker.setPreselectedOptions(options)
        }
    }

    fun setOnAddressPickSuccessListener(l: (region: Region) -> Unit) {
        addressPickSuccessListener = l
    }

    data class Region(val province: String, val city: String, val district: String)
}
