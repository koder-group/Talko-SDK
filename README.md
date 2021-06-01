### Ellen Android SDK Setup

#### 1. To use JitPack with private repositories:
Add the token to $HOME/.gradle/gradle.properties (if file not exist create one)
```
authToken=jp_os7h7mpvr2t3772cd8vo4t7o1u
```

#### 2. build.gradle (project level)
```
allprojects {
  repositories {
    maven {
      url "https://jitpack.io"
      credentials { username authToken }
    }
  }
}
```

#### 3. build.gradle (app level)
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
  def talko_version = "0.89" // Or latest version
  def multidex_version = "2.0.1"
  def material_version ="1.1.0"
  def firebase_messaging_version="20.1.0"

  implementation "com.github.koder-group:Talko-SDK:$talko_version"
  implementation "androidx.multidex:multidex:$multidex_version"
  implementation "com.google.android.material:material:$material_version" 
}
```

#### 4. AndroidManifest.xml
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

#### 5. Set Messenger

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

#### . Implement onRefreshTokenRequest

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

#### 6. UI Kit

##### 6a. UI Unified
UI Unified is an Activity that includes the entire chat application.
```
startActivity(new Intent(YourActivity.this, MessengerActivity.class));
```
##### 6b. UI Screens

UI Screens are Fragments that make up each part of a chat application.

- ConversationScreen
- UserSearchScreen
- MessageScreen
- MessageInfoScreen
- AddParticipantScreen

**Conversation Screen**
```
val conversationScreen = ConversationScreen()
getSupportFragmentManager().beginTransaction().replace(
  R.id.frame_layout,
  conversationScreen
).commit()
```

**UserSearchScreen**
- Does not require any arguments.

**Message Screen**
- Pass in a Conversation ID to show a Conversation 
- Pass in a User ID to create a new Conversation
```
// Show a Conversation by passing in CONVERSATION_ID
val bundle = Bundle()
val messageScreen = MessageScreen()
bundle.putString("CONVERSATION_ID", conversationId)
messageScreen.setArguments(bundle)
getSupportFragmentManager().beginTransaction().replace(
  R.id.frame_layout, 
  messageScreen
).commit()

// Create a Conversation by passing in ADD_USER_ID
val bundle = Bundle()
val messageScreen = MessageScreen()
bundle.putString("ADD_USER_ID", userId)
messageScreen.setArguments(bundle)
getSupportFragmentManager().beginTransaction().replace(
  R.id.frame_layout, 
  messageScreen
).commit()
```

**Message Info Screen**
- Pass in a Conversation ID to show the Info screen
```
val bundle = Bundle()
val messageInfoScreen = MessageInfoScreen()
bundle.putString("CONVERSATION_ID", conversationId)
messageInfoScreen.setArguments(bundle)
getSupportFragmentManager().beginTransaction().replace(
  R.id.frame_layout, 
  messageInfoScreen
).commit()
```

**Add Participant Screen**
- Pass in a Conversation ID
```
val bundle = Bundle()
val addParticipantScreen = AddParticipantScreen()
bundle.putString("CONVERSATION_ID", conversationId)
addParticipantScreen.setArguments(bundle)
getSupportFragmentManager().beginTransaction().replace(
  R.id.screenFrame, 
  addParticipantScreen
).commit()
```

#####Click Events

To get the click event in a screen, you must use the provided click listeners

######Conversation Screen
```
ConversationScreen.setItemClickListener(object: ConversationScreen.OnItemClickListener() {
  override fun OnItemClickListener(conversation: Conversation, position: Int) {
    // Show the selected conversation
  }
})
```
######User Search Screen
To get the selected user:
```
val userSearchScreen = supportFragmentManager.findFragmentByTag(resources.getString(R.string.search)) as UserSearchScreen
val user = userSearchScreen.getSelectedUser()
user?.let {
  findOrCreateConversation(user)
  hideKeyboard(this@MainActivity)
}
```

######Message Info Screen
```
MessageInfoScreen.setItemClickListener(object: MessageInfoScreen.OnItemClickListener() {
  override fun onClickAddParticipant(conversationId: String) {
    // Show the Add Partcipant Screen
  }
})
```

######Add Participant Screen
```
AddParticipantScreen.setItemClickListener(object: AddParticipantScreen.OnItemClickListener() {
    override fun OnItemClickListener(
        userId: String,
        conversationId: String,
        position: Int
    ) {
      // Add the selected user to the conversation
    }
})
```

**Customizable UI Options**
```
// All Screens
Messenger.screenBackgroundColor = "#FFFFFF"
Messenger.screenCornerRadius = intArrayOf(0, 0, 0, 0) // top left, top right, bottom right, bottom left

// Conversation Screen
Messenger.conversationItemTopPadding = 10 // dp
Messenger.conversationItemBottomPadding = 10 // dp
Messenger.conversationIconRadius = 21 // dp
Messenger.conversationTitleSize = 14f // sp, float
Messenger.conversationSubtitleSize = 14f // sp, float

// Message Screen
Messenger.senderMessageRadius = 18 // dp
Messenger.selfMessageRadius = 18
Messenger.senderBackgroundColor = "#88000000"  // gray
Messenger.selfBackgroundColor = "#1A73E9"  // blue
```