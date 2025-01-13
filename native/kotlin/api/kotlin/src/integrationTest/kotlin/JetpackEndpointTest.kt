package rs.wordpress.api.kotlin

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import uniffi.wp_api.ParsedUrl
import uniffi.wp_api.wpAuthenticationFromUsernameAndPassword

class JetpackEndpointTest {
    // These credentials are from a throwaway site
    private val client = JetpackApiClient(
        ParsedUrl.parse("https://sure-willow.jurassic.ninja/"), wpAuthenticationFromUsernameAndPassword(
            username = "demo", password = "ugGK K5SV DpmA IZvJ OXXn 4g5k"
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
