package com.dicoding.storyapp.helper

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun localizeDate(utcDate: String): String {
        return try {
            val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            utcFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date: Date? = utcFormat.parse(utcDate)

            val localFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            localFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

            localFormat.format(date ?: return utcDate)
        } catch (e: Exception) {
            e.printStackTrace()
            utcDate
        }
    }
}
