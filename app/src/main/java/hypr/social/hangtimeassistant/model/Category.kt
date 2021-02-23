package hypr.social.hangtimeassistant.model
import android.graphics.Color
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category (
    // keys
    val ID: String = "",

    // attributes
    @ColumnInfo var color: Int = Color.argb(255, 200, 200, 200),
    @ColumnInfo var name: String = ""
) : Parcelable
