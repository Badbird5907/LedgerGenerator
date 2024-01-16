plugins {
    id("java")
    id("io.freefair.lombok") version "8.4"
}

group = "dev.badbird.ledger"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.poi:poi:5.2.0")
    implementation("org.apache.poi:poi-ooxml:5.2.0")
    implementation("org.jxls:jxls-jexcel:1.0.9")
    implementation("org.dhatim:fastexcel-reader:0.15.3")
    implementation("org.dhatim:fastexcel:0.15.3")
}

tasks.test {
    useJUnitPlatform()
}