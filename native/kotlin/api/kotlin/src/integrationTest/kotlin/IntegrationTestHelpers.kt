package rs.jetpack.api.kotlin

import rs.wordpress.api.kotlin.WpRequestResult
import uniffi.wp_api.WpErrorCode

fun <T> WpRequestResult<T>.assertSuccess() {
    assert(this is WpRequestResult.WpRequestSuccess)
}

fun <T> WpRequestResult<T>.assertSuccessAndRetrieveData(): T {
    assert(this is WpRequestResult.WpRequestSuccess)
    return (this as WpRequestResult.WpRequestSuccess).data
}

fun <T> WpRequestResult<T>.wpErrorCode(): WpErrorCode {
    assert(this is WpRequestResult.WpError)
    return (this as WpRequestResult.WpError).errorCode
}
