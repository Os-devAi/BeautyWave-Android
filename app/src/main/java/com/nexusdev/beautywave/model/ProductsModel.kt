package com.nexusdev.beautywave.model

import android.os.Parcel
import android.os.Parcelable

data class ProductsModel(
    var id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = 0.0,
    val category: String? = null,
    val status: String? = null,
    val backOn: String? = null,
    val imgUrl: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeValue(price)
        parcel.writeString(category)
        parcel.writeString(status)
        parcel.writeString(backOn)
        parcel.writeString(imgUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductsModel> {
        override fun createFromParcel(parcel: Parcel): ProductsModel {
            return ProductsModel(parcel)
        }

        override fun newArray(size: Int): Array<ProductsModel?> {
            return arrayOfNulls(size)
        }
    }
}
