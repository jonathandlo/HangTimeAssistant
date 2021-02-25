package hypr.social.hangtimeassistant.model

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import hypr.social.hangtimeassistant.utils.Constants
import kotlinx.coroutines.tasks.await

object HTAFirestore {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var userDoc : DocumentReference

    fun registerUser(userInfo: User): Task<Void> {
        return db.collection(Constants.USERS)
            .document(userInfo.ID)
            .set(userInfo, SetOptions.merge())
    }
    fun getCurrentUserDetails(): Task<DocumentSnapshot>{
        return db.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
    }
    private fun getCurrentUserID(): String {
        FirebaseAuth.getInstance().currentUser?.let {
            userDoc = db.collection(Constants.USERS).document(it.uid)
            return it.uid
        }

        return ""
    }


    // Set functions

    fun add(pItem: Contact): String {
        // create a new ID
        val id = userDoc.collection(Constants.CONTACTS).document().id
        pItem.ID = id

        userDoc.collection(Constants.CONTACTS).document(id).set(pItem)
        return id
    }
    fun add(pItem: Category): String {
        // create a new ID
        val id = userDoc.collection(Constants.CATEGORIES).document().id
        pItem.ID = id

        userDoc.collection(Constants.CATEGORIES).document(id).set(pItem)
        return id
    }
    fun add(pItem: Event): String {
        // create a new ID
        val id = userDoc.collection(Constants.EVENTS).document().id
        pItem.ID = id

        userDoc.collection(Constants.EVENTS).document(id).set(pItem)
        return id
    }

    fun update(pItem: Contact) {
        userDoc.collection(Constants.CONTACTS).document(pItem.ID).set(pItem)
    }
    fun update(pItem: Category) {
        userDoc.collection(Constants.CATEGORIES).document(pItem.ID).set(pItem)
    }
    fun update(pItem: Event) {
        userDoc.collection(Constants.EVENTS).document(pItem.ID).set(pItem)
    }

    fun delete(pItem: Contact) {
        userDoc.collection(Constants.CONTACTS).document(pItem.ID).delete()
    }
    fun delete(pItem: Category) {
        userDoc.collection(Constants.CATEGORIES).document(pItem.ID).delete()
    }
    fun delete(pItem: Event) {
        userDoc.collection(Constants.EVENTS).document(pItem.ID).delete()
    }


    fun link(pContact: Contact, pCategory: Category){
        val id = userDoc.collection(Constants.CONTACTS_2_CATEGORIES).document().id
        userDoc.collection(Constants.CONTACTS_2_CATEGORIES).document(id)
            .set(Contact2Category(
                pContact.ID,
                pCategory.ID
            ))
    }
    fun link(pContact: Contact, pEvent: Event){
        val id = userDoc.collection(Constants.CONTACTS_2_CATEGORIES).document().id
        userDoc.collection(Constants.CONTACTS_2_EVENTS).document(id)
            .set(Contact2Event(
                pContact.ID,
                pEvent.ID
            ))
    }
    fun link(pEvent: Event, pCategory: Category){
        val id = userDoc.collection(Constants.CONTACTS_2_CATEGORIES).document().id
        userDoc.collection(Constants.EVENTS_2_CATEGORIES).document(id)
            .set(Event2Category(
                pEvent.ID,
                pCategory.ID
            ))
    }


