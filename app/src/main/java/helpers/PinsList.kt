package helpers

// this expects an object called results who's value is a list not a straight list
data class PinsList(val results: List<Pin>)
data class Pin(val id: Int, val lat_lon: String?)
//data class PinResponse(val code: Int?, val meta: String?, val data: Pin?)