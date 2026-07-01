pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven { url = uri("https://www.jitpack.io") }
        // 阿里云镜像已全部移除
        // 如需国内加速，可添加如下备用镜像（示例）：
        // maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        // maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://www.jitpack.io") }
        google()
        mavenCentral()
        // 如有特殊仓库（如 Xposed），可在此添加
        // maven(url = "https://api.xposed.info/")
    }
}

rootProject.name = "location"
include(":app")
include(":NewBlackbox:Bcore")
include(":NewBlackbox:black-reflection")
include(":NewBlackbox:compiler")
