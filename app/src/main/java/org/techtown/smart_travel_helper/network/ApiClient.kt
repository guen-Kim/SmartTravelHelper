package org.techtown.smart_travel_helper.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.techtown.smart_travel_helper.model.SearchPoi
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiClient {
    @GET("pois?searchKeyword=%ED%9C%B4%EA%B2%8C%EC%86%8C&version=1&searchType=all&searchtypCd=R&centerLon=126.98502043&centerLat=37.5664821&reqCoordType=WGS84GEO&resCoordType=WGS84GEO&page=1&count=10&multiPoint=N&poiGroupYn=N")
    fun getSearchROI(
        @Query("searchKeyword") searchKeyword: String,
        @Query("radius") radius: String,
        @Query("appKey") appKey: String,


        )
            : Call<SearchPoi>// json 최상위 키 리스트 아님!


    companion object {
        val baseUrl = "https://apis.openapi.sk.com/tmap/"

        //// 없으면 생성
        //        if(instance == null)
        fun create(): ApiClient {

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create()) // 통신 성공 시 응답객체 body 별도 변환 x json to obj
                .client(getOkHttpClient())
                .build()
                .create(ApiClient::class.java) // 인터페이스 구현체 반환
        }

        private fun getOkHttpClient() : OkHttpClient{

            //OkHttp HttpLoggingInterceptor
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            //OkHttp client 생성
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(2000, TimeUnit.MILLISECONDS) //커넥션 작업의 타임아웃
                .readTimeout(2000,TimeUnit.MILLISECONDS)    // 읽기 작업의 타임아웃
                .writeTimeout(2000,TimeUnit.MILLISECONDS)   // 쓰기 작업의 타임아웃
                .retryOnConnectionFailure(false) //실패할 경우 다시 시도


            return client.build()
        }


    }
}