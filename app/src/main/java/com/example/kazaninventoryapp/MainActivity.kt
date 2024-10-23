package com.example.kazaninventoryapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID
import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.navigation.NavController
import com.example.kazaninventoryapp.Models.Employee
import com.example.kazaninventoryapp.Models.createNewAsset
import com.example.kazaninventoryapp.httpservice.httpgetemployees
import com.example.kazaninventoryapp.httpservice.httpgetlocations
import com.example.kazaninventoryapp.httpservice.httppostasset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.util.Base64
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.kazaninventoryapp.Models.EditAsset
import com.example.kazaninventoryapp.Models.TransferAsset
import com.example.kazaninventoryapp.Models.TransferHistory
import com.example.kazaninventoryapp.Models.UpdateAsset
import com.example.kazaninventoryapp.httpservice.httpgetassetforedit
import com.example.kazaninventoryapp.httpservice.httpgettransferhistory
import com.example.kazaninventoryapp.httpservice.httppostupdatedasset
import com.example.kazaninventoryapp.httpservice.httptransferassets
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random


//TODO 1: Implement the clear button for the search bar correctly by clearing the search query
//TODO 2: Implement the landscape mode for the app where change in orientation does not affect the filters
//TODO 3: Implement the generation of the new asset serial number when transferring an asset correctly
//TODO 4: Implement the snackbar for the app
//TODO 5: Implement display of multiple images correctly

