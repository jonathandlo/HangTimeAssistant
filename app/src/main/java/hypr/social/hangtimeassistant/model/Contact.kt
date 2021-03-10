package hypr.social.hangtimeassistant.model
import android.os.Parcel
import android.os.Parcelable
import androidx.room.PrimaryKey
import com.google.firebase.encoders.annotations.Encodable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.JsonAdapter
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.time.Instant

@Parcelize
@IgnoreExtraProperties
open class Contact (
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
    var reminderDelayUnit: String = "days"
) : Parcelable