# AndroidStaticGlassesSample使用说明

[官网](http://www.topplusvision.com)

## 开发环境说明 ##

### 使用android studio 2.0及以上版本开发，相关工具的版本情况如下： ###

```Java
    compileSdkVersion 24
    buildToolsVersion '24.0.2'

    defaultConfig {
        applicationId "example.com.staticglasses"
        minSdkVersion 14
        targetSdkVersion 22
    }
```

### 支持平台说明 ###
目前 sdk 支持的 android 系统是 android4.0 及以上，然后支持的 android 芯片平台有 armeabi-v7a,armeabi,x86,x86_64,arm64-v8a。

## 接入流程 ##
### 依赖库导入 ###

基础库: [commonutils-release.aar](https://github.com/topplus/AndroidStaticGlassesSample/raw/master/commonutils-release/commonutils-release.aar)，需添加到Android项目中。
渲染库: [render-release.aar](https://github.com/topplus/AndroidStaticGlassesSample/raw/master/render-release/render-release.aar)，需添加到Android项目中。
眼镜库: [staticglasses-release.aar](https://github.com/topplus/AndroidStaticGlassesSample/raw/master/staticglasses-release/staticglasses-release.aar)，需添加到Android项目中。

### 授权认证 ###

调用topplus.com.commonutils.Library.init(getApplicationContext(), " client_id", " client_secret",false);
说明：申请 client_id 和 client_secret 后调用此函数获得授权。可通过商务合作邮箱sales@topplusvision.com获得client_id 和 client_secret


## 接口定义和使用说明 ##

[文档](https://github.com/topplus/AndroidStaticGlassesSample/raw/master/doc/眼镜虚拟试戴Android+SDK人脸录制试戴文档.pdf)

## 联系我们 ##

商务合作sales@topplusvision.com

媒体合作pr@topplusvision.com

技术支持support@topplusvision.com