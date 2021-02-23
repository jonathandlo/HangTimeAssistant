package hypr.social.hangtimeassistant

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.ColorSpace
import android.widget.Toast
import hypr.social.hangtimeassistant.model.Category
import hypr.social.hangtimeassistant.model.Contact
import hypr.social.hangtimeassistant.model.Event
import hypr.social.hangtimeassistant.model.HTAFirestore

object FirebaseMigration {
    fun migrationToFirebase(context:Context){
        val db = HangTimeDB.getDatabase(context)
        var stop = false

        AlertDialog.Builder(context)
            .setTitle("Warning")
            .setMessage("This will delete your cloud data. Proceed?")
            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                Toast.makeText(context, "Migration starting", Toast.LENGTH_SHORT).show()
                // TODO: 2/23/2021 implement deleting data

                // migrate core data
                db.contactDao().getAll().forEach {
                    HTAFirestore.add(
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
                            it.reminderDelayAmount
                        )
                    )
                }

                db.categoryDao().getAll().forEach {
                    HTAFirestore.add(
                        Category(
                            it.ID.toString(),
                            it.color,
                            it.name
                        )
                    )
                }

                db.eventDao().getAll().forEach {
                    HTAFirestore.add(
                        Event(
                            it.ID.toString(),
                            it.ID,
                            it.name,
                            it.description,
                            it.address)
                    )
                }

                // migrate associations
                db.contactDao().getAllContact2Category().forEach {
                    HTAFirestore.link(
                        Contact(it.contactID.toString()),
                        Category(it.categoryID.toString())
                    )
                }
                db.contactDao().getAllContact2Event().forEach {
                    HTAFirestore.link(
                        Contact(it.contactID.toString()),
                        Event(it.eventID.toString())
                    )
                }
                db.contactDao().getAllEvent2Category().forEach {
                    HTAFirestore.link(
                        Event(it.eventID.toString()),
                        Category(it.categoryID.toString())
                    )
                }

                Toast.makeText(context, "Migration complete!", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }
            .show()


    }
}