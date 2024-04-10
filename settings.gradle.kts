plugins {
    kotlin("jvm") version "1.9.21" apply false
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "network-labs"
include("web-server")
include("udp-pinger")
include("proxy")
