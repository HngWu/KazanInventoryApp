package com.example.kazaninventoryapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Picture
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kazaninventoryapp.Models.Asset
import com.example.kazaninventoryapp.httpservice.httpgetassets
import com.example.kazaninventoryapp.ui.theme.KazanInventoryAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.jetbrains.annotations.Async
import java.io.File
import java.util.UUID
import android.Manifest

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("kilo", "Permission granted")
        } else {
            Log.i("kilo", "Permission denied")
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("kilo", "Permission previously granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> Log.i("kilo", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContent {
            val navController = rememberNavController()
            NavHost(navController, "RegisterAssets") {
                composable("Assets") { AssetsScreen() }
                composable("RegisterAssets") { RegisterAssets(context = applicationContext) }
            }
        }
        requestCameraPermission()

    }


}




@Composable
fun RegisterAssets(context: Context) {
    var assetName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var assetGroup by remember { mutableStateOf("") }
    var accountableParty by remember { mutableStateOf("") }
    var assetDescription by remember { mutableStateOf("") }
    var expiredWarranty by remember { mutableStateOf("") }
    var pictures by remember { mutableStateOf<List<Picture>>(emptyList()) }
    var assetSN by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf(listOf<Uri>()) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris != null) {
            imageUris = imageUris + uris
        }
    }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            imageUris = imageUris + cameraImageUri!!
        }
    }


    fun generateImageUri(context: Context): Uri {
        val imageFile = File(context.getExternalFilesDir(null), "${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Register Assets", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = "",
            onValueChange = { assetName = it },
            label = { Text("Asset Name") },
            modifier = Modifier.width(300.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            DropDownMenu(
                listOf(
                    "Exploration",
                    "Production",
                    "Transportation",
                    "R&D",
                    "Distribution",
                    "QHSE"
                ), "Department"
            ) {
                department = it
            }

            DropDownMenu(listOf("Kazan", "Volka", "Moscow "), "Location") {
                location = it
            }

        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            DropDownMenu(
                listOf("Hydraulic", "Electrical", "Mechanical "), "Asset Group"
            ) {
                assetGroup = it
            }
            DropDownMenu(listOf("NA", "NA", "NA"), "Acc. Party") {
                accountableParty = it
            }

        }
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = "",
            onValueChange = { assetDescription = it },
            label = { Text("Asset Description") },
            modifier = Modifier.width(300.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            DatePickerDocked("startDate", "Start Date", expiredWarranty) {
                expiredWarranty = it

            }
            Text(text = "Asset SN: \n" + assetSN)
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                galleryLauncher.launch("image/*")
            }) {
                Text("Select Images from Device")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                try {



                    val generatedUri = generateImageUri(context)
                    cameraImageUri = generatedUri
                    cameraLauncher.launch(generatedUri)
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }

            }) {
                Text("Capture Image with Camera")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display the selected or captured images
            if (imageUris.isNotEmpty()) {
                LazyRow {
                    items(imageUris.size) { index ->
                        val uri = imageUris[index]
                        Image(
                            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(8.dp)
                        )
                    }
                }
            }        }
        Spacer(modifier = Modifier.height(10.dp))



        Spacer(modifier = Modifier
            .height(10.dp)
            .width(300.dp))

        Row(
            horizontalArrangement = Arrangement.Absolute.Right,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Button(onClick = { /* TODO: Add action here */ }) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = { /* TODO: Add action here */ }) {
                Text("Register")
            }
        }


    }
}


