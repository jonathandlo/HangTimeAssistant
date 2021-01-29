package hypr.social.hangtimeassistant

import android.graphics.Color
import androidx.room.*
import java.time.Instant

//
// Daos
//

@Dao
interface ContactDao {
    // Set functions
    @Insert fun insert(pItem: Contact): Long
    @Update fun update(vararg pItem: Contact): Int
    @Delete fun delete(vararg pItem: Contact): Int

    @Query("""
        INSERT INTO contact2category (contactID, categoryID)
        VALUES (:pContactID, :pCategoryID)""")
    fun linkCategory(pContactID: Long, pCategoryID: Long)

    @Query("""
        INSERT INTO contact2event (contactID, eventID)
        VALUES (:pContactID, :pEventID)""")
    fun linkEvent(pContactID: Long, pEventID: Long)

    @Query("""
        DELETE FROM contact2category
        WHERE contactID = :pContactID
        AND categoryID = :pCategoryID""")
    fun unlinkCategory(pContactID: Long, pCategoryID: Long)

    @Query("""
        DELETE FROM contact2event
        WHERE contactID = :pContactID
        AND eventID = :pEventID""")
    fun unlinkEvent(pContactID: Long, pEventID: Long)

    @Query("DELETE FROM contact2category WHERE contactID = :pID")
    fun _delCon2Cat(pID: Long)
    @Query("DELETE FROM contact2event WHERE contactID = :pID")
    fun _delCon2Event(pID: Long)
    @Transaction
    fun deleteAssociations(pContactID: Long){
        _delCon2Cat(pContactID)
        _delCon2Event(pContactID)
    }

    // Get functions
    @Query("SELECT COUNT(*) FROM tbl_contact")
    fun countAll() : Int

    @Query("""
        SELECT COUNT(*) FROM contact2category
        WHERE categoryID = :pCategoryID
        AND contactID = :pContactID""")
    fun countCategories(pContactID: Long, pCategoryID: Long) : Int

    @Query("""
        SELECT COUNT(*) FROM contact2event
        WHERE eventID = :pEventID
        AND contactID = :pContactID""")
    fun countEvents(pContactID: Long, pEventID: Long) : Int

    @Query("SELECT * FROM tbl_contact WHERE rowid = :pRowID")
    fun getRow(pRowID: Long): Contact

    @Query("SELECT * FROM tbl_contact")
    fun getAll(): List<Contact>

    @Query("""
        SELECT tbl_category.* FROM tbl_category
        INNER JOIN contact2category ON tbl_category.ID = contact2category.categoryID
        WHERE contactID = :pContactID""")
    fun loadCategories(pContactID: Long): List<Category>

    @Query("""
        SELECT * FROM tbl_event
        INNER JOIN contact2event ON tbl_event.ID = contact2event.eventID
        WHERE contactID = :pContactID""")
    fun loadEvents(pContactID: Long): List<Event>
}

@Dao
interface EventDao {
    // Set functions
    @Insert fun insert(pItem: Event): Long
    @Update fun update(vararg pItem: Event): Int
    @Delete fun delete(vararg pItem: Event): Int

    @Query("DELETE FROM contact2event WHERE eventID = :pID")
    fun delCon2Eve(pID: Long)
    @Query("DELETE FROM event2category WHERE eventID = :pID")
    fun delEve2Cat(pID: Long)
    @Transaction
    fun deleteAssociations(pEventID: Long){
        delCon2Eve(pEventID)
        delEve2Cat(pEventID)
    }

    // Get functions
    @Query("SELECT COUNT(*) FROM tbl_event")
    fun countAll() : Int

    @Query("SELECT * FROM tbl_event WHERE rowid = :pRowID")
    fun getRow(pRowID: Long): Event

    @Query("SELECT * FROM tbl_event")
    fun getAll(): List<Event>
}

@Dao
interface CategoryDao {
    // Set functions
    @Insert fun insert(pItem: Category): Long
    @Update fun update(vararg pItem: Category): Int
    @Delete fun delete(vararg pItem: Category): Int

    @Query("""
        INSERT INTO contact2category (contactID, categoryID)
        VALUES (:pContactID, :pCategoryID)""")
    fun linkContact(pCategoryID: Long, pContactID: Long)

