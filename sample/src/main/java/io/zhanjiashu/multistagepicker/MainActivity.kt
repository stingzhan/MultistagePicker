package io.zhanjiashu.multistagepicker

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.zhanjiashu.library.MultistagePicker
import io.zhanjiashu.library.MultistagePickerDialog
import io.zhanjiashu.library.RegionPicker
import io.zhanjiashu.library.provider.MultistagePickerDataProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val options = mapOf(
                "业务相关" to listOf("业务员", "区域经理", "业务经理", "业务总监"),
                "销售相关" to listOf("销售员", "柜长", "销售经理", "展厅经理", "销售总监")
        )

        val dataProvider = object : MultistagePickerDataProvider() {
            private val keys = listOf("Key1", "Key2")

            override fun stageKeys(): List<String> {
                return keys
            }

            override fun stageData(stageKey: String, upperStageSelectedOptions: Map<String, String>): List<String>? {
                return when (stageKey) {
                    "Key1" -> options.keys.toList()
                    "Key2" -> {
                        val key = upperStageSelectedOptions["Key1"]
                        options[key]
                    }
                    else -> null
                }
            }
        }
        val picker = MultistagePicker(this, dataProvider)
        picker.setOnPickCompletedListener { selectedOptions ->
            Log.i("MultiPicker", selectedOptions.toString())
            firstTextView.text = "结果： $selectedOptions"
        }

        val simplePicker = MultistagePickerDialog(this, dataProvider)
        simplePicker.setOnPickCompletedListener { selectedOptions ->
            secondTextView.text = "结果： $selectedOptions"
        }

        firstBtn.setOnClickListener {
            if (picker.getView().parent == null) {
                containerLayout.addView(picker.getView())
                containerLayout.setBackgroundResource(R.drawable.border)
            }
        }

        secondBtn.setOnClickListener {
            simplePicker.show()
        }

        val regionPicker = RegionPicker(this)
        regionPicker.setOnAddressPickSuccessListener { region ->
            thirdTextView.text = "结果： ${region.province} - ${region.city} - ${region.district}"
            thirdBtn.tag = region
        }
        thirdBtn.setOnClickListener {
            thirdBtn.tag.safeCast<RegionPicker.Region> {
                regionPicker.setDefaultRegion(this@safeCast)
            }
            regionPicker.show()
        }
    }
}

inline fun <reified T> Any?.safeCast(action: T.() -> Unit) {
    if (this is T) {
        this.action()
    }
}
