package hypr.social.hangtimeassistant.model
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.time.Instant

@Parcelize
data class Contact (
    // keys
    var ID: String = "",

    // attributes
    var name: String = "",
    var IGUrl: String = "",
    var FBUrl: String = "",
    var phoneNum: String = "",
    var address: String = "",

    // reminders
    var reminder: Boolean = false,
    var reminderStartDate: Long = 0,
    var reminderCadence: Long = 7,
    var reminderCadenceUnit: String = "days",
    var reminderDelay: Boolean = false,
    var reminderDelayAmount: Long = 0,
    var reminderDelayUnit: String = "days",

    // meta data
    @Ignore var metaNextReminder: Instant = Instant.EPOCH
) : Parcelable