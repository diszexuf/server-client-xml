val xmlbeansConfig by configurations.creating

plugins {
    id("java")
}

group = "com.github.diszexuf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    xmlbeansConfig("org.apache.xmlbeans:xmlbeans:5.3.0")
    implementation(files("lib/schema.jar"))
    implementation("org.xerial:sqlite-jdbc:3.51.3.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runScomp") {
    group = "build"
    description = "Генерация из message.xsd"

    classpath = xmlbeansConfig
    mainClass.set("org.apache.xmlbeans.impl.tool.SchemaCompiler")

    inputs.file(file("schema/message.xsd"))
    outputs.file(file("lib/schema.jar"))

    args(
        "-out", file("lib/schema.jar").absolutePath,
        "-d", file("${layout.buildDirectory.get()}/generated/classes").absolutePath,
        file("schema/message.xsd").absolutePath
    )
}

tasks.named("compileJava") {
    dependsOn("runScomp")
}