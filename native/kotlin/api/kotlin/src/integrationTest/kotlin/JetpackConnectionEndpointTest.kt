package rs.jetpack.api.kotlin

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import uniffi.wp_api.ParsedUrl
import uniffi.wp_api.wpAuthenticationFromUsernameAndPassword

class JetpackConnectionEndpointTest {
    // These credentials are from a throwaway site
    private val client = JetpackApiClient(
        ParsedUrl.parse("https://victorious-leopon-humboldt.jurassic.ninja"), wpAuthenticationFromUsernameAndPassword(
            username = "demo", password = "8rFY WF3l JjHb AQEu vxNi JPFl"
        )
    )

    @Test
    fun testConnection() = runTest {
        val status = client.request { requestBuilder ->
            requestBuilder.connection().status()
        }.assertSuccessAndRetrieveData()
        assert(!status.isActive)
    }
}
