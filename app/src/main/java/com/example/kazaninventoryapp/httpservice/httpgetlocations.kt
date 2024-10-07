package com.example.kazaninventoryapp.httpservice

import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class httpgetlocations {

    fun getLocations(department: String): MutableList<String>? {
        val url = URL("http://10.0.2.2:5232/api/Asset/locations/$department")
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
                val locationArray = JSONArray(jsonData)
                val locationList = mutableListOf<String>()
                for (i in 0 until locationArray.length()) {
                    locationList.add(locationArray[i].toString())
                }

                return locationList
            }

            con.disconnect()
        } catch (e: Exception) {
            return null
        }

        return null


    }

}