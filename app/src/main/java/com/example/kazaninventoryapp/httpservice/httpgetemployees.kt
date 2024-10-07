package com.example.kazaninventoryapp.httpservice

import com.example.kazaninventoryapp.Models.Employee
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class httpgetemployees {
    fun getEmployees(): MutableList<Employee>? {
        val url = URL("http://10.0.2.2:5232/api/Asset/employee")
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
                val employeeList: MutableList<Employee> = mutableListOf<Employee>()
                for (i in 0 until jsonArray.length()) {
                    val employeeObject = jsonArray.getJSONObject(i)
                    val employee = Employee(
                        employeeObject.getInt("id"),
                        employeeObject.getString("firstName"),

                    )
                    employeeList.add(employee)
                }

                return employeeList
            }

            con.disconnect()
        } catch (e: Exception) {
            return null
        }

        return null


    }
}