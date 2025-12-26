pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Mapbox Maven repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials.username = "mapbox"
            // Read from local.properties (not tracked by git)
            credentials.password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN")
                .orElse(providers.environmentVariable("MAPBOX_DOWNLOADS_TOKEN"))
                .getOrElse("") // Empty string if not found - build will fail with proper error
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

rootProject.name = "RunMapProApp"
include(":app")
 