    suspend fun unlink(pContact: Contact, pCategory: Category){
        userDoc.collection(Constants.CONTACTS_2_CATEGORIES)
            .whereEqualTo("contactID", pContact.ID)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().await()
            .documents.forEach {
                it.reference.delete()
            }
    }
    suspend fun unlink(pContact: Contact, pEvent: Event){
        userDoc.collection(Constants.CONTACTS_2_EVENTS)
            .whereEqualTo("contactID", pContact.ID)
            .whereEqualTo("eventID", pEvent.ID)
            .get().await()
            .documents.forEach {
                it.reference.delete()
            }
    }
    suspend fun unlink(pEvent: Event, pCategory: Category){
        userDoc.collection(Constants.EVENTS_2_CATEGORIES)
            .whereEqualTo("eventID", pEvent.ID)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().await().documents.forEach {
                it.reference.delete()
            }
    }
    suspend fun unlink(pContact: Contact){
        userDoc.collection(Constants.CONTACTS_2_EVENTS)
            .whereEqualTo("contactID", pContact.ID)
            .get().await().documents.forEach {
                it.reference.delete()
            }

        userDoc.collection(Constants.CONTACTS_2_CATEGORIES)
            .whereEqualTo("contactID", pContact.ID)
            .get().await().documents.forEach {
                it.reference.delete()
            }
    }
    suspend fun unlink(pCategory: Category){
        userDoc.collection(Constants.CONTACTS_2_CATEGORIES)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().await().documents.forEach {
                it.reference.delete()
            }

        userDoc.collection(Constants.EVENTS_2_CATEGORIES)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().await().documents.forEach {
                it.reference.delete()
            }
    }
    suspend fun unlink(pEvent: Event){
        userDoc.collection(Constants.CONTACTS_2_EVENTS)
            .whereEqualTo("eventID", pEvent.ID)
            .get().await().documents.forEach {
                it.reference.delete()
            }

        userDoc.collection(Constants.CONTACTS_2_CATEGORIES)
            .whereEqualTo("eventID", pEvent.ID)
            .get().await().documents.forEach {
                it.reference.delete()
            }
    }


    // Get functions

    suspend fun getContact(pID: String) : Contact? {
        return userDoc.collection(Constants.CONTACTS).document(pID).get().await().toObject(Contact::class.java)
    }
    suspend fun getCategory(pID: String) : Category? {
        return userDoc.collection(Constants.CATEGORIES).document(pID).get().await().toObject(Category::class.java)
    }
    suspend fun getEvent(pID: String) : Event? {
        return userDoc.collection(Constants.EVENTS).document(pID).get().await().toObject(Event::class.java)
    }

    suspend fun getCategories(pItem: Contact) : List<Category> {
        return userDoc.collection(Constants.CATEGORIES)
            .whereIn("ID", userDoc
                .collection(Constants.CONTACTS_2_CATEGORIES)
                .whereEqualTo("contactID", pItem.ID)
                .get().await().toObjects(Contact2Category::class.java).map{ it.categoryID }
            )
            .get().await().toObjects(Category::class.java)
    }
    suspend fun getEvents(pItem: Contact) : List<Event> {
        return userDoc.collection(Constants.EVENTS)
            .whereIn("ID", userDoc
                .collection(Constants.CONTACTS_2_CATEGORIES)
                .whereEqualTo("contactID", pItem.ID)
                .get().await().toObjects(Contact2Event::class.java).map{ it.eventID }
            )
            .get().await().toObjects(Event::class.java)
    }

    suspend fun getAllContacts() : List<Contact> {
        return userDoc.collection(Constants.CONTACTS).get().await().toObjects(Contact::class.java)
    }
    suspend fun getAllCategories() : List<Category> {
        return userDoc.collection(Constants.CATEGORIES).get().await().toObjects(Category::class.java)
    }
    suspend fun getAllEvents() : List<Event> {
        return userDoc.collection(Constants.EVENTS).get().await().toObjects(Event::class.java)
    }

    suspend fun exists(pItem: Contact) : Boolean {
        return userDoc.collection(Constants.CONTACTS).document(pItem.ID).get().await().exists()
    }
    suspend fun exists(pItem: Category) : Boolean {
        return userDoc.collection(Constants.CATEGORIES).document(pItem.ID).get().await().exists()
    }
    suspend fun exists(pItem: Event) : Boolean {
        return userDoc.collection(Constants.EVENTS).document(pItem.ID).get().await().exists()
    }

    suspend fun exists(pContact: Contact, pCategory: Category) : Boolean {
        return !userDoc.collection(Constants.CONTACTS_2_CATEGORIES)
            .whereEqualTo("contactID", pContact.ID)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().await().isEmpty
    }
    suspend fun exists(pContact: Contact, pEvent: Event) : Boolean {
        return !userDoc.collection(Constants.CONTACTS_2_EVENTS)
            .whereEqualTo("contactID", pContact.ID)
            .whereEqualTo("eventID", pEvent.ID)
            .get().await().isEmpty
    }
    suspend fun exists(pEvent: Event, pCategory: Category) : Boolean {
        return !userDoc.collection(Constants.EVENTS_2_CATEGORIES)
            .whereEqualTo("eventID", pEvent.ID)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().await().isEmpty
    }
}