// swift-tools-version: 6.0

import Foundation
import PackageDescription

let libjetpackFFIVersion: JetpackRSVersion = .local

#if os(Linux)
let libjetpackFFI: Target = .systemLibrary(
        name: "libjetpackFFI",
        path: "target/swift-bindings/libjetpackFFI-linux/"
    )
#elseif os(macOS)
let libjetpackFFI: Target = libjetpackFFIVersion.target
#endif

var package = Package(
    name: "JetpackAPI",
    platforms: [
        .iOS(.v13),
        .macOS(.v11),
        .tvOS(.v13),
        .watchOS(.v8)
    ],
    products: [
        .library(
            name: "JetpackAPI",
            targets: ["JetpackAPI"]
        )
    ],
    dependencies: [
//        .package(url: "https://github.com/Automattic/wordpress-rs", revision: "alpha-20241116"),
        .package(path: "../wordpress-rs"),
    ],
    targets: [
        .target(
            name: "JetpackAPI",
            dependencies: [
                .target(name: "JetpackAPIInternal")
            ],
            path: "native/swift/Sources/jetpack-api",
            swiftSettings: [
                .enableExperimentalFeature("StrictConcurrency"),
            ]
        ),
        .target(
            name: "JetpackAPIInternal",
            dependencies: [
                .target(name: libjetpackFFI.name),
                .product(name: "WordPressAPI", package: "wordpress-rs")
            ],
            path: "native/swift/Sources/jetpack-api-wrapper",
            swiftSettings: [
                .swiftLanguageMode(.v5)
            ]
        ),
        libjetpackFFI,
        .testTarget(
            name: "JetpackAPITests",
            dependencies: [
                .target(name: "JetpackAPI"),
                .target(name: libjetpackFFI.name)
            ],
            path: "native/swift/Tests/jetpack-api"
        )
    ]
)

// MARK: - Enable local development toolings

let localDevelopment = false // libjetpackFFIVersion.isLocal

if localDevelopment {
    try enableSwiftLint()
}

// MARK: - Helpers

enum JetpackRSVersion {
    case local
    case release(version: String, checksum: String)

    var isLocal: Bool {
        if case .local = self {
            return true
        }
        return false
    }

    var target: Target {
        switch libjetpackFFIVersion {
        case .local:
            return .binaryTarget(name: "libjetpackFFI", path: "target/libjetpackFFI.xcframework")
        case let .release(version, checksum):
            return .binaryTarget(
                name: "libjetpackFFI",
                url: "https://cdn.a8c-ci.services/wordpress-rs/\(version)/libjetpackFFI.xcframework.zip",
                checksum: checksum
            )
        }
    }
}

// Add SwiftLint to the package so that we can see linting issues directly from Xcode.
@MainActor
func enableSwiftLint() throws {
#if os(macOS)
    let filePath = URL(string:"./.swiftlint.yml", relativeTo: URL(filePath: #filePath))!
    let version = try String(contentsOf: filePath, encoding: .utf8)
        .split(separator: "\n")
        .first(where: { $0.starts(with: "swiftlint_version") })?
        .split(separator: ":")
        .last?
        .trimmingCharacters(in: .whitespaces)
    guard let version else {
        fatalError("Can't find swiftlint_version in .swiftlint.yml")
    }

    package.dependencies.append(.package(url: "https://github.com/realm/SwiftLint", exact: .init(version)!))

    var platforms = package.platforms ?? []
    if let mac = platforms.firstIndex(where: { $0 == .macOS(.v11) }) {
        platforms.remove(at: mac)
        platforms.append(.macOS(.v12))
    }
    package.platforms = platforms

    if let target = package.targets.first(where: { $0.name == "JetpackAPI" }) {
        target.plugins = (target.plugins ?? []) + [.plugin(name: "SwiftLintBuildToolPlugin", package: "SwiftLint")]
    }
#endif
}
