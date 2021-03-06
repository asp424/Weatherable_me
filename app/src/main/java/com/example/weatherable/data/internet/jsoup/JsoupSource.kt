package com.example.weatherable.data.internet.jsoup

import com.example.weatherable.data.view_states.InternetResponse
import com.example.weatherable.utilites.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class JsoupSource {
    suspend fun getCityValues(): InternetResponse =
        suspendCoroutine { continuation ->
            CoroutineScope(Dispatchers.Default).launch {
                continuation.resume(
                    InternetResponse.OnSuccess(
                        JSONObject()
                            .put("nov_value", getCityValue(NOV_URL))
                            .put("ana_value", getCityValue(ANA_URL))
                            .put("gel_value", getCityValue(GEL_URL))
                            .put("yan_temp", getOnSitesTemps(YAN_URL, CLASS_TEMP, 1))
                            .put("hydro_temp", getOnSitesTemps(GID_URL, TAG, 10, 1))
                            .put("gis_temp", getOnSitesTemps(KRM_URL, CLASS_CITY))
                            .put("krm_wind", getOnSitesTemps(KRM_URL, CLASS_WIND))
                            .put("chel_temp", getOnSitesTemps(CHL_URL, CLASS_CITY))
                    )
                )
            }
        }

    private suspend fun getCityValue(city: String): JSONObject =
        suspendCoroutine { continuation ->
            runCatching {
                try {
                    Jsoup.connect(city).get().apply {
                        continuation.resume(
                            JSONObject()
                                .put("temp", getElementsByClass(CLASS_CITY)[0]?.text())
                                .put("water", getElementsByClass(CLASS_INFO)[8]?.text())
                        )
                    }
                } catch (e: Exception) {
                    continuation.resume(
                        JSONObject()
                            .put("temp", "Err")
                            .put("water", "Err")
                    )
                }
            }
        }
    suspend fun getGisData(): InternetResponse =
        suspendCoroutine { continuation ->
            CoroutineScope(Dispatchers.Default).launch {
                continuation.resume(
                    InternetResponse.OnSuccess(
                        JSONObject()
                            .put("gis_temp_tod", getOnSitesTemps(GIS_URL_TOD, GIS_TEMP_TOD, 0, 4))
                            .put("gis_icon_tod", getOnSitesTemps(GIS_URL_TOD, GIS_ICON_LIST, 0, 5))
                            .put("gis_temp_tom", getOnSitesTemps(GIS_URL_TOM, GIS_TEMP_TOD, 0, 4))
                            .put("gis_icon_tom", getOnSitesTemps(GIS_URL_TOM, GIS_ICON_LIST, 0, 5))
                            .put("gis_sun_up", getOnSitesTemps(KRM_URL, GIS_SUN_UP, 0))
                            .put("gis_sun_down", getOnSitesTemps(KRM_URL, GIS_SUN_UP, 1))
                    )
                )
            }
        }

    suspend fun getYanData(): InternetResponse =
        suspendCoroutine { continuation ->
            CoroutineScope(Dispatchers.Default).launch {
                continuation.resume(
                    InternetResponse.OnSuccess(
                        JSONObject().apply {
                            for (i in 0..3) {
                                put("yan_temp_tod$i", valueT(i))
                                put("yan_temp_rain$i", valueR(i))
                            }
                            for (i in 4..7) {
                                put("yan_temp_tom$i", valueT(i))
                                put("yan_temp_rain_t$i", valueR(i))
                            }
                        })
                )
            }
        }

    private suspend fun valueT(i: Int) = getOnSitesTemps(YAN_URL_DETAILS, YAN_TOD_DETAIL_TEMP, i)
    private suspend fun valueR(i: Int) = getOnSitesTemps(YAN_URL_DETAILS, YAN_TOD_DETAIL_RAIN, i)
}

suspend fun getOnSitesTemps(
    url: String,
    classOrTag: String,
    index: Int = 0,
    flag: Int = 0
): String? = suspendCoroutine { continuation ->
    runCatching {
        when (flag) {
            0 -> Jsoup.connect(url).get()
                .getElementsByClass(classOrTag)[index]?.text()
            1 -> Jsoup.connect(url).get()
                .getElementsByTag(classOrTag)[index]?.text()
            2 -> Jsoup.connect(url).get()
                .getElementsByTag(classOrTag)[index]
            3 -> Jsoup.connect(url).get()
                .getElementsByTag(classOrTag)
            4 -> Jsoup.connect(url).get()
                .getElementsByClass(classOrTag).text()
            5 -> Jsoup.connect(url).get()
                .getElementsByClass(classOrTag)
            else -> {
            }
        }
    }.onSuccess {
        continuation.resume(it.toString())
    }.onFailure {
        continuation.resume("Err")
    }
}