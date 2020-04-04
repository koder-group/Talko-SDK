## Ellen Android SDK Setup

### 1. build.gradle (project level)
```
allprojects {
  repositories {
    maven {
      url "https://jitpack.io"
    }
  }
}
```

### 2. build.gradle (app level)
```
android {
  defaultConfig {
    multiDexEnabled true
  }

  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}

dependencies {
  def multidex_version = "2.0.1"
  def material_version ="1.1.0"
  def firebase_messaging_version="20.1.0"

  implementation 'com.github.jffhsu:ellen-android-sdk:0.1'
  implementation "androidx.multidex:multidex:$multidex_version"
  implementation "com.google.android.material:material:$material_version" 
}
```

### 3. AndroidManifest.xml
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<application>
<activity android:name="com.koder.ellen.MessengerActivity"
  android:label=""
  android:launchMode="singleTop"
  android:theme="@style/Theme.MaterialComponents.DayNight.NoActionBar"></activity>
</application>
```

### 4. Set Messenger

1. userToken - Messaging token from user authentication
2. applicationContext - Application Context

#### Java
```
Messenger.set(userToken, applicationContext, new CompletionCallback() {
  @Override
  public void onCompletion(Result<?> result) {
    if(result instanceof Result.Success) {
      Log.d(TAG, "Messenger successfully set");
    }
  }
});
```

#### Kotlin
```
Messenger.set(userToken, userId, object: CompletionCallback() {
  override fun onCompletion(result: Result<Any>) {
    if(result is Result.Success) {
      Log.d(TAG, "Messenger successfully set")
    }
  }
})
```

### 5. Launch UI Unified
```
startActivity(new Intent(YourActivity.this, MessengerActivity.class));
```