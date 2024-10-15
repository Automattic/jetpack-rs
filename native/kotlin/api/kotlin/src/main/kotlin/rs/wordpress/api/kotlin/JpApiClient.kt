package rs.wordpress.api.kotlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.jetpack_api.JetpackApiException
import uniffi.jetpack_api.JetpackRequestExecutor
import uniffi.jetpack_api.UniffiJetpackClient
import uniffi.wp_api.ParsedUrl
import uniffi.wp_api.WpAuthentication

class JetpackApiClient
@Throws(JetpackApiException::class)
constructor(
    siteUrl: ParsedUrl,
    authentication: WpAuthentication,
    private val requestExecutor: JetpackRequestExecutor = JpRequestExecutor(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // Don't expose `WpRequestBuilder` directly so we can control how it's used
    private val requestBuilder by lazy {
        UniffiJetpackClient(siteUrl, authentication, requestExecutor)
    }

    // Provides the _only_ way to execute authenticated requests using our Kotlin wrapper.
    //
    // It makes sure that the errors are wrapped in `JpRequestResult` type instead of forcing
    // clients to try/catch the errors.
    //
    // It'll also help make sure any breaking changes to the API will end up as a compiler error.
    suspend fun <T> request(
        executeRequest: suspend (UniffiJetpackClient) -> T
    ): JpRequestResult<T> = withContext(dispatcher) {
        try {
            JpRequestResult.JpRequestSuccess(data = executeRequest(requestBuilder))
        } catch (exception: JetpackApiException) {
            when (exception) {
                is JetpackApiException.InvalidHttpStatusCode -> JpRequestResult.InvalidHttpStatusCode(
                    statusCode = exception.statusCode,
                )
                is JetpackApiException.RequestExecutionFailed -> JpRequestResult.RequestExecutionFailed(
                    statusCode = exception.statusCode,
                    reason = exception.reason
                )
                is JetpackApiException.ResponseParsingException -> JpRequestResult.ResponseParsingError(
                    reason = exception.reason,
                    response = exception.response,
                )
                is JetpackApiException.SiteUrlParsingException -> JpRequestResult.SiteUrlParsingError(
                    reason = exception.reason,
                )
                is JetpackApiException.UnknownException -> JpRequestResult.UnknownError(
                    statusCode = exception.statusCode,
                    response = exception.response,
                )
                is JetpackApiException.WpException -> JpRequestResult.WpError(
                    errorCode = exception.errorCode,
                    errorMessage = exception.errorMessage,
                    statusCode = exception.statusCode,
                    response = exception.response,
                )
            }
        }
    }
}
