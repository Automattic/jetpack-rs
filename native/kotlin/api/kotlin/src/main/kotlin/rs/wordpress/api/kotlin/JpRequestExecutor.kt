package rs.wordpress.api.kotlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import uniffi.jetpack_api.JetpackNetworkResponse
import uniffi.jetpack_api.JetpackRequestExecutor
import uniffi.wp_api.WpNetworkHeaderMap
import uniffi.wp_api.WpNetworkRequest
import uniffi.wp_api.WpNetworkResponse

class JpRequestExecutor(
    private val okHttpClient: OkHttpClient = OkHttpClient(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : JetpackRequestExecutor {

    override suspend fun execute(request: WpNetworkRequest): JetpackNetworkResponse =
        withContext(dispatcher) {
            val requestBuilder = Request.Builder().url(request.url())
            requestBuilder.method(
                request.method().toString(),
                request.body()?.contents()?.toRequestBody()
            )
            request.headerMap().toMap().forEach { (key, values) ->
                values.forEach { value ->
                    requestBuilder.addHeader(key, value)
                }
            }

            okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
                return@withContext JetpackNetworkResponse(
                    WpNetworkResponse(
                        body = response.body?.bytes() ?: ByteArray(0),
                        statusCode = response.code.toUShort(),
                        headerMap = WpNetworkHeaderMap.fromMultiMap(response.headers.toMultimap())
                    )
                )
            }
        }
}