@Suppress("NAME_SHADOWING")
class MainActivity : ComponentActivity() {
    private val assetChangeTrigger = mutableStateOf(false)

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
            NavHost(navController, "Assets") {
                composable("Assets") { AssetsScreen(navController, assetChangeTrigger) }
                composable("RegisterAssets") {
                    RegisterAssets(
                        navController,
                        context = applicationContext
                    )
                }
                composable(
                    "TransferAsset/{assetId}",
                    arguments = listOf(navArgument("assetId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val assetId = backStackEntry.arguments?.getInt("assetId") ?: return@composable
                    TransferAssetForm(navController, assetId, context = applicationContext) {
                        assetChangeTrigger.value = !assetChangeTrigger.value
                    }
                }
                composable(
                    "EditAssets/{assetId}",
                    arguments = listOf(navArgument("assetId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val assetId = backStackEntry.arguments?.getInt("assetId") ?: return@composable
                    EditAssets(navController, context = applicationContext, assetId = assetId) {
                        assetChangeTrigger.value = !assetChangeTrigger.value

                    }
                }
                composable(
                    "TransferHistory/{assetId}",
                    arguments = listOf(navArgument("assetId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val assetId = backStackEntry.arguments?.getInt("assetId") ?: return@composable
                    val httpgettransferhistory = remember { httpgettransferhistory() }
                    val transferHistory =
                        httpgettransferhistory.GetHistory(assetId) // Implement this function to fetch transfer history
                    TransferHistoryScreen(navController, assetId, transferHistory)
                }

            }

            requestCameraPermission()

        }


    }


    @Composable
    fun TransferHistoryScreen(
        navController: NavController,
        assetId: Int,
        transferHistory: MutableList<TransferHistory?>?
    ) {
        var transferHistory by remember { mutableStateOf(transferHistory) }
        var isLoading by remember { mutableStateOf(true) }



        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val httpgettransferhistory = httpgettransferhistory()
                val fetchedTransferHistory = httpgettransferhistory.GetHistory(assetId)
                transferHistory = fetchedTransferHistory
                isLoading = false

            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text("Transfer History", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))




            if (isLoading) {
                Text("No recent transfers in the last twelve months.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back")
                }
            } else {
                LazyColumn {
                    items(transferHistory!!.sortedBy { it!!.transferDate }) { record ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text("Transfer Date: ${record?.transferDate}")
                            Text("Old Department: ${record?.newDepartment}")
                            Text("New Department: ${record?.oldDepartment}")
                            Text("Old Asset SN: ${record?.fromAssetSn}")
                            Text("New Asset SN: ${record?.toAssetSn}")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back")
                }
            }
        }
    }


    @Composable
    fun TransferAssetForm(
        navController: NavController,
        assetId: Int,
        context: Context,
        onTransfer: (String) -> Unit,
    ) {
        var destinationDepartment by remember { mutableStateOf("") }
        var destinationLocation by remember { mutableStateOf("") }
        var newAssetSN by remember { mutableStateOf("") }
        var asset by remember { mutableStateOf<EditAsset?>(null) }
        var checkAsset by remember { mutableStateOf(false) }
        var assetName by remember { mutableStateOf("") }
        var department by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var assetGroup by remember { mutableStateOf("") }
        var assetGroupName by remember { mutableStateOf("") }
        var assetDescription by remember { mutableStateOf("") }
        var expiredWarranty by remember { mutableStateOf("") }
        var assetSN by remember { mutableStateOf("") }
        var assetGroupId by remember { mutableStateOf(0) }
        var getLocation by remember { mutableStateOf(false) }
        var newNumber by remember { mutableStateOf(0) }
        var generateNumber by remember { mutableStateOf(true) }
        var departmentList by remember {
            mutableStateOf<List<String>>(
                listOf(
                    "Exploration",
                    "Production",
                    "Transportation",
                    "R&D",
                    "Distribution",
                    "QHSE"
                )
            )
        }
        var locationList by remember { mutableStateOf<List<String>>(emptyList()) }
        var uniquePart by remember { mutableStateOf("") }
        var lastNumber by remember { mutableStateOf(0) }
        var departmentPart by remember { mutableStateOf("") }
        var assetGroupPart by remember { mutableStateOf("") }


        fun generateAssetSN(departmentId: Int, assetGroup: String): String {
            val assetgroupDict = mapOf(
                1 to "Hydraulic",
                3 to "Electrical",
                4 to "Mechanical "
            )

            departmentPart = departmentId.toString().padStart(2, '0')
            assetGroupPart = assetgroupDict.entries.find { it.value == assetGroup }?.key?.toString()?.padStart(2, '0') ?: "00"
            var intassetGroup = assetgroupDict.entries.find { it.value == assetGroup }?.key?.toInt() ?: 0

            if (generateNumber) {

                val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                lastNumber = sharedPreferences.getInt("last_number_${departmentPart}_${intassetGroup}", 0)
                newNumber = lastNumber + 1
                generateNumber = false
            }
            val previousSN = assetSN.split("/")[0]
            if (previousSN == departmentPart) {
                uniquePart = assetSN.split("/")[2]
                newAssetSN = "$departmentPart/$assetGroupPart/$uniquePart"
            } else {
                uniquePart = newNumber.toString().padStart(4, '0')
                newAssetSN = "$departmentPart/$assetGroupPart/$uniquePart"
            }

            return "$departmentPart/$assetGroupPart/$uniquePart"
        }
        if (true) {
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val fetchedAsset = httpgetassetforedit().getAssetForEdit(assetId)
                    if (fetchedAsset != null) {
                        asset = fetchedAsset
                        assetName = fetchedAsset.AssetName
                        location = fetchedAsset.Location
                        assetGroup = fetchedAsset.AssetGroupName
                        assetDescription = fetchedAsset.Description
                        expiredWarranty = fetchedAsset.WarrantyDate
                        assetSN = fetchedAsset.AssetSN
                        assetGroupName = fetchedAsset.AssetGroupName
                        department = fetchedAsset.DepartmentName
                        assetGroupId = fetchedAsset.AssetGroupID
                        checkAsset = true
                    }
                }
            }

            LaunchedEffect(destinationDepartment) {
                if (destinationDepartment.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        val httpgetlocations = httpgetlocations()
                        val locations = httpgetlocations.getLocations(destinationDepartment)
                        if (locations != null) {
                            locationList = locations
                        }
                    }
                }
            }
        }

