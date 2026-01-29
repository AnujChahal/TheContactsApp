package com.synac.thecontactsapp

import kotlinx.coroutines.flow.Flow

//a place where all your data will be stored / where we store the data with the help of queries
class ContactRepository(
    private val contactDao: ContactDao //calling dao interface for accessing queries
) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    //Contact is data & Flow is part of Coroutines Flow Packet which is used for handling asynchronous data streams

    suspend fun insert(contact: Contact/*creating insert function with the data i.e., entity class Contact*/) {
        contactDao.insert(contact) //calling insert function from DAO & storing data (contact)
    }

    suspend fun update(contact: Contact) {
        contactDao.update(contact)
    }

    suspend fun delete(contact: Contact) {
        contactDao.delete(contact)
    }
}