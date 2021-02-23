package hypr.social.hangtimeassistant.model
import android.graphics.Color
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

//
// Relationship tables
//

@Parcelize
data class Contact2Category(
    val contactID: String = "",
    val categoryID: String = ""
) : Parcelable

@Parcelize
data class Contact2Event(
    val contactID: String = "",
    val eventID: String = ""
) : Parcelable

@Parcelize
data class Event2Category(
    val eventID: String = "",
    val categoryID: String = ""
) : Parcelable