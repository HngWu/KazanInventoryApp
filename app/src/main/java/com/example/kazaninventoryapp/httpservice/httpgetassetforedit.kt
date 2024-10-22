package com.example.kazaninventoryapp.httpservice

import com.example.kazaninventoryapp.Models.Asset
import com.example.kazaninventoryapp.Models.EditAsset
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class httpgetassetforedit {
    fun getAssetForEdit(id: Int): EditAsset? {
        // This function is not implemented yet
        val url = URL("http://10.0.2.2:5232/api/Asset/edit/$id")
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
                val jsonObject = JSONObject(jsonData)

                val imagesJsonArray = jsonObject.getJSONArray("images")
                val imagesList = mutableListOf<String>()
                for (i in 0 until imagesJsonArray.length()) {
                    imagesList.add(imagesJsonArray.getString(i))
                }

                val asset = EditAsset(
                    jsonObject.getInt("id"),
                    jsonObject.getString("assetSN"),
                    jsonObject.getString("assetName"),
                    jsonObject.getInt("departmentID"),
                    jsonObject.getInt("employeeID"),
                    jsonObject.getInt("assetGroupID"),
                    jsonObject.getString("description"),
                    jsonObject.getString("warrantyDate"),
                    jsonObject.getString("departmentName"),
                    jsonObject.getString("assetGroupName"),
                    jsonObject.getString("location"),
                    imagesList
                )
                return asset
            }

            con.disconnect()
        } catch (e: Exception) {
            return null
        }

        return null

    }




}