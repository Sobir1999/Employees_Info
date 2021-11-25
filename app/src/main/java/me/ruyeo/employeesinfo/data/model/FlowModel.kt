package me.ruyeo.employeesinfo.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

/**
 *Created by farrukh_kh on 6/12/21 6:21 PM
 *me.ruyeo.employeesinfo.data.model
 **/

/**
 Xodim keldi ketdisi (api/v1/flow endpoint) uchun data class
 */
data class FlowModel(
    @SerializedName("staff")
    val staffId: Int,
    @SerializedName("came")
    val cameTime: DateTime?,
    @SerializedName("went")
    val wentTime: DateTime?
)
