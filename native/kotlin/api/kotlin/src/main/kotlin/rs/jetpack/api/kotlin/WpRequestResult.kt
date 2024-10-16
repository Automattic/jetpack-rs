package rs.jetpack.api.kotlin

import uniffi.wp_api.WpErrorCode

// TODO: If we have a shared base module between wp_api & jp_api, we could use the `WpRequestResult`
sealed class JpRequestResult<T> {
    class JpRequestSuccess<T>(val data: T) : JpRequestResult<T>()
    class WpError<T>(
        val errorCode: WpErrorCode,
        val errorMessage: String,
        val statusCode: UShort,
        val response: String,
    ) : JpRequestResult<T>()

    class InvalidHttpStatusCode<T>(
        val statusCode: UShort
    ) : JpRequestResult<T>()

    class RequestExecutionFailed<T>(
        val statusCode: UShort?,
        val reason: String,
    ) : JpRequestResult<T>()

    class SiteUrlParsingError<T>(
        val reason: String,
    ) : JpRequestResult<T>()

    class ResponseParsingError<T>(
        val reason: String,
        val response: String,
    ) : JpRequestResult<T>()

    class UnknownError<T>(
        val statusCode: UShort,
        val response: String,
    ) : JpRequestResult<T>()
}
