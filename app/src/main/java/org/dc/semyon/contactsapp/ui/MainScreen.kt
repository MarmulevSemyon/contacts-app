package org.dc.semyon.contactsapp.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.dc.semyon.contactsapp.viewmodel.ContactsViewModel
import android.content.pm.PackageManager
import androidx.compose.ui.text.style.TextAlign

@Composable
fun MainScreen(viewModel: ContactsViewModel = viewModel()) {
    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsState()
    var phoneToCall by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var hasContactsPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    )}
    // Фильтрация контактов
        val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                        contact.phone.contains(searchQuery)
            }
        }
    }

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted){
            viewModel.loadContacts()
        }
    }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        when {
            isGranted && phoneToCall.filter { it.isDigit() }.isNotEmpty() -> {//есть разрешение и номер
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${phoneToCall.filter { it.isDigit() }}"))
                context.startActivity(intent)
            }
            !isGranted->{
                Toast.makeText(context, "Нет разрешения на звонки(((", Toast.LENGTH_LONG).show() //нет разрешения

            }
            phoneToCall.filter { it.isDigit() }.isEmpty() -> Toast.makeText(context, "Номер без цифр(((", Toast.LENGTH_SHORT).show() //нет номера
        }
    }


    LaunchedEffect(Unit) {
        contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        if(hasContactsPermission){
            // поле для поиска
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск контактов") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            !hasContactsPermission->{
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Чтобы покзывать контакты необходимо разрешение", textAlign = TextAlign.Center)
                }
            }
             !viewModel.hasLoadedContacts -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            contacts.isEmpty() && viewModel.hasLoadedContacts->{
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Нет доступных контактов")
                }
            }

            filteredContacts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Контакты не найдены")
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredContacts) { contact ->
                        ContactCard(contact = contact){
                            phoneToCall = contact.phone
                            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        }
                    }
                }
            }
        }
    }
}
