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
    implementation("org.apache.xmlbeans:xmlbeans:5.3.0")
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
    description = "Генерация классов из message.xsd"

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

tasks.register<JavaExec>("runServer") {
    group = "application"
    description = "Запуск серверной части"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.github.diszexuf.server.Server")

    // сервер должен видеть banned_words.txt рядом с собой
    workingDir = rootDir

    standardInput = System.`in`
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Запуск клиентской части"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.github.diszexuf.client.Client")

    workingDir = rootDir

    // Подключение консоли для ввода команад
    standardInput = System.`in`
}