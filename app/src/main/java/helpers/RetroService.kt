package helpers

import retrofit2.Call
import retrofit2.http.*

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

    @PUT("pins/{pin_id}")
    fun updatePin(@Path("pin_id") pin_id: String, @Body params: Pin): Call<PinResponse>

    @DELETE("pins/{pin_id}")
    fun deletePin(@Path("pin_id") pin_id: String): Call<PinResponse>
}