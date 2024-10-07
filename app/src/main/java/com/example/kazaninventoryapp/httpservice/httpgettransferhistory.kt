package com.example.kazaninventoryapp.httpservice

import com.example.kazaninventoryapp.Models.Asset
import com.example.kazaninventoryapp.Models.TransferAsset
import com.example.kazaninventoryapp.Models.TransferHistory
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class httpgettransferhistory {
    fun GetHistory(assetId:Int): MutableList<TransferHistory?>? {
        val url = URL("http://10.0.2.2:5232/api/Asset/transferhistory/$assetId")

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
                val assetsHistoryList = mutableListOf<TransferHistory?>()
                for (i in 0 until jsonArray.length()) {
                    val assetObject = jsonArray.getJSONObject(i)
                    val asset = TransferHistory(
                        assetObject.getInt("id"),
                        assetObject.getString("transferDate"),
                        assetObject.getString("fromAssetSn"),
                        assetObject.getString("toAssetSn"),
                        assetObject.getString("oldDepartment"),
                        assetObject.getString("newDepartment")
                    )
                    assetsHistoryList.add(asset)
                }

                return assetsHistoryList
            }

            con.disconnect()
        } catch (e: Exception) {
            return null
        }

        return null


    }
}