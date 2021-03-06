package com.example.stayahead

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.NotNull

@Parcelize
data class Checkpoint(var checkpointName: String, var date: String,var time: String, var isCompleted: Boolean, var goalId: Int = 0, var checkpointId: Int = 0): Parcelable{

}
