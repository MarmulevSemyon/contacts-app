package org.dc.semyon.contactsapp.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.dc.semyon.contactsapp.model.Contact

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts
    var hasLoadedContacts by mutableStateOf(false)
        private set
    fun loadContacts() {
        viewModelScope.launch {
            val list = mutableListOf<Contact>()
            try{
                val resolver: ContentResolver = getApplication<Application>().contentResolver
                val cursor = resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC"
                )
                cursor?.use { // use автоматически закрывает
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext()) {
                        val name = it.getString(nameIndex) ?: "No name"
                        val phone = it.getString(phoneIndex) ?: "No number"
                        list.add(Contact(name, phone))
                    }
                }
            }catch(e: Exception){
                Log.e("Contacts123", "Ошибка загрузки", e)
            }

            _contacts.value = list
            hasLoadedContacts = true
        }
    }
}