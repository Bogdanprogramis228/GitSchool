import com.example.gitschool.Anime
import com.google.gson.annotations.SerializedName
import com.example.gitschool.Images

data class AnimeDetailResponse(
    @SerializedName("data") val data: AnimeDetail?
)

data class AnimeDetail(
    @SerializedName("mal_id")   val malId: Int,
    @SerializedName("title")    val title: String?,
    @SerializedName("images")   val images: Images?,
    @SerializedName("score")    val score: Double?,
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("year")     val year: Int?,
    @SerializedName("season")   val season: String?,
    @SerializedName("studios")  val studios: List<Producer>?,
    @SerializedName("genres")   val genres: List<Genre>?,
    @SerializedName("type")     val type: String?,
    @SerializedName("episodes") val episodes: Int?,
    @SerializedName("status")   val status: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("trailer")  val trailer: Trailer?,
    @SerializedName("aired")   val aired: Aired?
)

data class Trailer(
    @SerializedName("youtube_id") val youtubeId: String?
)

data class Producer(
    @SerializedName("mal_id") val malId: Int?,
    @SerializedName("type")   val type: String?,
    @SerializedName("name")   val name: String?,
    @SerializedName("url")    val url: String?
)

data class Genre(
    @SerializedName("mal_id") val malId: Int?,
    @SerializedName("type")   val type: String?,
    @SerializedName("name")   val name: String?,
    @SerializedName("url")    val url: String?
)

// В окремому файлі (або там же):
data class Images(
    @SerializedName("jpg")  val jpg: Jpg?,
    @SerializedName("webp") val webp: Webp? = null
)

data class Jpg(
    @SerializedName("image_url")       val imageUrl: String?,
    @SerializedName("small_image_url") val smallImageUrl: String? = null,
    @SerializedName("large_image_url") val largeImageUrl: String? = null
)

data class Webp(
    @SerializedName("image_url") val imageUrl: String?
)

data class Anime(
    @SerializedName("mal_id") val malId: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("images") val images: Images?,
    @SerializedName("score") val score: Double?,
    @SerializedName("episodes") val episodes: Int?,
    @SerializedName("aired") val aired: Aired?,
    @SerializedName("synopsis") val synopsis: String?
)

data class Aired(
    @SerializedName("from") val from: String?,
    @SerializedName("to")   val to: String?
)

data class AnimeRecommendationsResponse(
    @SerializedName("data") val data: List<RecommendationData>?
)

data class RecommendationData(
    @SerializedName("entry") val entry: RecommendationEntry?,
    @SerializedName("votes") val votes: Int?
)

data class RecommendationEntry(
    @SerializedName("mal_id") val malId: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("images") val images: Images?
    // ...
)
fun RecommendationEntry.toAnime(): Anime {
    return Anime(
        malId = this.malId,
        title = this.title,
        images = this.images,
        score = null,
        episodes = null,
        aired = null,
        synopsis = null
    )
}

fun AnimeDetail.toAnime(): Anime {
    return Anime(
        malId = this.malId,
        title = this.title,
        images = this.images,
        score = this.score,
        episodes = this.episodes,
        aired = null,
        synopsis = this.synopsis
    )
}

