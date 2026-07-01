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
    // 改为 PREFER_SETTINGS，允许项目中的仓库作为补充
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        // 官方仓库（首选）
        google()
        mavenCentral()
        
        // 备用国内镜像（按稳定性排序）
        maven {
            url = uri("https://repo.huaweicloud.com/repository/maven/")
            name = "HuaweiCloud"
        }
        maven {
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
            name = "TencentCloud"
        }
        // 阿里云镜像（如果之前有，可保留，但放后面）
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
            name = "Aliyun"
        }
        // 特殊仓库（保持不变）
        maven(url = "https://api.xposed.info/")
    }
}

rootProject.name = "KailLocationXposed"
include(":app")
