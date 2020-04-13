### Ellen Android SDK Setup

#### 1. build.gradle (project level)
```
allprojects {
  repositories {
    maven {
      url "https://jitpack.io"
    }
  }
}
```

#### 2. build.gradle (app level)
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
  def ellen_version = "0.6" // Or latest version
  def multidex_version = "2.0.1"
  def material_version ="1.1.0"
  def firebase_messaging_version="20.1.0"

  implementation "com.github.jffhsu:ellen-android-sdk:$ellen_version"
  implementation "androidx.multidex:multidex:$multidex_version"
  implementation "com.google.android.material:material:$material_version" 
}
```

#### 3. AndroidManifest.xml
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

#### 4. Set Messenger

1. userToken - Messaging token from user authentication
2. applicationContext - Application Context

##### Java
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

##### Kotlin
```
Messenger.set(userToken, applicationContext, object: CompletionCallback() {
  override fun onCompletion(result: Result<Any>) {
    if(result is Result.Success) {
      Log.d(TAG, "Messenger successfully set")
    }
  }
})
```

#### 5. Implement onRefreshTokenRequest

```
// Add request handler at app-level
public class MyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Request handler for refreshing user tokens
    Messenger.addRequestHandler(new RequestHandler() {
      @Override
      public String onRefreshTokenRequest() {
        // TODO Implement functionality and return a new user token
        String newUserToken = getNewUserToken();
        return newUserToken;
      }
    });
  }
}
```

```
<!-- AndroidManifest.xml -->
<application
  android:name=".MyApplication"
  ...
  >
```

#### 5. UI Kit

##### 5a. UI Unified
```
startActivity(new Intent(YourActivity.this, MessengerActivity.class));
```
##### 5b. UI Screens

In your layout.xml, add the following snippet to use the Conversation list screen
```
<fragment
  android:id="@+id/conversation_screen"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  class="com.koder.ellen.screen.ConversationScreen"
  />
```

To get the click event of the list you must use setItemClickListener
```
ConversationScreen.setItemClickListener(object: ConversationScreen.OnItemClickListener() {
  override fun OnItemClickListener(conversation: Conversation, position: Int) {

  }
})
```

Setting background color and corner radius
```
val conversationScreen = supportFragmentManager.findFragmentById(R.id.conversation_screen) as ConversationScreen
conversationScreen.setBackgroundColor("#00CCCC")
conversationScreen.setListCornerRadius(20, 20, 0, 0)  // dp
```