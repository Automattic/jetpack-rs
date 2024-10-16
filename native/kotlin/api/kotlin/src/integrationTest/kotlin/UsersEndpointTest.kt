package rs.jetpack.api.kotlin

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import rs.wordpress.api.kotlin.WpApiClient
import rs.wordpress.api.kotlin.WpRequestResult
import uniffi.wp_api.ParsedUrl
import uniffi.wp_api.UserListParams
import uniffi.wp_api.wpAuthenticationFromUsernameAndPassword

class UsersEndpointTest {
    // These credentials are from a throwaway site
    private val client = WpApiClient(
        ParsedUrl.parse("https://victorious-leopon-humboldt.jurassic.ninja"), wpAuthenticationFromUsernameAndPassword(
            username = "demo", password = "8rFY WF3l JjHb AQEu vxNi JPFl"
        )
    )

    @Test
    fun testUserListRequest() = runTest {
        val response = client.request { requestBuilder ->
            requestBuilder.users().listWithEditContext(params = UserListParams())
        }
        assert(response is WpRequestResult.WpRequestSuccess)
        val userList =  (response as WpRequestResult.WpRequestSuccess).data
        assert(userList.isNotEmpty())
    }
}
