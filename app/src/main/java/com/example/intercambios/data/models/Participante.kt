package com.example.intercambios.data.models
import android.os.Parcel
import android.os.Parcelable

data class Participante(
    val uid: String = "",
    val temaRegalo: String = "",
    val asignadoA: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(temaRegalo)
        parcel.writeString(asignadoA)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Participante> {
            override fun createFromParcel(parcel: Parcel): Participante {
                return Participante(parcel)
            }

            override fun newArray(size: Int): Array<Participante?> {
                return arrayOfNulls(size)
            }
        }
    }
}
