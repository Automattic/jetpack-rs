use std::{fmt::Debug, sync::Arc};

use crate::JetpackRequestExecutionError;
use serde::de::DeserializeOwned;
use wp_api::{
    request::{ParsedResponse, WpNetworkRequest, WpNetworkResponse},
    url_query::FromUrlQueryPairs,
    ParsedRequestError,
};

pub mod endpoint;

#[uniffi::export(with_foreign)]
#[async_trait::async_trait]
pub trait JetpackRequestExecutor: Send + Sync + Debug {
    async fn execute(
        &self,
        request: Arc<WpNetworkRequest>,
    ) -> Result<JetpackNetworkResponse, JetpackRequestExecutionError>;
}

#[derive(Debug, Default, uniffi::Object)]
pub struct JetpackDummyObject;

#[uniffi::export]
impl JetpackDummyObject {
    #[uniffi::constructor]
    pub fn new() -> Self {
        Self
    }
}

#[derive(Debug, uniffi::Record)]
pub struct JetpackNetworkResponse {
    pub inner: WpNetworkResponse,
    dummy: Arc<JetpackDummyObject>,
}

impl From<WpNetworkResponse> for JetpackNetworkResponse {
    fn from(value: WpNetworkResponse) -> Self {
        Self { inner: value, dummy: JetpackDummyObject.into() }
    }
}

impl JetpackNetworkResponse {
    pub fn parse<ResponseType, DataType, ParamsType, E>(self) -> Result<ResponseType, E>
    where
        ResponseType: DeserializeOwned,
        ResponseType: From<ParsedResponse<DataType, ParamsType>>,
        ParsedResponse<DataType, ParamsType>: From<ResponseType>,
        ParamsType: FromUrlQueryPairs,
        E: ParsedRequestError,
    {
        self.inner.parse()
    }
}
