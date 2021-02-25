package hypr.social.hangtimeassistant.model
import android.graphics.Color
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Event (
    // keys
    var ID: String = "",

    // attributes
    var date: Long = 0,
    var name: String = "",
    var description: String = "",
    var address: String = ""
) : Parcelable