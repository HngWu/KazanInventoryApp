package com.example.kazaninventoryapp.httpservice

import com.example.kazaninventoryapp.Models.Asset
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

//TODO 1: Implement the clear button for the search bar correctly by clearing the search query
//TODO 2: Implement the landscape mode for the app where change in orientation does not affect the filters
//TODO 3: Implement the generation of the new asset serial number when transferring an asset correctly
//TODO 4: Implement the snackbar for the app
//TODO 5: Implement display of multiple images correctly
// git pull from laptop first


class httpgetassets {
    fun getAssets(): MutableList<Asset>? {
        val url = URL("http://10.0.2.2:5232/api/Asset")

        try {
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.setRequestProperty("Content-Type", "application/json; utf-8")
            con.setRequestProperty("Accept", "application/json")



            if (con.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(con.inputStream))
                val jsonData = reader.use { it.readText() }
//                var line: String?
//                while (reader.readLine().also { line = it } != null) {
//                    jsonData.append(line)
//                }
                reader.close()
                val jsonArray = JSONArray(jsonData)
                val assetsList = mutableListOf<Asset>()
                for (i in 0 until jsonArray.length()) {
                    val assetObject = jsonArray.getJSONObject(i)
                    val asset = Asset(
                        assetObject.getInt("id"),
                        assetObject.getString("assetSn"),
                        assetObject.getString("assetName"),
                        assetObject.getInt("departmentLocationId"),
                        assetObject.getInt("employeeId"),
                        assetObject.getInt("assetGroupId"),
                        assetObject.getString("description"),
                        assetObject.getString("warrantyDate"),
                        assetObject.getString("departmentName"),
                        assetObject.getString("assetGroupName"),
                        )
                    assetsList.add(asset)
                }

                return assetsList
            }

            con.disconnect()
        } catch (e: Exception) {
            return null
        }

        return null


    }
}