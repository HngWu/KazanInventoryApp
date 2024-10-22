package com.example.kazaninventoryapp.Models
import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable


@Serializable
data class Asset
(
    val ID: Int,
    val AssetSN: String,
    val AssetName: String,
    val DepartmentID: Int,
    val EmployeeID: Int,
    val AssetGroupID: Int,
    val Description: String,
    val WarrantyDate: String,
    val DepartmentName : String,
    val AssetGroupName : String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
    ID = parcel.readInt(),
    AssetSN = parcel.readString() ?: "",
    AssetName = parcel.readString() ?: "",
    DepartmentID = parcel.readInt(),
    EmployeeID = parcel.readInt(),
    AssetGroupID = parcel.readInt(),
    Description = parcel.readString() ?: "",
    WarrantyDate = parcel.readString() ?: "",
    DepartmentName = parcel.readString() ?: "",
    AssetGroupName = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ID)
        parcel.writeString(AssetSN)
        parcel.writeString(AssetName)
        parcel.writeInt(DepartmentID)
        parcel.writeInt(EmployeeID)
        parcel.writeInt(AssetGroupID)
        parcel.writeString(Description)
        parcel.writeString(WarrantyDate)
        parcel.writeString(DepartmentName)
        parcel.writeString(AssetGroupName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Asset> {
        override fun createFromParcel(parcel: Parcel): Asset {
            return Asset(parcel)
        }

        override fun newArray(size: Int): Array<Asset?> {
            return arrayOfNulls(size)
        }
    }
}