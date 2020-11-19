# RootHttp
基于coroutines+flow+OkHttp+Retrofit的网络请求库封装
用协程和流替换RxJava方案

### 引入方式 
[![Release](https://img.shields.io/github/release/Dazhi528/RootHttp?style=flat)](https://jitpack.io/#Dazhi528/RootHttp)
[![API](https://img.shields.io/badge/API-16%2B-green.svg?style=flat)](https://android-arsenal.com/api?level=16)

**Gradle** <br/>
Step 1. Add the JitPack repository to your build file

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```
dependencies {
    implementation 'com.github.Dazhi528:RootHttp:Tag'
}
```