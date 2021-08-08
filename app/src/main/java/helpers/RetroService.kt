package helpers

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetroService {

    @GET("pins")
//    @Headers(
//        "Accept: application/json",
//        "Content-Type: application/json")
    fun getPinsList(): Call<List<Pin>>

    @POST("pins")
//    @Headers(
//        "Accept: application/json",
//        "Content-Type: application/json")
//    // Can add authorization to 'Headers' section if needed
    fun createPin(@Body params: Pin): Call<PinResponse>
}