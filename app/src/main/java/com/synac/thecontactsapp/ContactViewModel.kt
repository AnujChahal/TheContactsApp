package com.synac.thecontactsapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ContactViewModel(
    private val repository: ContactRepository //we need stored data present in the ContactRepository
): ViewModel() { //it helps data in ui to communicate

    val allContacts: LiveData<List<Contact>> = repository.allContacts.asLiveData()
    /*it's data type is LiveData, to this variable we'll assign all contacts from repository,
    considering it as live data,
    live data is a lifecycle aware data holder that's used to update the ui automatically when data changes,
    ensuring that the app's ui is always up to date with the latest data from the repository.*/

    fun addContact(
        image: String,
        name: String,
        phoneNumber: String,
        email: String
    ) { //launching new coroutine thread using viewmodel scope
        viewModelScope.launch {
            //viewModelScope ensures operation is performed asynchronously,
            //and is tied to the lifecycle of the viewModel
            val contact/*new contact object with provided details*/ = Contact(
                id = 0,
                image = image,
                name = name,
                phoneNumber = phoneNumber,
                email = email
            ) //this contact is then inserted into the repository
            repository.insert(contact)
        }
    }

    /*similarly we'll update and delete contact,
    but this time instead of each parameter we'll directly consider a contact item
    it's line whenever we update or delete the contact entire info will be updated or deleted*/
    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            repository.update(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.delete(contact)
        }
    }
}

/*View Model Factory
Initializes the viewmodel, it's mostly syntax only
*/
class ContactViewModelFactory(
    /*a factory for making instanced of ContactViewModel
    it takes a ContactRepository as a parameter,
    and overwrite the create method to return a ContactViewModel instance,
    if the requested model class is ContactViewModel,
    if the requested model class is not ContactViewModel,
    it will throw an IllegalArgumentException,
    basically this entire code ensures that the ContactViewModel is created with the necessary repository parameters.
    */
    private val repository: ContactRepository
): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ContactViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}