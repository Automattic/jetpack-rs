package rs.jetpack.api.kotlin

import uniffi.wp_api.WpErrorCode

fun <T> JpRequestResult<T>.assertSuccess() {
    assert(this is JpRequestResult.JpRequestSuccess)
}

fun <T> JpRequestResult<T>.assertSuccessAndRetrieveData(): T {
    assert(this is JpRequestResult.JpRequestSuccess)
    return (this as JpRequestResult.JpRequestSuccess).data
}

fun <T> JpRequestResult<T>.wpErrorCode(): WpErrorCode {
    assert(this is JpRequestResult.WpError)
    return (this as JpRequestResult.WpError).errorCode
}