    @Query("""
        INSERT INTO event2category (eventID, categoryID)
        VALUES (:pEventID, :pCategoryID)""")
    fun linkEvent(pCategoryID: Long, pEventID: Long)

    @Query("""
        DELETE FROM contact2category
        WHERE contactID = :pContactID
        AND categoryID = :pCategoryID""")
    fun unlinkContact(pCategoryID: Long, pContactID: Long)

    @Query("""
        DELETE FROM event2category
        WHERE categoryID = :pCategoryID
        AND eventID = :pEventID""")
    fun unlinkEvent(pCategoryID: Long, pEventID: Long)

    @Query("DELETE FROM contact2category WHERE categoryID = :pID")
    fun delCon2Cat(pID: Long)
    @Query("DELETE FROM event2category WHERE categoryID = :pID")
    fun delEve2Cat(pID: Long)
    @Transaction
    fun deleteAssociations(pCategoryID: Long){
        delCon2Cat(pCategoryID)
        delEve2Cat(pCategoryID)
    }

    // Get functions
    @Query("SELECT COUNT(*) FROM tbl_category")
    fun countAll() : Int

    @Query("SELECT * FROM tbl_category WHERE rowid = :pRowID")
    fun getRow(pRowID: Long): Category

    @Query("SELECT * FROM tbl_category")
    fun getAll(): List<Category>

    @Query("""
        SELECT tbl_contact.* FROM tbl_contact
        INNER JOIN contact2category ON tbl_contact.ID = contact2category.contactID
        WHERE categoryID = :pCategoryID""")
    fun loadContacts(pCategoryID: Long): List<Contact>

    @Query("""
        SELECT * FROM tbl_event
        INNER JOIN event2category ON tbl_event.ID = event2category.eventID
        WHERE categoryID = :pCategoryID""")
    fun loadEvents(pCategoryID: Long): List<Event>
}

//
// Relationship tables
//

@Entity(tableName = "contact2category")
data class Contact2Category(
    @PrimaryKey(autoGenerate = true) var ID: Long,
    @ColumnInfo val contactID: Long,
    @ColumnInfo val categoryID: Long
)

@Entity(tableName = "contact2event")
data class Contact2Event(
    @PrimaryKey(autoGenerate = true) var ID: Long,
    @ColumnInfo val contactID: Long,
    @ColumnInfo val eventID: Long
)

@Entity(tableName = "event2category")
data class Event2Category(
    @PrimaryKey(autoGenerate = true) var ID: Long,
    @ColumnInfo val eventID: Long,
    @ColumnInfo val categoryID: Long
)

//
// Entity tables
//

@Entity(tableName = "tbl_contact")
data class Contact (
    // keys
    @PrimaryKey(autoGenerate = true) var ID: Long = 0,

    // attributes
    @ColumnInfo var name: String = "",
    @ColumnInfo var IGUrl: String = "",
    @ColumnInfo var FBUrl: String = "",
    @ColumnInfo var phoneNum: String = "",
    @ColumnInfo var address: String = "",

    // reminders
    @ColumnInfo var reminder: Boolean = false,
    @ColumnInfo var reminderStartDate: Long = 0,
    @ColumnInfo var reminderCadence: Long = 7,
    @ColumnInfo var reminderCadenceUnit: String = "days",
    @ColumnInfo var reminderDelay: Boolean = false,
    @ColumnInfo var reminderDelayAmount: Long = 0,
    @ColumnInfo var reminderDelayUnit: String = "days",

    // meta data
    @Ignore var metaNextReminder: Instant = Instant.EPOCH
)

@Entity(tableName = "tbl_event")
data class Event (
    // keys
    @PrimaryKey(autoGenerate = true) var ID: Long = 0,

    // attributes
    @ColumnInfo var date: Long = 0,
    @ColumnInfo var name: String = "",
    @ColumnInfo var description: String = "",
    @ColumnInfo var address: String = ""
)

@Entity(tableName = "tbl_category")
data class Category (
    // keys
    @PrimaryKey(autoGenerate = true) var ID: Long = 0,

    // attributes
    @ColumnInfo var color: Int = Color.argb(255, 200, 200, 200),
    @ColumnInfo var name: String = ""
)