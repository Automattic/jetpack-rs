use std::sync::Arc;

use jetpack_api::JetpackClient;
use jetpack_api_integration_tests::AsyncJpNetworking;
use serial_test::parallel;
use wp_api::{ParsedUrl, WpAuthentication};
use wp_api_integration_tests::AssertResponse;

#[tokio::test]
#[parallel]
async fn jetpack_connection() {
    // This is a disposable site, so temporarily having these credentials here is not a problem
    let authentication = WpAuthentication::from_username_and_password(
        "demo".to_string(),
        "mgvd IcUJ 8NDd 51k4 xIcb 6y5b".to_string(),
    );

    let site_url = ParsedUrl::parse("https://pheasant-of-papers.jurassic.ninja/").unwrap();
    let jetpack_client = JetpackClient::new(
        site_url.into(),
        authentication,
        Arc::new(AsyncJpNetworking::default()),
    );
    let connection_status = jetpack_client.connection().status().await.assert_response();
    assert!(!connection_status.is_active, "{:#?}", connection_status);
}
