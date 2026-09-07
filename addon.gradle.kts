// Preserve the upstream license in both distributable and source jars.
listOf("jar", "sourcesJar").forEach { taskName ->
    tasks.named<Jar>(taskName) {
        from("LICENSE") {
            into("META-INF")
        }
    }
}