@SuppressLint("UnrememberedMutableState")
@Composable
fun AssetsScreen() {
    var assetsList = remember { mutableStateOf<List<Asset>>(emptyList()) }
    var filteredAssetsList = remember { mutableStateOf<List<Asset>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAssetGroup by mutableStateOf("")
    var selectedDepartment by mutableStateOf("")
    var startDate by mutableStateOf("")
    var endDate by mutableStateOf("")
    val httpgetassets = remember { httpgetassets() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE


    fun filterAssets() {
        if (((searchQuery.isEmpty() || searchQuery.length < 3) && selectedAssetGroup.isEmpty() && selectedDepartment.isEmpty() && startDate.isEmpty() && endDate.isEmpty())) {
            filteredAssetsList.value = assetsList.value

        } else {
            filteredAssetsList.value = assetsList.value.filter { asset ->
                (searchQuery.isEmpty() || asset.AssetSN.contains(
                    searchQuery,
                    ignoreCase = true
                ) || asset.AssetName.contains(searchQuery, ignoreCase = true)) &&
                        (selectedAssetGroup.isEmpty() || asset.AssetGroupName == selectedAssetGroup) &&
                        (selectedDepartment.isEmpty() || asset.DepartmentName == selectedDepartment) &&
                        (startDate.isEmpty() || asset.WarrantyDate >= startDate) &&
                        (endDate.isEmpty() || asset.WarrantyDate <= endDate)
            }
        }
    }


    LaunchedEffect(Unit) {
        val fetchedAssets = withContext(Dispatchers.IO)
        { httpgetassets.getAssets() }
        assetsList.value = fetchedAssets ?: emptyList()
        filteredAssetsList.value = assetsList.value

    }




    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (!isLandscape) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropDownMenu(
                    listOf(
                        "Exploration",
                        "Production",
                        "Transportation",
                        "R&D",
                        "Distribution",
                        "QHSE"
                    ), "Department"
                ) {
                    selectedDepartment = it
                    filterAssets()
                }
                DropDownMenu(listOf("Hydraulic", "Electrical", "Mechanical "), "Asset Group") {
                    selectedAssetGroup = it
                    filterAssets()
                }

            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DatePickerDocked("startDate", "Start Date", startDate) {
                    startDate = it
                    filterAssets()

                }
                DatePickerDocked("endDate", "End Date", endDate) {
                    endDate = it
                    filterAssets()
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (searchQuery.length > 2) {
                            filterAssets()
                        }

                    },
                    label = { Text("Search") },
                    modifier = Modifier.width(300.dp)
                )

                Spacer(
                    modifier = Modifier
                        .width(10.dp)
                        .height(30.dp)
                        .padding(5.dp)
                        .align(Alignment.CenterVertically)
                )
                Button(onClick = {
                    searchQuery = ""
                    selectedAssetGroup = ""
                    selectedDepartment = ""
                    startDate = ""
                    endDate = ""
                    filterAssets()
                }) {
                    Text("Clear")
                }

                Spacer(modifier = Modifier.height(10.dp))

            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Assets List", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(filteredAssetsList.value) { asset ->
                AssetCard(asset)
            }
        }
        Text(
            text = "Showing ${filteredAssetsList.value.size} of ${assetsList.value.size} records",
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))
        FloatingActionButton(
            onClick = {
                /* TODO: Add action here */
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }

    }
}

@Composable
fun AssetCard(asset: Asset) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = asset.AssetName, modifier = Modifier.weight(1f))
            Text(text = asset.AssetSN, modifier = Modifier.weight(1f))
            IconButton(onClick = { /* TODO: Handle Move action */ }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Move")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = asset.AssetName)
                    Text(text = asset.DepartmentName)
                    Text(text = asset.AssetSN)
                    Text(text = asset.WarrantyDate)
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = { /* TODO: Handle Edit action */ }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { /* TODO: Handle Move action */ }) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Move")
                    }
                    IconButton(onClick = { /* TODO: Handle History action */ }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "History")
                    }
                }
            }


        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDocked(
    identifier: String,
    selectedDate: String,
    label: String,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    Box(
        modifier = Modifier
            .width(200.dp)
            .padding(5.dp)
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .width(200.dp)
                .height(64.dp)
        )

        if (showDatePicker) {
            Popup(
                onDismissRequest = { showDatePicker = false },
                alignment = Alignment.TopStart,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val selectedDateMillis = datePickerState.selectedDateMillis
                                    if (selectedDateMillis != null) {
                                        onDateSelected(convertMillisToDate(selectedDateMillis))
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

            }
        }
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenu(items: List<String>, name: String, onItemSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .width(200.dp)
            .padding(5.dp) // Adjust the width as needed

    ) {
        TextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            label = { Text(name) },

            )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        if (item == "Default") {
                            selectedItem = ""
                        } else {
                            selectedItem = item
                        }
                        expanded = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }

}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KazanInventoryAppTheme {
        //RegisterAssets(context = context)
    }
}