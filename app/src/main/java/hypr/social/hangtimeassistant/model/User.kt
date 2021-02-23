package hypr.social.hangtimeassistant.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User (
    // keys
    val ID: String = "",

    // attributes
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",

    // extra
    val image: String = "",
    val profileCompleted: Boolean = false
) : Parcelable