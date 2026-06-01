/**
 *          GrimAC Build Configuration
 *
 * Build Flags:
 * -PshadePE=true   - Enables 'lite' mode
 * -Prelocate=false - Adds 'no_relocate' modifier
 * -Prelease=true   - Removes commit/modifiers for release build
 *
 * Logic in: buildSrc/versioning/BuildConfig.kt & VersionUtil.kt
 */

plugins {
    base
}

import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.compile.JavaCompile
import versioning.BuildConfig
import versioning.VersionUtil

BuildConfig.init(project)

val baseVersion = "2.3.74"
group = "ac.grim.grimac"
version = VersionUtil.computeVersion(baseVersion)
description = "Libre simulation anticheat designed for 1.21 with 1.8–1.21 support, powered by PacketEvents 2.11.2."

ext["timestamp"] = System.currentTimeMillis().toString()
ext["git_branch"] = VersionUtil.getGitBranch(true)
ext["git_commit"] = VersionUtil.getGitCommitHash(true)
ext["git_org"] = System.getenv("GRIM_GIT_ORG") ?: VersionUtil.getGitUser()
ext["git_repo"] = System.getenv("GRIM_GIT_REPO") ?: "Grim"

println("Build configuration:")
println("    shadePE            = ${BuildConfig.shadePE}")
println("    relocate           = ${BuildConfig.relocate}")
println("    mavenLocalOverride = ${BuildConfig.mavenLocalOverride}")
println("    release            = ${BuildConfig.release}")
println("    version            = $version")

tasks.register("printVersion") {
    group = "versioning"
    description = "Prints the computed project version"
    doLast {
        println("VERSION=$version")
    }
}

// ---------- Java Compile Optimization ----------
subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.isFork = true
        options.isIncremental = true
    }
}

tasks.register<Copy>("copyBukkitJarToRoot") {
    dependsOn(":bukkit:shadowJar")
    from(project(":bukkit").layout.buildDirectory.dir("libs")) {
        include("LightningGrim-bukkit-*.jar")
        rename("LightningGrim-bukkit-.*\\.jar", "LightningGrim-$baseVersion.jar")
    }
    into(layout.buildDirectory.dir("libs"))
}

tasks.named("build") {
    dependsOn("copyBukkitJarToRoot")
}
