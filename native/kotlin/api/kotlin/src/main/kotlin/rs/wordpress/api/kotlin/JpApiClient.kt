package rs.wordpress.api.kotlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.wp_api.ParsedUrl
import uniffi.wp_api.RequestExecutor
import uniffi.wp_api.UniffiWpApiClient
import uniffi.wp_api.WpApiException
import uniffi.wp_api.WpAuthentication

class JetpackApiClient
@Throws(WpApiException::class)
constructor(
    siteUrl: ParsedUrl,
    authentication: WpAuthentication,
    private val requestExecutor: RequestExecutor = JpRequestExecutor(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    // Don't expose `WpRequestBuilder` directly so we can control how it's used
    private val requestBuilder by lazy {
        UniffiWpApiClient(siteUrl, authentication, requestExecutor)
    }

    // Provides the _only_ way to execute authenticated requests using our Kotlin wrapper.
    //
    // It makes sure that the errors are wrapped in `JpRequestResult` type instead of forcing
    // clients to try/catch the errors.
    //
    // It'll also help make sure any breaking changes to the API will end up as a compiler error.
    suspend fun <T> request(
        executeRequest: suspend (UniffiWpApiClient) -> T
    ): JpRequestResult<T> = withContext(dispatcher) {
        try {
            JpRequestResult.WpRequestSuccess(data = executeRequest(requestBuilder))
        } catch (exception: WpApiException) {
            when (exception) {
                is WpApiException.InvalidHttpStatusCode -> JpRequestResult.InvalidHttpStatusCode(
                    statusCode = exception.statusCode,
                )
                is WpApiException.RequestExecutionFailed -> JpRequestResult.RequestExecutionFailed(
                    statusCode = exception.statusCode,
                    reason = exception.reason
                )
                is WpApiException.MediaFileNotFound -> JpRequestResult.MediaFileNotFound(
                    filePath = exception.filePath
                )
                is WpApiException.ResponseParsingException -> JpRequestResult.ResponseParsingError(
                    reason = exception.reason,
                    response = exception.response,
                )
                is WpApiException.SiteUrlParsingException -> JpRequestResult.SiteUrlParsingError(
                    reason = exception.reason,
                )
                is WpApiException.UnknownException -> JpRequestResult.UnknownError(
                    statusCode = exception.statusCode,
                    response = exception.response,
                )
                is WpApiException.WpException -> JpRequestResult.WpError(
                    errorCode = exception.errorCode,
                    errorMessage = exception.errorMessage,
                    statusCode = exception.statusCode,
                    response = exception.response,
                )
            }
        }
    }
}
