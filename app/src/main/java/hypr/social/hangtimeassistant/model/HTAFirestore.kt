package hypr.social.hangtimeassistant.model

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import hypr.social.hangtimeassistant.utils.Constants
import kotlin.contracts.Returns
import kotlin.contracts.ReturnsNotNull

object HTAFirestore {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var userCollection : DocumentReference

    fun registerUser(userInfo: User): Task<Void>{
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
            userCollection = db.collection(Constants.USERS).document(it.uid)
            return it.uid
        }

        return ""
    }


    // Set functions

    fun add(pItem: Contact): String {
        val id = userCollection.collection(Constants.CONTACTS).document().id
        userCollection.collection(Constants.CONTACTS).document(id).set(pItem)
        return id
    }
    fun add(pItem: Category): String {
        val id = userCollection.collection(Constants.CATEGORIES).document().id
        userCollection.collection(Constants.CATEGORIES).document(id).set(pItem)
        return id
    }
    fun add(pItem: Event): String {
        val id = userCollection.collection(Constants.EVENTS).document().id
        userCollection.collection(Constants.EVENTS).document(id).set(pItem)
        return id
    }


    fun update(pItem: Contact) {
        userCollection.collection(Constants.CONTACTS).document(pItem.ID).set(pItem)
    }
    fun update(pItem: Category) {
        userCollection.collection(Constants.CATEGORIES).document(pItem.ID).set(pItem)
    }
    fun update(pItem: Event) {
        userCollection.collection(Constants.EVENTS).document(pItem.ID).set(pItem)
    }

    fun delete(pItem: Contact) {
        userCollection.collection(Constants.CONTACTS).document(pItem.ID).delete()
    }
    fun delete(pItem: Category) {
        userCollection.collection(Constants.CATEGORIES).document(pItem.ID).delete()
    }
    fun delete(pItem: Event) {
        userCollection.collection(Constants.EVENTS).document(pItem.ID).delete()
    }


    fun link(pContact: Contact, pCategory: Category){
        val id = userCollection.collection(Constants.CONTACTS_2_CATEGORIES).document().id
        userCollection.collection(Constants.CONTACTS_2_CATEGORIES).document(id)
            .set(Contact2Category(
                pContact.ID,
                pCategory.ID
            ))
    }
    fun link(pContact: Contact, pEvent: Event){
        val id = userCollection.collection(Constants.CONTACTS_2_CATEGORIES).document().id
        userCollection.collection(Constants.CONTACTS_2_EVENTS).document(id)
            .set(Contact2Event(
                pContact.ID,
                pEvent.ID
            ))
    }
    fun link(pEvent: Event, pCategory: Category){
        val id = userCollection.collection(Constants.CONTACTS_2_CATEGORIES).document().id
        userCollection.collection(Constants.EVENTS_2_CATEGORIES).document(id)
            .set(Event2Category(
                pEvent.ID,
                pCategory.ID
            ))
    }


    fun unlink(pContact: Contact, pCategory: Category){
        userCollection.collection(Constants.CONTACTS_2_CATEGORIES)
            .whereEqualTo("contactID", pContact.ID)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().result.documents.forEach {
                it.reference.delete()
            }
    }
    fun unlink(pContact: Contact, pEvent: Event){
        userCollection.collection(Constants.CONTACTS_2_EVENTS)
            .whereEqualTo("contactID", pContact.ID)
            .whereEqualTo("eventID", pEvent.ID)
            .get().result.documents.forEach {
                it.reference.delete()
            }
    }
    fun unlink(pEvent: Event, pCategory: Category){
        userCollection.collection(Constants.EVENTS_2_CATEGORIES)
            .whereEqualTo("eventID", pEvent.ID)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().result.documents.forEach {
                it.reference.delete()
            }
    }
    fun unlink(pContact: Contact){
        userCollection.collection(Constants.CONTACTS_2_EVENTS)
            .whereEqualTo("contactID", pContact.ID)
            .get().result.documents.forEach {
                it.reference.delete()
            }

        userCollection.collection(Constants.CONTACTS_2_CATEGORIES)
            .whereEqualTo("contactID", pContact.ID)
            .get().result.documents.forEach {
                it.reference.delete()
            }
    }
    fun unlink(pCategory: Category){
        userCollection.collection(Constants.CONTACTS_2_CATEGORIES)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().result.documents.forEach {
                it.reference.delete()
            }

        userCollection.collection(Constants.EVENTS_2_CATEGORIES)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().result.documents.forEach {
                it.reference.delete()
            }
    }
    fun unlink(pEvent: Event){
        userCollection.collection(Constants.CONTACTS_2_EVENTS)
            .whereEqualTo("eventID", pEvent.ID)
            .get().result.documents.forEach {
                it.reference.delete()
            }

        userCollection.collection(Constants.CONTACTS_2_CATEGORIES)
            .whereEqualTo("eventID", pEvent.ID)
            .get().result.documents.forEach {
                it.reference.delete()
            }
    }


    // Get functions

    fun getContact(pID: String) : Contact? {
        return userCollection.collection(Constants.CONTACTS).document(pID).get().result.toObject(Contact::class.java)
    }
    fun getCategory(pID: String) : Category? {
        return userCollection.collection(Constants.CATEGORIES).document(pID).get().result.toObject(Category::class.java)
    }
    fun getEvent(pID: String) : Event? {
        return userCollection.collection(Constants.EVENTS).document(pID).get().result.toObject(Event::class.java)
    }

    fun getCategories(pItem: Contact) : List<Category> {
        return userCollection.collection(Constants.CATEGORIES)
            .whereIn("ID",userCollection
                .collection(Constants.CONTACTS_2_CATEGORIES)
                .whereEqualTo("contactID", pItem.ID)
                .get().result.toObjects(Contact2Category::class.java).map{ it.categoryID }
            )
            .get().result.toObjects(Category::class.java)
    }
    fun getEvents(pItem: Contact) : List<Event> {
        return userCollection.collection(Constants.EVENTS)
            .whereIn("ID", userCollection
                .collection(Constants.CONTACTS_2_CATEGORIES)
                .whereEqualTo("contactID", pItem.ID)
                .get().result.toObjects(Contact2Event::class.java).map{ it.eventID }
            )
            .get().result.toObjects(Event::class.java)
    }

    fun getAllContacts() : List<Contact> {
        return userCollection.collection(Constants.CONTACTS).get().result.toObjects(Contact::class.java)
    }
    fun getAllCategories() : List<Category> {
        return userCollection.collection(Constants.CATEGORIES).get().result.toObjects(Category::class.java)
    }
    fun getAllEvents() : List<Event> {
        return userCollection.collection(Constants.EVENTS).get().result.toObjects(Event::class.java)
    }

    fun exists(pItem: Contact) : Boolean {
        return userCollection.collection(Constants.CONTACTS).document(pItem.ID).get().result.exists()
    }
    fun exists(pItem: Category) : Boolean {
        return userCollection.collection(Constants.CATEGORIES).document(pItem.ID).get().result.exists()
    }
    fun exists(pItem: Event) : Boolean {
        return userCollection.collection(Constants.EVENTS).document(pItem.ID).get().result.exists()
    }

    fun exists(pContact: Contact, pCategory: Category) : Boolean {
        return !userCollection.collection(Constants.CONTACTS_2_CATEGORIES)
            .whereEqualTo("contactID", pContact.ID)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().result.isEmpty
    }
    fun exists(pContact: Contact, pEvent: Event) : Boolean {
        return !userCollection.collection(Constants.CONTACTS_2_EVENTS)
            .whereEqualTo("contactID", pContact.ID)
            .whereEqualTo("eventID", pEvent.ID)
            .get().result.isEmpty
    }
    fun exists(pEvent: Event, pCategory: Category) : Boolean {
        return !userCollection.collection(Constants.EVENTS_2_CATEGORIES)
            .whereEqualTo("eventID", pEvent.ID)
            .whereEqualTo("categoryID", pCategory.ID)
            .get().result.isEmpty
    }
}