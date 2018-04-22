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


package io.zhanjiashu.library.provider

import android.content.Context
import android.util.Log
import io.zhanjiashu.library.internal.RegionsXMLParserHandler
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory

const val STAGE_KEY_PROVINCE = "Province"
const val STAGE_KEY_CITY = "City"
const val STAGE_KEY_DISTRICT = "District"

class AddressPickerDataProvider(context: Context) : MultistagePickerDataProvider() {

    companion object {
        private val mRegionData: MutableMap<String, MutableMap<String, List<String>>> = mutableMapOf()

        fun parseRegionsXML(context: Context) {
            var inputStream: InputStream? = null
            try {
                inputStream = context.assets.open("regions.xml")
                inputStream?.let {
                    val saxParser = SAXParserFactory.newInstance().newSAXParser()
                    val handler = RegionsXMLParserHandler()
                    saxParser.parse(inputStream, handler)
                    inputStream.close()

                    mRegionData.putAll(handler.data)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    init {
        if (mRegionData.isEmpty()) {
            parseRegionsXML(context)
        }
    }

    private val keys = listOf(STAGE_KEY_PROVINCE, STAGE_KEY_CITY, STAGE_KEY_DISTRICT)

    override fun stageKeys(): List<String> {
        return keys
    }

    override fun stageTabText(stageKey: String): String {
        return when (stageKey) {
            STAGE_KEY_PROVINCE -> "省"

            STAGE_KEY_CITY -> "城市"

            STAGE_KEY_DISTRICT -> "区县"

            else -> "请选择"
        }
    }

    override fun stageData(stageKey: String, upperStageSelectedOptions: Map<String, String>): List<String>? {
        return when (stageKey) {
            STAGE_KEY_PROVINCE -> mRegionData.keys.toList()

            STAGE_KEY_CITY -> {
                val province = upperStageSelectedOptions[STAGE_KEY_PROVINCE]
                mRegionData[province]?.keys?.toList() ?: emptyList()
            }

            STAGE_KEY_DISTRICT -> {
                val province = upperStageSelectedOptions[STAGE_KEY_PROVINCE]
                val city = upperStageSelectedOptions[STAGE_KEY_CITY]

                mRegionData[province]?.get(city)
            }

            else -> emptyList()
        }
    }

}