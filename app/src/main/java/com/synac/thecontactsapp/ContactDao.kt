package com.synac.thecontactsapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


//dao is where all the queries are present / where all the queries are handled
//all functions of data (insert,update,delete,retrieve) is done through queries
@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact)

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    //to display and retrieve the data
    @Query("SELECT * FROM contacts")
    fun getAllContacts(): Flow<List<Contact>>
}