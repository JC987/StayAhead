package com.example.stayahead

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Checkpoint(var checkpointName: String, var date: String,var time: String, var isCompleted: Boolean, var goalId: Int = 0, var checkpointId: Int = 0): Parcelable{

}
