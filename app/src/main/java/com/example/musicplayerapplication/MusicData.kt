package com.example.musicplayerapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
class MusicData(
    var id: String,
    var title: String?,
    var artist: String?,
    var albumId: String?,
    var duration: Int?,
    var likes: Int?
) :
    Parcelable {

    companion object : Parceler<MusicData> {
        override fun create(parcel: Parcel): MusicData {
            return MusicData(parcel)
        }

        override fun MusicData.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(title)
            parcel.writeString(artist)
            parcel.writeString(albumId)
            parcel.writeInt(duration!!)
            parcel.writeInt(likes!!)
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    )

    fun getMusicUri(): Uri =
        Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, this.id)

    fun getAlbumUri(): Uri = Uri.parse("content://media//external/audio/albumart/${this.albumId}")

    fun getAlbumBitmap(context: Context, albumSize: Int): Bitmap? {
        val contentResolver = context.contentResolver
        val albumUri = getAlbumUri()
        var bitmap: Bitmap? = null
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        try {
            if (albumUri != null) {
                parcelFileDescriptor = contentResolver.openFileDescriptor(albumUri, "r")
                // Get only the dimensions of the bitmap by setting inJustDecodeBounds to true
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFileDescriptor(
                    parcelFileDescriptor?.fileDescriptor,
                    null,
                    options
                )
                options.inSampleSize = calculateInSampleSize(options, albumSize, albumSize)
                options.inDensity = options.outWidth
                options.inTargetDensity = albumSize * options.inSampleSize
                options.inJustDecodeBounds = false
                bitmap = BitmapFactory.decodeFileDescriptor(
                    parcelFileDescriptor?.fileDescriptor,
                    null,
                    options
                )
            }
        } catch (e: java.lang.Exception) {
            Log.e("MusicData", e.toString())
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close()
                }
            } catch (e: Exception) {
                Log.e("MusicData", e.toString())
            }
        }
        return bitmap
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 2
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

}