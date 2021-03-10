package hypr.social.hangtimeassistant

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import hypr.social.hangtimeassistant.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

object FirebaseMigration {
    fun migrationToFirebase(context:Context){
        val db = HangTimeDB.getDatabase(context)
        var stop = false

        AlertDialog.Builder(context)
            .setTitle("Warning")
            .setMessage("Delete cloud data?")
            .setPositiveButton("Yes, delete") { _: DialogInterface, _: Int ->
                Toast.makeText(context, "Deleting cloud data...", Toast.LENGTH_SHORT).show()

                CoroutineScope(IO).launch {
                    HTAFirestore.getAllContacts().forEach {
                        HTAFirestore.unlink(it)
                        HTAFirestore.delete(it)
                    }
                    HTAFirestore.getAllCategories().forEach {
                        HTAFirestore.unlink(it)
                        HTAFirestore.delete(it)
                    }
                    HTAFirestore.getAllEvents().forEach {
                        HTAFirestore.unlink(it)
                        HTAFirestore.delete(it)
                    }

                    migrate(context, db)
                }
            }
            .setNeutralButton("Migrate without deleting") { _: DialogInterface, _: Int ->
                CoroutineScope(IO).launch {
                    migrate(context, db)
                }
            }
            .setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }
            .show()
        }
    }

    private suspend fun migrate(context: Context, db: HangTimeDB) {
        withContext(Main) {
            Toast.makeText(context, "Migration starting...", Toast.LENGTH_SHORT).show()
        }

        // track mapping between old and new IDs
        val contactIdMap =  mutableMapOf<Long, String>()
        val categoryIdMap =  mutableMapOf<Long, String>()
        val eventIdMap =  mutableMapOf<Long, String>()

        // migrate core data
        db.contactDao().getAll().forEach {
            val newID = HTAFirestore.add(
                Contact(
                    it.ID.toString(),
                    it.name,
                    it.IGUrl,
                    it.FBUrl,
                    it.phoneNum,
                    it.address,
                    it.reminder,
                    it.reminderStartDate,
                    it.reminderCadence,
                    it.reminderCadenceUnit,
                    it.reminderDelay,
                    it.reminderDelayAmount,
                    it.reminderDelayUnit
                )
            ).ID

            contactIdMap[it.ID] = newID
        }

        db.categoryDao().getAll().forEach {
            val newID = HTAFirestore.add(
                Category(
                    it.ID.toString(),
                    it.color,
                    it.name
                )
            ).ID

            categoryIdMap[it.ID] = newID
        }

        db.eventDao().getAll().forEach {
            val newID = HTAFirestore.add(
                Event(
                    it.ID.toString(),
                    it.ID,
                    it.name,
                    it.description,
                    it.address
                )
            ).ID

            eventIdMap[it.ID] = newID
        }

        // migrate associations
        db.contactDao().getAllContact2Category().forEach {
            HTAFirestore.link(
                Contact(contactIdMap[it.contactID]!!),
                Category(categoryIdMap[it.categoryID]!!)
            )
        }
        db.contactDao().getAllContact2Event().forEach {
            HTAFirestore.link(
                Contact(contactIdMap[it.contactID]!!),
                Event(eventIdMap[it.eventID]!!)
            )
        }
        db.contactDao().getAllEvent2Category().forEach {
            HTAFirestore.link(
                Event(eventIdMap[it.eventID]!!),
                Category(categoryIdMap[it.categoryID]!!)
            )
        }

        withContext(Main) { Toast.makeText(context, "Migration complete!", Toast.LENGTH_LONG).show() }
}