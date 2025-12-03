package com.example.lolnk.repository

import com.example.lolnk.data.local.Contact
import com.example.lolnk.data.local.ContactDao
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()

    suspend fun getContactByNodeId(nodeId: Int): Contact? {
        return contactDao.getContactByNodeId(nodeId)
    }

    suspend fun insert(contact: Contact) {
        contactDao.insert(contact)
    }

    suspend fun delete(contact: Contact) {
        contactDao.delete(contact)
    }
}