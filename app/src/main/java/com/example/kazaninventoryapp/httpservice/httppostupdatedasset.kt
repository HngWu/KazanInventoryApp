package com.example.kazaninventoryapp.httpservice

import android.os.Handler
import android.os.Looper
import com.example.kazaninventoryapp.Models.UpdateAsset
import com.example.kazaninventoryapp.Models.createNewAsset
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class httppostupdatedasset {
    fun postAsset(asset: UpdateAsset?, onSuccess: (Boolean) -> Unit, onFailure: (Throwable) -> Unit) {


        val url = URL("http://10.0.2.2:5232/api/Asset/updateasset")
        try {
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "application/json; utf-8")
            con.setRequestProperty("Accept", "application/json")
            con.doOutput = true


            val json = Json.encodeToString(asset)
            val os = OutputStreamWriter(con.outputStream)

            os.write(json)
            os.flush()
            os.close()

            val status = con.responseCode
            if (status == 200) {

                Handler(Looper.getMainLooper()).post() {
                    onSuccess(true)
                }
            } else {
                Handler(Looper.getMainLooper()).post{
                    onFailure(Throwable("Post asset failed"))
                }
            }



        }catch (e: Exception) {
            onFailure(e)
        }

    }
}