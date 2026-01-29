package com.synac.thecontactsapp

import androidx.room.Database
import androidx.room.RoomDatabase

//database is where we initialize the room database
@Database(
    entities = [Contact::class], //data
    version = 1, //version of the database
    exportSchema = false //because we don't want to keep the history of versions
)
//abstract because it's mostly used to extend or implement abstract methods(functions with no body)
abstract class ContactDatabase: RoomDatabase() {
    abstract fun contactDao(): ContactDao
}