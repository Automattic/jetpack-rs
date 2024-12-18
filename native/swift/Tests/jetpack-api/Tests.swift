import Foundation
import Testing
import WordPressAPIInternal
import JetpackAPIInternal

#if canImport(FoundationNetworking)
import FoundationNetworking
#endif

extension URLSession: JetpackRequestExecutor {
    public func execute(request: WpNetworkRequest) async throws -> JetpackAPIInternal.JetpackNetworkResponse {
        var urlRequest = URLRequest(url: URL(string: request.url())!)
        urlRequest.httpMethod = request.method() == .get ? "GET" : "POST"
        urlRequest.httpBody = request.body()?.contents()
        for (key, value) in request.headerMap().toMap() {
            for v in value {
                urlRequest.addValue(v, forHTTPHeaderField: key)
            }
        }

        do {
            let (data, response) = try await self.data(for: urlRequest)
            let httpResp = response as! HTTPURLResponse
            let headers = Dictionary<String, String>(uniqueKeysWithValues: httpResp.allHeaderFields.map { ($0 as! String, $1 as! String) })
            let wpResponse = WpNetworkResponse(body: data, statusCode: UInt16(httpResp.statusCode), headerMap: try .fromMap(hashMap: headers))
            return .init(inner: wpResponse, dummy: .init())
        } catch {
            throw JetpackRequestExecutionError.RequestExecutionFailed(statusCode: 500, reason: "Fake")
        }
    }


}

@Test
func testJetpackAPI() async throws {
    let url = try ParsedUrl.parse(input: "https://longreads.com")
    let client = UniffiJetpackClient(siteUrl: url, authentication: .none, requestExecutor: URLSession.shared)
    let status = try await client.connection().status()
    #expect(status.data.isActive)
}
