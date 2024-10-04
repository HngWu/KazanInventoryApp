package com.example.kazaninventoryapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavHost(navController, "Assets") {
                composable("Assets") { AssetsScreen() }
            }
        }
    }
}



@SuppressLint("UnrememberedMutableState")
@Composable
fun AssetsScreen()
{
    var assetsList = remember { mutableStateOf<List<Asset>>(emptyList()) }
    var filteredAssetsList = remember { mutableStateOf<List<Asset>>(emptyList()) }
    var searchQuery by mutableStateOf("")
    var selectedAssetGroup by mutableStateOf("")
    var selectedDepartment by mutableStateOf("")
    var startDate by mutableStateOf("")
    var endDate by mutableStateOf("")
    val httpgetassets = remember { httpgetassets() }
    fun filterAssets() {
        filteredAssetsList.value = assetsList.value.filter { asset ->
            (searchQuery.isEmpty() || asset.AssetSN.contains(searchQuery, ignoreCase = true) || asset.AssetName.contains(searchQuery, ignoreCase = true)) &&
                    (selectedAssetGroup.isEmpty() || asset.AssetGroupName == selectedAssetGroup) &&
                    (selectedDepartment.isEmpty() || asset.DepartmentName == selectedDepartment) &&
                    (startDate.isEmpty() || asset.WarrantyDate >= startDate) &&
                    (endDate.isEmpty() || asset.WarrantyDate <= endDate)
        }
    }
    LaunchedEffect(Unit) {
        val fetchedAssets = withContext(Dispatchers.IO)
        {httpgetassets.getAssets()}
        assetsList.value = fetchedAssets ?: emptyList()
        filteredAssetsList.value = assetsList.value

    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DropDownMenu(listOf("Exploration", "Production", "Transportation", "R&D", "Distribution", "QHSE"), "Department") {
                selectedDepartment = it
                filterAssets()
            }
            DropDownMenu(listOf("Toyota Hilux FAF321", "Suction Line 852", "ZENY 3,5CFM Single-Stage 5 Pa Rotary Vane", "Volvo FH16"), "Asset Group") {
                selectedAssetGroup = it
                filterAssets()
            }

        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DatePickerDocked("startDate","Start Date", startDate ) {
                startDate = it
                filterAssets()

            }
            DatePickerDocked("endDate","End Date", endDate) {
                endDate = it
                filterAssets()
            }
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (searchQuery.length > 2) {
                    filterAssets()
                }
            },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )
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

    }
}

@Composable
fun AssetCard(asset: Asset) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(5.dp)
            .background(MaterialTheme.colorScheme.surface)
            .shadow(elevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = asset.AssetName)
            Text(text = asset.DepartmentName)
            Text(text = asset.AssetSN)

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDocked(identifier: String, selectedDate: String, label: String,onDateSelected: (String) -> Unit) {
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
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
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
fun DropDownMenu(items: List<String>, name: String,onItemSelected: (String) -> Unit)
{
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
                        selectedItem = item
                        expanded = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }

}

@Composable
fun DropDownMenu1() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("This is a text")

        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KazanInventoryAppTheme {
        Column {
            AssetsScreen()
        }
    }
}