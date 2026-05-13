// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}

tasks.register("checkJlink") {
    doLast {
        val asPath = "/Applications/Android Studio.app/Contents/jbr/Contents/Home"
        val jlink = java.io.File(asPath, "bin/jlink")
        println("AS Path: $asPath")
        println("jlink exists: ${jlink.exists()}")
        if (jlink.exists()) {
            println("jlink is executable: ${jlink.canExecute()}")
        }
    }
}
