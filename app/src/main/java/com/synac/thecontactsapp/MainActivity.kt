package com.synac.thecontactsapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.synac.thecontactsapp.ui.theme.GreenJC
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initializing all the required classes
        val database = Room.databaseBuilder(
            applicationContext,
            ContactDatabase::class.java,
            "contact_database" //database name
        ).build()
        //this database is a part of a repository
        val repository = ContactRepository(database.contactDao())
        //this repository will be a part of a viewmodel
        val viewmodel/*ues this viewmodel to connect out view inside ui */: ContactViewModel by viewModels/*gives us access to attached ContactViewModelFactory*/{
            ContactViewModelFactory(repository)
        }

        setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController, startDestination = "contactList"
            ) {
                //creating roots
                composable("contactList") {
                    ContactListScreen(viewmodel, navController)
                }
                composable("addContact") {
                    AddContactScreen(viewmodel, navController)
                }
                composable("contactDetail/{contactId}") { backStackEntry -> //variable, this allow access to the navigation backstack entry from which contact ID can be accessed
                    val contactId =
                        backStackEntry.arguments?.getString("contactId")?.toInt()
                    /*retrieves the contactId form backStackEntry and converts to an integer
                    once extracted we need to fetch the contact,
                    this observes the list of all contacts from the viewmodel and finds the contact with the matching contact ID,
                    if matching contact is found it navigates to the contact detail screen*/
                    val contact =
                        viewmodel.allContacts.observeAsState(initial = emptyList()).value
                            .find { it.id == contactId }
                    contact?.let {
                        ContactDetailScreen(it, viewmodel, navController)
                    }
                }
                composable("editContact/{contactId}") { backStackEntry ->
                    val contactId =
                        backStackEntry.arguments?.getString("contactId")?.toInt()
                    val contact =
                        viewmodel.allContacts.observeAsState(initial = emptyList()).value
                            .find { it.id == contactId }
                    contact?.let {
                        EditContactScreen(it, viewmodel, navController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    viewModel: ContactViewModel,
    navController: NavController,
) {
    //adding toast message, we require a context variable
    val context = LocalContext.current.applicationContext
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var phonenumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    //creating a launcher variable
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri/*this code initializes a Content picker using jetpack compose,
        rememberLauncherForActivityResult sets up and activity result launcher for selecting content like images from the device,
        when the user picks a file, the selected URI is assigned to image URI variable,
        in easy way = launchers help us pick an image from the gallery,
        and whatever the image URI means the path of the image is,
        that's assigned to the image URI variable*/
    }
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text(text = "Add Contact", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Add Contact",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_contact),
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenJC,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            imageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),/*to retrieve image chosen from gallery*/
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.padding(12.dp))
            Button(
                onClick = { launcher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(GreenJC)
            ) {
                Text(text = "Choose Image")
            }
            Spacer(modifier = Modifier.padding(16.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = "Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                value = phonenumber,
                onValueChange = { phonenumber = it },
                label = { Text(text = "Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.padding(16.dp))
            Button(
                onClick = {
                    imageUri?.let {
                        //creating copyUriToInternalStorage function
                        val internalPath = copyUriToInternalStorage(context, it, "$name.jpg")
                        internalPath?.let { path -> //inside internalPath creating path variable, this will be our internal storage image path
                            viewModel.addContact(
                                path,
                                name,
                                phonenumber,
                                email
                            ) //once we have path we'll add contact using viewmodel
                            navController.navigate("contactList") { //once stored we navigate to display contact screen
                                popUpTo(0) //whenever we click the back button it'll be properly navigate to the required screen instead of stacking app the screens
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(GreenJC)
            ) {
                Text(text = "Add Contact")
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(contact.image),
                contentDescription = contact.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.padding(16.dp))
            Text(contact.name)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    viewModel: ContactViewModel,
    navController: NavController
) {
    val context = LocalContext.current.applicationContext
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text("Contacts", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Contacts",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.contact_icon),
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenJC,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = GreenJC,
                onClick = {
                    navController.navigate("addContact")
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { paddingValues ->
        val contacts/*contacts list if observed from viewModel which is initially set to an empty list*/ by viewModel.allContacts.observeAsState(
            initial = emptyList()
        )
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(contacts) { contact ->
                ContactItem(contact = contact) {
                    navController.navigate("contactDetail/${contact.id}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contact: Contact,
    viewModel: ContactViewModel,
    navController: NavController
) {
    val context = LocalContext.current.applicationContext
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text("Contact Details", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Contact Details",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.contact),
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenJC,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = GreenJC,
                onClick = { navController.navigate("editContact/${contact.id}") }
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Contact")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(contact.image),
                        contentDescription = contact.name,
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Name:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(contact.name, fontSize = 16.sp)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Phone Number:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(contact.phoneNumber, fontSize = 16.sp)
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Email:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(contact.email, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                colors = ButtonDefaults.buttonColors(GreenJC),
                onClick = {
                    viewModel.deleteContact(contact)
                    navController.navigate("contactList") {
                        popUpTo(0)
                    }
                }
            ) {
                Text("Delete Contact")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    contact: Contact,
    viewModel: ContactViewModel,
    navController: NavController
) {
    val context = LocalContext.current.applicationContext
    var imageUri by remember {
        mutableStateOf(contact.image)
    }
    var name by remember {
        mutableStateOf(contact.name)
    }
    var phoneNumber by remember {
        mutableStateOf(contact.phoneNumber)
    }
    var email by remember {
        mutableStateOf(contact.email)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { newUri ->
            val internalPath = copyUriToInternalStorage(context, newUri, "$name.jpg")
            internalPath?.let { path -> imageUri = path }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text(text = "Edit Contact", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Edit Contact",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit_contact),
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenJC,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),/*to retrieve image chosen from gallery*/
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.padding(12.dp))
            Button(
                onClick = { launcher.launch("images/*") },
                colors = ButtonDefaults.buttonColors(GreenJC)
            ) {
                Text(text = "Choose Image")
            }
            Spacer(modifier = Modifier.padding(16.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = "Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(text = "Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.padding(16.dp))
            Button(
                onClick = {
                    val updateContact = contact.copy(
                        image = imageUri,
                        name = name,
                        phoneNumber = phoneNumber,
                        email = email
                    )
                    viewModel.updateContact(updateContact)
                    navController.navigate("contactList") {
                        popUpTo(0)
                    }
                },
                colors = ButtonDefaults.buttonColors(GreenJC)
            ) {
                Text(text = "Update Contact")
            }
        }
    }
}


//copies a file from a given uri to the internal storage of the app
fun copyUriToInternalStorage(
    context: Context,
    uri: Uri,
    fileName: String
): String? {
    val file/*create a new file in apps internal storage*/ = File(context.filesDir, fileName)
    return try {
        context.contentResolver.openInputStream(uri)/*opens a input stream from uri*/
            ?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)/*copies the input stream to the new file output stream*/
                }
            }
        file.absolutePath /*if successful it returns the absolute path of the copied file*/
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
