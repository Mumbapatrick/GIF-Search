package com.giphy.giphysearchapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class GiphyResponse(
    val data: List<GifItem> = emptyList(),
    val pagination: Pagination? = null,
    val meta: Meta? = null
) : Parcelable

@Parcelize
@Serializable
data class GifItem(
    val id: String? = null,
    val title: String? = null,
    val username: String? = null,
    val rating: String? = null,
    val images: Images? = null
) : Parcelable {
    override fun hashCode(): Int {
        // Safe hash for Compose AnimatedContent
        return id?.hashCode() ?: 0
    }
}

@Parcelize
@Serializable
data class Images(
    val preview: ImageInfo? = null,
    val original: ImageInfo? = null
) : Parcelable {
    override fun hashCode(): Int {
        // Safe hash for Compose AnimatedContent
        return original?.hashCode() ?: 0
    }
}

@Parcelize
@Serializable
data class ImageInfo(
    val url: String? = null,
    val width: String? = null,
    val height: String? = null
) : Parcelable {
    override fun hashCode(): Int {
        // Safe hash for Compose AnimatedContent
        return url?.hashCode() ?: 0
    }
}

@Parcelize
@Serializable
data class Pagination(
    @SerialName("total_count") val totalCount: Int? = null,
    val count: Int? = null,
    val offset: Int? = null
) : Parcelable

@Parcelize
@Serializable
data class Meta(
    val status: Int? = null,
    val msg: String? = null
) : Parcelable
