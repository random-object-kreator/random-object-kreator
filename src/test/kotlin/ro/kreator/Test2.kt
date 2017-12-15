package ro.kreator

import com.memoizr.assertk.expect
import com.memoizr.assertk.of
import org.junit.Test
import java.io.File
import java.io.Serializable

class Test2 {
    interface Media : Serializable
    interface Visual : Media
    interface Video : Visual
    interface Audio : Media
    interface Image : Visual
    interface Gif : Image

    data class UriVideo(val uri: String) : Video
    data class FileVideo(val file: File) : Video
    data class UriImage(val uri: String) : Image
    data class UriGif(val uri: String) : Gif
    data class UriAudio(val uri: String) : Audio

    data class SizedVisual<out T : Media>(val media: T, val size: Size)
    data class Clip(val thumbnail: SizedVisual<Image>)

    val aRandomClip by aRandom<Clip>()

    @Test
    fun `media is always an image`() {
        expect that aRandomClip.thumbnail.media isInstance of<Image>()
    }
}