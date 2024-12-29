package com.example.calendarapp;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;
import java.util.concurrent.TimeUnit;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class WeatherService {
    private static final String TAG = "WeatherService";
    // 修正中央氣象署的正確 API URL
    private static final String BASE_URL = "https://opendata.cwa.gov.tw/";
    private static final String API_KEY = "CWA-7A63821A-3562-41B0-B1AA-5F3CDD674F3D";
    private static WeatherService instance;
    private final ApiService service;

    private WeatherService() {
        // 添加 OkHttp 客戶端配置
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                Log.d(TAG, "OkHttp: " + message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ApiService.class);
    }

    public static synchronized WeatherService getInstance() {
        if (instance == null) {
            instance = new WeatherService();
        }
        return instance;
    }

    public ApiService getService() {
        return service;
    }

    public interface ApiService {
        @GET("api/v1/rest/datastore/F-D0047-063")
        Call<WeatherResponse> getWeather(
                @Query("Authorization") String authorization,
                @Query("locationName") String locationName,
                @Query("elementName") String elementName
        );
    }

    public String getApiKey() {
        return API_KEY;
    }


    public static class WeatherResponse {
        @SerializedName("success")
        private String success;

        @SerializedName("records")
        private Records records;

        public String getSuccess() {
            return success;
        }

        public Records getRecords() {
            return records;
        }

        public static class Records {
            @SerializedName("Locations")
            private List<Location> locations;

            public List<Location> getLocations() {
                return locations;
            }
        }

        public static class Location {
            @SerializedName("Location")
            private List<LocationData> locationData;

            public List<LocationData> getLocationData() {
                return locationData;
            }
        }

        public static class LocationData {
            @SerializedName("LocationName")
            private String locationName;

            @SerializedName("WeatherElement")
            private List<WeatherElement> weatherElements;

            public String getLocationName() {
                return locationName;
            }

            public List<WeatherElement> getWeatherElements() {
                return weatherElements;
            }
        }

        public static class ElementValue {
            @SerializedName("value")
            private String value;

            // 新增這些欄位來匹配 JSON 回應
            @SerializedName("Temperature")
            private String temperature;

            @SerializedName("MaxTemperature")
            private String maxTemperature;

            @SerializedName("MinTemperature")
            private String minTemperature;

            public String getValue() {
                // 如果一般的 value 為空，嘗試返回其他溫度值
                if (value == null) {
                    if (temperature != null) return temperature;
                    if (maxTemperature != null) return maxTemperature;
                    if (minTemperature != null) return minTemperature;
                }
                return value;
            }
        }
        public static class WeatherElement {
            @SerializedName("ElementName")
            private String elementName;

            @SerializedName("Time")
            private List<TimeData> time;

            public String getElementName() {
                return elementName;
            }

            public List<TimeData> getTime() {
                return time;
            }
        }
        public static class TimeData {
            @SerializedName("StartTime")
            private String startTime;

            @SerializedName("EndTime")
            private String endTime;

            @SerializedName("ElementValue")
            private List<ElementValue> elementValues;

            public String getStartTime() {
                return startTime;
            }

            public String getEndTime() {
                return endTime;
            }

            public List<ElementValue> getElementValues() {
                return elementValues;
            }
        }


    }
}
