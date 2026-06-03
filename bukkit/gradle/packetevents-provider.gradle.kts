// True-OG PacketEvents JAR staging convention.
// Resolution order (first hit wins): -PtruogPacketEventsJar, TRUEOG_PACKETEVENTS_JAR env, default glob under ~/eclipse-workspace/PacketEvents/spigot/build/libs (case-insensitive, latest by mtime). See context/refs/packetevents-provider.md.

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Comparator

fun resolveTrueOgPacketEventsJar(project: Project): File {
    val home = System.getProperty("user.home")
    val defaultDir = Paths.get(home, "eclipse-workspace", "PacketEvents", "spigot", "build", "libs")

    // Every resolution path is reported in the failure message regardless of which got tried.
    val propValue = (project.findProperty("truogPacketEventsJar") as? String)?.takeIf { it.isNotBlank() } ?: "<unset>"
    val envValue = System.getenv("TRUEOG_PACKETEVENTS_JAR")?.takeIf { it.isNotBlank() } ?: "<unset>"
    val attempts = listOf(
        "Gradle property truogPacketEventsJar=$propValue",
        "Environment variable TRUEOG_PACKETEVENTS_JAR=$envValue",
        "Default glob $defaultDir/PacketEvents-*.jar (case-insensitive; falls back to packetevents-spigot-*.jar; latest by mtime)",
    )

    val gradleProp = (project.findProperty("truogPacketEventsJar") as? String)?.trim()
    if (!gradleProp.isNullOrEmpty()) {
        val f = File(gradleProp)
        if (f.isFile) return f
    }

    val envVar = System.getenv("TRUEOG_PACKETEVENTS_JAR")?.trim()
    if (!envVar.isNullOrEmpty()) {
        val f = File(envVar)
        if (f.isFile) return f
    }

    if (Files.isDirectory(defaultDir)) {
        val nameRegex = Regex("(?i)(PacketEvents|packetevents-spigot|packetevents)-.*\\.jar")
        val match = Files.list(defaultDir).use { stream ->
            stream
                .filter { nameRegex.matches(it.fileName.toString()) }
                .sorted(Comparator.comparingLong<Path> { Files.getLastModifiedTime(it).toMillis() }.reversed())
                .findFirst()
                .orElse(null)
        }
        if (match != null) return match.toFile()
    }

    throw GradleException(
        buildString {
            appendLine("True-OG PacketEvents JAR not found. Tried:")
            attempts.forEach { appendLine("  - $it") }
            appendLine("Provide one of:")
            appendLine("  ./gradlew -PtruogPacketEventsJar=/abs/path/to/PacketEvents.jar ...")
            appendLine("  TRUEOG_PACKETEVENTS_JAR=/abs/path/to/PacketEvents.jar ./gradlew ...")
            appendLine("  Build the fork at \${user.home}/eclipse-workspace/PacketEvents (default location).")
        },
    )
}


// Registers a Copy task that stages the resolved JAR into runDir/plugins/PacketEvents.jar. See context/refs/packetevents-provider.md for the consumer integration snippet.
val registerStagePacketEventsTask: (Project, String, File) -> TaskProvider<Copy> = { p, taskName, runDir ->
    p.tasks.register(taskName, Copy::class.java) {
        group = "packetevents"
        description = "Stage the locally built True-OG PacketEvents JAR into plugins/."
        // Eager filesystem resolution at configuration time is incompatible with the config cache.
        notCompatibleWithConfigurationCache(
            "Resolves the JAR via filesystem inspection during configuration.",
        )
        val jar = resolveTrueOgPacketEventsJar(p)
        from(jar)
        into(runDir.resolve("plugins"))
        rename { "PacketEvents.jar" }
    }
}

extra["resolveTrueOgPacketEventsJar"] = { p: Project -> resolveTrueOgPacketEventsJar(p) }
extra["registerStagePacketEventsTask"] = registerStagePacketEventsTask