        if (!checkAsset) {
            // Show loading or error state
            Text("Loading...")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text("Transfer Asset", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                // Display asset details (read-only)
                OutlinedTextField(
                    value = assetName,
                    onValueChange = {},
                    label = { Text("Asset Name") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = department,
                    onValueChange = {},
                    label = { Text("Current Department") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = assetSN,
                    onValueChange = {},
                    label = { Text("Asset SN") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Destination Department drop-down
                DropDownMenu(departmentList, "Destination Department",destinationDepartment, 200) { selectedItem->
                    destinationDepartment = selectedItem
                    newAssetSN = generateAssetSN(
                        departmentList.indexOf(destinationDepartment) + 1,
                        assetGroup
                    )
                    getLocation = true
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Destination Location drop-down
                DropDownMenu(locationList, "Destination Location", destinationLocation, 200) {selectedItem->
                    destinationLocation = selectedItem
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Display new asset serial number
                OutlinedTextField(
                    value = newAssetSN,
                    onValueChange = {},
                    label = { Text("New Asset SN") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                    Button(onClick = {
                        val transferAsset = TransferAsset(
                            assetId,
                            newAssetSN,
                            departmentList.indexOf(destinationDepartment) + 1,
                            destinationLocation,
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            val httpposttransferasset = httptransferassets()
                            httpposttransferasset.postAsset(transferAsset,
                                {
                                    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().putInt("last_number_${departmentPart}_${assetGroupPart}", uniquePart.toInt()).apply()
                                    onTransfer("Asset transferred successfully")
                                    Toast.makeText(context, "Asset transferred successfully", Toast.LENGTH_SHORT).show()
                                    navController.navigate("Assets")
                                },
                                {
                                    Log.i("kilo", "Asset transfer failed")
                                    Toast.makeText(context, "Asset transfer failed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }) {
                        Text("Submit")
                    }
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Cancel")
                    }
                    Toast.LENGTH_SHORT
                }
            }
        }
    }

    @Composable
    fun RegisterAssets(navController: NavController, context: Context) {
        var newNumber by remember { mutableStateOf(0) }
        var assetName by remember { mutableStateOf("") }
        var department by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var assetGroup by remember { mutableStateOf("") }
        var accountableParty by remember { mutableStateOf("") }
        var assetDescription by remember { mutableStateOf("") }
        var expiredWarranty by remember { mutableStateOf("") }
        var assetSN by remember { mutableStateOf("") }
        var imageUris by remember { mutableStateOf(listOf<Uri>()) }
        var locationList by remember { mutableStateOf<List<String>>(emptyList()) }
        var getLocation by remember { mutableStateOf(false) }
        var employeeList by remember { mutableStateOf<List<Employee>>(emptyList()) }
        var generateNumber by remember { mutableStateOf(false) }
        var departmentList by remember {
            mutableStateOf<List<String>>(
                listOf(
                    "Exploration",
                    "Production",
                    "Transportation",
                    "R&D",
                    "Distribution",
                    "QHSE"
                )
            )
        }
        var lastNumber by remember { mutableStateOf(0) }
        var departmentPart by remember { mutableStateOf("") }
        var assetGroupPart by remember { mutableStateOf("") }
        var uniquePart by remember { mutableStateOf("") }

        var assetGroupList by remember {
            mutableStateOf<List<String>>(
                listOf(
                    "Hydraulic",
                    "Electrical",
                    "Mechanical "
                )
            )
        }


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

        LaunchedEffect(Unit) {

            withContext(Dispatchers.IO)
            {
                val httpgetemployees = httpgetemployees()
                val employees = httpgetemployees.getEmployees()
                if (employees != null) {
                    employeeList = employees
                }
            }
        }


        if (getLocation) {
            LaunchedEffect(Unit) {

                withContext(Dispatchers.IO)
                {
                    val httpgetlocations = httpgetlocations()
                    val locations = httpgetlocations.getLocations(department)
                    if (locations != null) {
                        locationList = locations
                    }
                    getLocation = false
                }
            }
        }

        fun generateAssetSN(departmentId: Int, assetGroup: String): String {
            val assetgroupDict = mapOf(
                1 to "Hydraulic",
                3 to "Electrical",
                4 to "Mechanical "
            )
            departmentPart = departmentId.toString().padStart(2, '0')
            assetGroupPart =
                assetgroupDict.entries.find { it.value == assetGroup }?.key?.toString()
                    ?.padStart(2, '0') ?: "00"
            var intassetGroup = assetgroupDict.entries.find { it.value == assetGroup }?.key?.toInt() ?: 0
            if (generateNumber) {
                val sharedPreferences =
                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                lastNumber = sharedPreferences.getInt("last_number_${departmentPart}_${intassetGroup}", 0)
                newNumber = lastNumber + 1
                generateNumber = false
            }

            val uniquePart = newNumber.toString().padStart(4, '0')
            assetSN = "$departmentPart/$assetGroupPart/$uniquePart"
            return "$departmentPart/$assetGroupPart/$uniquePart"
        }


        fun generateImageUri(context: Context): Uri {
            val imageFile = File(context.getExternalFilesDir(null), "${UUID.randomUUID()}.jpg")
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        }

        fun getByteArrayFromUri(context: Context, uri: Uri): ByteArray? {
            return try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.readBytes()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
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
                value = assetName,
                onValueChange = { assetName = it },
                label = { Text("Asset Name") },
                modifier = Modifier.width(300.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(5.dp)
            ) {
                DropDownMenu(
                    departmentList, "Department", selectedItem = department, width = 200
                ) {
                    department = it
                    getLocation = true
                }

                DropDownMenu(locationList, "Location", selectedItem = location, width = 200) {
                    location = it
                }

            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(10.dp)
            ) {
                DropDownMenu(
                    assetGroupList, "Asset Group", selectedItem = assetGroup, width = 200
                ) {
                    assetGroup = it
                    generateNumber = true
                }
                DropDownMenu(employeeList.map { it.FirstName }, "Acc. Party", selectedItem = accountableParty, width = 200) {
                    accountableParty = it
                }

            }
            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = assetDescription,
                onValueChange = { assetDescription = it },
                label = { Text("Asset Description") },
                modifier = Modifier.width(300.dp),
            )
            Spacer(modifier = Modifier.height(2.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(5.dp)
            ) {
                DatePickerDocked("startDate", "Start Date", expiredWarranty) {
                    expiredWarranty = it

                }
                Text(
                    text = "Asset SN: \n" + generateAssetSN(
                        departmentList.indexOf(department) + 1,
                        assetGroup
                    )
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Button(onClick = {
                        galleryLauncher.launch("image/*")
                    }) {
                        Text("Select Images")
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(onClick = {
                        try {
                            val generatedUri = generateImageUri(context)
                            cameraImageUri = generatedUri
                            cameraLauncher.launch(generatedUri)
                        } catch (e: Exception) {
                            Log.d("kilo", "Error: ${e.message}")
                            e.printStackTrace()
                        }

                    }) {
                        Text("Capture Image")
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Absolute.Right,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Button(onClick = {
                        navController.navigate("Assets")
                    }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Toast.LENGTH_SHORT
                    Button(onClick = {
                        try {
                            var imageByteArrays = imageUris.mapNotNull { uri ->
                                getByteArrayFromUri(context, uri)
                            }

                            val base64Images = imageByteArrays.map {
                                Base64.encodeToString(it, Base64.NO_WRAP)
                            }

                            val assetgroupDict = mapOf(
                                1 to "Hydraulic",
                                3 to "Electrical",
                                4 to "Mechanical "
                            )

                            val assetGroupPart =
                                assetgroupDict.entries.find { it.value == assetGroup }?.key

                            var asset = createNewAsset(
                                assetSN,
                                assetName,
                                (departmentList.indexOf(department) + 1),
                                location,
                                employeeList.find { it.FirstName == accountableParty }!!.ID,
                                (assetGroupPart?.toInt() ?: 0),
                                assetDescription,
                                expiredWarranty,
                                base64Images
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                val httppostasset = httppostasset()
                                httppostasset.postAsset(asset,
                                    {
                                        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                        sharedPreferences.edit().putInt("last_number_${departmentPart}_${assetGroupPart}", newNumber).apply()
                                        navController.navigate("Assets")
                                        Toast.makeText(context, "Asset registered successfully", Toast.LENGTH_SHORT).show()
                                    },
                                    {
                                        Log.i("kilo", "Asset post failed")
                                        Toast.makeText(context, "Asset registration failed", Toast.LENGTH_SHORT).show()
                                    }

                                )
                            }
                        }catch (e: Exception){
                            Log.d("kilo", "Error: ${e.message}")
                            e.printStackTrace()
                        }



                    }) {
                        Text("Register")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Display the selected or captured images
                if (imageUris.isNotEmpty()) {
                    LazyColumn {
                        items(imageUris.size) { index ->
                            val uri = imageUris[index]
                            Image(
                                bitmap = MediaStore.Images.Media.getBitmap(
                                    context.contentResolver,
                                    uri
                                ).asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))


        }
    }


    @Composable
    fun EditAssets(
        navController: NavController,
        context: Context,
        assetId: Int,
        onAssetEdited: () -> Unit
    ) {
        var assetName by remember { mutableStateOf("") }
        var department by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var assetGroup by remember { mutableStateOf("") }
        var accountableParty by remember { mutableStateOf("") }
        var assetDescription by remember { mutableStateOf("") }
        var expiredWarranty by remember { mutableStateOf("") }
        var assetSN by remember { mutableStateOf("") }
        var imageUris by remember { mutableStateOf(listOf<Uri>()) }
        var locationList by remember { mutableStateOf<List<String>>(emptyList()) }
        var getLocation by remember { mutableStateOf(false) }
        var employeeList by remember { mutableStateOf<List<Employee>>(emptyList()) }
        var initialAsset by remember { mutableStateOf<EditAsset?>(null) }
        var generateNumber by remember { mutableStateOf(true) }
        var newNumber by remember { mutableStateOf(0) }
        var bitmapImage = remember { mutableStateOf<Bitmap?>(null) }
        var lastNumber by remember { mutableStateOf(0) }
        var departmentPart by remember { mutableStateOf("") }
        var assetGroupPart by remember { mutableStateOf("") }
        var uniquePart by remember { mutableStateOf("") }

        var departmentList by remember {
            mutableStateOf<List<String>>(
                listOf(
                    "Exploration",
                    "Production",
                    "Transportation",
                    "R&D",
                    "Distribution",
                    "QHSE"
                )
            )
        }
        var assetGroupList by remember {
            mutableStateOf<List<String>>(
                listOf(
                    "Hydraulic",
                    "Electrical",
                    "Mechanical "
                )
            )
        }
        var assetforupdate by remember { mutableStateOf<UpdateAsset?>(null) }
        var ImageUri by remember { mutableStateOf<Uri?>(null) }

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

        fun convertBase64StringToUri(base64String: String): Uri {
            // Step 1: Convert Base64 string to byte array
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)

            // Step 2: Create a temporary file
            val file = File(context.cacheDir, "File_${UUID.randomUUID()}.jpg")
            file.createNewFile()

            // Write byte array to file
            FileOutputStream(file).use { it.write(imageBytes) }

            // Get the file's content URI using FileProvider
            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }


        fun base64ToBitmap(base64String: String): Bitmap? {
            // Decode the Base64 string into a byte array
            return try {
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                Log.e("ImageError", "Error decoding base64 image: ${e.message}")
                null
            }

        }
        LaunchedEffect(Unit) {

            withContext(Dispatchers.IO)
            {
                val httpgetassetforedit = httpgetassetforedit()
                val asset = httpgetassetforedit.getAssetForEdit(assetId)
                if (asset != null) {
                    initialAsset = asset
                    assetName = asset.AssetName
                    department = asset.DepartmentName
                    location = asset.Location
                    assetGroup = asset.AssetGroupName
                    assetDescription = asset.Description
                    expiredWarranty = asset.WarrantyDate
                    assetSN = asset.AssetSN
                    uniquePart = asset.AssetSN.split("/")[2]

                }

                if (asset?.images.isNullOrEmpty()) {

                }
                else {
                    //val fileName = "image_${System.currentTimeMillis()}.jpg"
                    for (image in asset?.images.orEmpty()) {
                        val imageUri = convertBase64StringToUri(image)
                        imageUris = imageUris + imageUri
                    }

                }



                val httpgetemployees = httpgetemployees()
                val employees = httpgetemployees.getEmployees()
                if (employees != null) {
                    employeeList = employees
                }
            }
        }


        if (getLocation) {
            LaunchedEffect(Unit) {

                withContext(Dispatchers.IO)
                {
                    val httpgetlocations = httpgetlocations()
                    val locations = httpgetlocations.getLocations(department)
                    if (locations != null) {
                        locationList = locations
                    }
                    getLocation = false
                }
            }
        }


        fun generateAssetSN(departmentId: Int, assetGroup: String): String {
            val assetgroupDict = mapOf(
                1 to "Hydraulic",
                3 to "Electrical",
                4 to "Mechanical "
            )


            departmentPart = departmentId.toString().padStart(2, '0')
            assetGroupPart =
                assetgroupDict.entries.find { it.value == assetGroup }?.key?.toString()
                    ?.padStart(2, '0') ?: "00"
            var intassetGroup = assetgroupDict.entries.find { it.value == assetGroup }?.key?.toInt() ?: 0

            if (generateNumber) {
                val sharedPreferences =
                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                lastNumber = sharedPreferences.getInt("last_number_${departmentPart}_${intassetGroup}", 0)
                newNumber = lastNumber + 1
                generateNumber = false
            }

            val uniquePart = newNumber.toString().padStart(4, '0')
            assetSN = "$departmentPart/$assetGroupPart/$uniquePart"
            return "$departmentPart/$assetGroupPart/$uniquePart"
        }


        fun generateImageUri(context: Context): Uri {
            val imageFile = File(context.getExternalFilesDir(null), "${UUID.randomUUID()}.jpg")
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        }

        fun getByteArrayFromUri(context: Context, uri: Uri): ByteArray? {
            return try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.readBytes()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
            return try {
                when {
                    uri.scheme == "content" -> {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                    }

                    uri.scheme == "file" -> {
                        BitmapFactory.decodeFile(uri.path)
                    }

                    else -> null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Edit Assets", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = assetName,
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
                OutlinedTextField(
                    value = department,
                    onValueChange = {},
                    label = { Text("Department") },
                    readOnly = true,
                    modifier = Modifier.width(150.dp)
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = {},
                    label = { Text("Location") },
                    readOnly = true,
                    modifier = Modifier.width(150.dp)
                )

            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(10.dp)
            ) {
                OutlinedTextField(
                    value = assetGroup,
                    onValueChange = {},
                    label = { Text("Asset Group") },
                    readOnly = true,
                    modifier = Modifier.width(150.dp)
                )
                DropDownMenu(employeeList.map { it.FirstName }, "Acc. Party", accountableParty, 200) {
                    accountableParty = it
                }

            }
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = assetDescription,
                onValueChange = { assetDescription = it },
                label = { Text("Asset Description") },
                modifier = Modifier.width(150.dp),
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
                Text(
                    text = "Asset SN: \n" + generateAssetSN(
                        departmentList.indexOf(department) + 1,
                        assetGroup
                    )
                )

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
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("kilo", "Error: ${e.message}")
                    }

                }) {
                    Text("Capture Image with Camera")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Absolute.Right,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Button(onClick = {
                        navController.navigate("Assets")
                    }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = {
                            try {
                                var imageByteArrays = imageUris.mapNotNull { uri ->
                                    getByteArrayFromUri(context, uri)
                                }

                                val base64Images = if (imageByteArrays.isEmpty()) null else imageByteArrays.map {
                                    Base64.encodeToString(it, Base64.NO_WRAP)
                                }

                                assetforupdate = UpdateAsset(
                                    assetId,
                                    assetSN,
                                    assetName,
                                    initialAsset!!.DepartmentID,
                                    initialAsset!!.Location,
                                    employeeList.find { it.FirstName == accountableParty }!!.ID,
                                    (initialAsset!!.AssetGroupID),
                                    assetDescription,
                                    expiredWarranty,
                                    base64Images
                                )

                            }catch (e: Exception){
                                Log.d("kilo", "Error: ${e.message}")
                                e.printStackTrace()
                                Log.e("Error", "Exception: ${e.message}", e)
                            }
                        CoroutineScope(Dispatchers.IO).launch {

                            val httppostupdatedasset = httppostupdatedasset()
                            httppostupdatedasset.postAsset(assetforupdate,
                                {
                                    onAssetEdited()
                                    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().putInt("last_number_${departmentPart}_${assetGroupPart}", uniquePart.toInt()).apply()
                                    navController.navigate("Assets")
                                    Toast.makeText(context, "Asset updated successfully", Toast.LENGTH_SHORT).show()
                                },
                                { Log.i("kilo", "Asset post failed") }
                            )
                        }





                    }) {
                        Text("Update")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Display the selected or captured images
                if (imageUris.isNotEmpty()) {

                    LazyColumn {
                        items(imageUris.size) { index ->
                            val uri = imageUris[index]
                            val bitmap = loadBitmapFromUri(context, uri)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(8.dp)
                                )
                            } else {
                                // Display a placeholder or error message
                                Text(
                                    text = "Image not available",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(8.dp)
                                        .background(Color.Gray),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))


    }
}


@SuppressLint("UnrememberedMutableState")
@Composable
fun AssetsScreen(navController: NavController, assetChangeTrigger: MutableState<Boolean>) {
    var assetsList = rememberSaveable { mutableStateOf<List<Asset>>(emptyList()) }
    var filteredAssetsList = rememberSaveable { mutableStateOf<List<Asset>>(emptyList()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedAssetGroup by rememberSaveable { mutableStateOf("") }
    var selectedDepartment by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("") }
    var endDate by rememberSaveable { mutableStateOf("") }
    val httpgetassets = remember { httpgetassets() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showErrorMessage by remember { mutableStateOf(false) }

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
    if (assetChangeTrigger.value) {
        LaunchedEffect(Unit) {
            val fetchedAssets = withContext(Dispatchers.IO)
            { httpgetassets.getAssets() }
            assetsList.value = fetchedAssets ?: emptyList()
            filteredAssetsList.value = assetsList.value
            assetChangeTrigger.value = false
        }
    }

    LaunchedEffect(Unit) {
        val fetchedAssets = withContext(Dispatchers.IO)
        { httpgetassets.getAssets() }
        assetsList.value = fetchedAssets ?: emptyList()
        filteredAssetsList.value = assetsList.value

    }

    if (showErrorMessage)
    {
        Toast.makeText(LocalContext.current, "End date cannot be before start date", Toast.LENGTH_SHORT).show()
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
                    ), "Department", selectedItem = selectedDepartment, width = 200
                ) {
                    selectedDepartment = it
                    filterAssets()
                }
                DropDownMenu(listOf("Hydraulic", "Electrical", "Mechanical "), "Asset Group", selectedItem = selectedAssetGroup, width = 200) {
                    selectedAssetGroup = it
                    filterAssets()
                }

            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DatePickerDocked("startDate", startDate, startDate) {
                    startDate = it
                    filterAssets()

                }
                DatePickerDocked("endDate", endDate, endDate) {
                    var previousEndDate = endDate
                    endDate = it
                    if (endDate >= startDate) {
                        filterAssets()
                    } else {
                        showErrorMessage = true
                        endDate = previousEndDate
                        // Handle the case where end date is before start date
                    }
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
        else
        {
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Assets List", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(10.dp))


        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp) // Add padding to avoid covering the button
            ) {
                items(filteredAssetsList.value) { asset ->
                    AssetCard(asset, navController){
                        filterAssets()
                    }
                }
                filterAssets()

            }
            Text(
                text = "Showing ${filteredAssetsList.value.size} of ${assetsList.value.size} records",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Toast.LENGTH_SHORT
            FloatingActionButton(
                onClick = {
                    /* TODO: Add action here */
                    navController.navigate("RegisterAssets")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

    }
}

@Composable
fun AssetCard(asset: Asset, navController: NavController, onLandScape: (String) -> Unit) {
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
            Text(text = asset.AssetSN, color = Color.Red ,modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(50.dp))
            IconButton(onClick = {
                navController.navigate("EditAssets/${asset.ID}")
            }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Move")
            }
        }
        onLandScape(asset.AssetSN)

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

                    IconButton(onClick = {
                        navController.navigate("EditAssets/${asset.ID}")
                    }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = {
                        navController.navigate("TransferAsset/${asset.ID}")
                    }) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Move")
                    }
                    IconButton(onClick = {
                        navController.navigate("TransferHistory/${asset.ID}")

                    }) {
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
    var selectedDateText by remember { mutableStateOf(selectedDate) }

    Box(
        modifier = Modifier
            .width(200.dp)
            .padding(5.dp)
    ) {
        OutlinedTextField(
            value = selectedDateText,
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
fun DropDownMenu(items: List<String>, name: String, selectedItem: String,width: Int, onItemSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .width(width.dp)
            .padding(5.dp) // Adjust the width as needed
    ) {
        TextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            label = { Text(name) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
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
        val navController = rememberNavController()

        //AssetsScreen(navController, assetChangeTrigger)
        //,context = LocalContext.current
    }
}
