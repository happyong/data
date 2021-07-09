# ExoDoris

![Doris](https://user-images.githubusercontent.com/230559/72438312-7379a800-379c-11ea-8558-c6be2db597c1.png "Doris")

## Advanced Android Media Player

ExoDoris is an advanced and highly optimised application level media
player for Android. It is based on Google's [ExoPlayer](https://github.com/google/ExoPlayer).
It supports playing audio and video both locally and over the Internet.
ExoDoris supports adaptive bitrate streaming protocols like HLS and DASH,
as well as most local media formats like MP4, MP3, AVI, MKV, etc.

* An opt-in UI compatible across phones, tablets and TVs. Unlike other players in the field, ExoDoris doesn't require you to peel back baked in UI, you can roll your own from scratch or use the default UI.

* A playback safe, intuitive DVR API. ExoDoris wraps the complexity of growing and sliding DVR windows into a straight forward API.

* Built in support for audio only streams. Audio only streams are played in a foreground Android service which allows the audio stream to continue playing when the user leaves your app or locks their device.

* Supports playback of DRM protected content.

* Support for external subtitle tracks. ExoDoris supports both subtitles that are embedded in the media, as well as subtitle tracks that are provided separately.

* Supports playback of dynamic ad insertion (DAI) IMA streams.

* Supports playback of client-side ad insertion (CSAI) IMA streams.

## Table of Contents

* [ExoDoris](#exodoris)
  * [Advanced Android Media Player](#advanced-android-media-player)
  * [Table of Contents](#table-of-contents)
  * [Getting Started](#getting-started)
    * [Install ExoDoris](#install-exodoris)
      * [1. Add repositories](#1-add-repositories)
      * [2. Add JitPack authorisation token](#2-add-jitpack-authorisation-token)
      * [3. Add ExoDoris module dependencies](#3-add-exodoris-module-dependencies)
      * [4. Turn on Java 8 support](#4-turn-on-java-8-support)
      * [5. Add permission to access the Internet](#5-add-permission-to-access-the-internet)
    * [Instantiation](#instantiation)
    * [Add the `DorisPlayerView` element](#add-the-dorisplayerview-element)
    * [Customising the UI](#customising-the-ui)
    * [Attach `DorisPlayerView` to `ExoDoris`](#attach-dorisplayerview-to-exodoris)
    * [Playing nice with the Activity lifecycle](#playing-nice-with-the-activity-lifecycle)
    * [Load a stream](#load-a-stream)
    * [Take control](#take-control)
    * [Listen to player events](#listen-to-player-events)
  * [Configuration](#configuration)
    * [Player configuration](#player-configuration)
      * [context](#context)
      * [playWhenReady](#playwhenready)
      * [userAgent](#useragent)
    * [Building `ExoDoris`](#building-exodoris)
    * [Source configuration](#source-configuration)
      * [id](#id)
      * [extension](#extension)
      * [videoType](#videotype)
      * [mediaItem](#mediaitem)
        * [Simple media item](#simple-media-item)
        * [DRM protected content](#drm-protected-content)
        * [Sideloading subtitles](#sideloading-subtitles)
        * [Client side ad insertion, CSAI](#client-side-ad-insertion-csai)
        * [From an existed media item](#from-an-existed-media-item)
      * [imaDaiProperties](#imadaiproperties)
        * [Live content](#live-content)
        * [VOD content](#vod-content)
        * [authToken](#authtoken)
        * [apiKey](#apikey)
        * [fallbackUri](#fallbackuri)
        * [adTagParameters](#adtagparameters)
        * [adTagParametersValidFrom](#adtagparametersvalidfrom)
        * [adTagParametersValidUntil](#adtagparametersvaliduntil)
      * [muxProperties](#muxproperties)
      * [contentType](#contenttype)
      * [shouldPlayOffline](#shouldplayoffline)
      * [offlineLicenseUri](#offlinelicenseuri)
      * [audioThumbnailUri](#audiothumbnailuri)
      * [audioTitle](#audiotitle)
      * [maxVideoWidth, maxVideoHeight](#maxvideowidth-maxvideoheight)
      * [initialWindowIndex](#initialwindowindex)
      * [initialPosition](#initialposition)
    * [Building a `Source`](#building-a-source)
    * [Loading a `Source`](#loading-a-source)
  * [Seeking and DVR](#seeking-and-dvr)
    * [Types of DVR window](#types-of-dvr-window)
  * [Digital Rights Management (DRM)](#digital-rights-management-drm)
    * [Load a DRM protected stream](#load-a-drm-protected-stream)
  * [ExoDoris Audio Only Extension](#exodoris-audio-only-extension)
    * [Add ExoDoris audio-only extension dependency](#add-exodoris-audio-only-extension-dependency)
    * [Define the audio service in the Android manifest](#define-the-audio-service-in-the-android-manifest)
    * [Create and start the audio service](#create-and-start-the-audio-service)
    * [Load an audio-only stream](#load-an-audio-only-stream)
  * [ExoDoris IMA DAI Extension](#exodoris-ima-dai-extension)
    * [Add ExoDoris IMA DAI extension dependency](#add-exodoris-ima-dai-extension-dependency)
    * [Create the ExoDoris IMA DAI Player](#create-the-exodoris-ima-dai-player)
    * [Load a IMA DAI stream](#load-a-ima-dai-stream)
  * [ExoDoris IMA CSAI Extension](#exodoris-ima-csai-extension)
    * [Add ExoDoris IMA CSAI extension dependency](#add-exodoris-ima-csai-extension-dependency)
    * [Create the ExoDoris IMA CSAI Player](#create-the-exodoris-ima-csai-player)
    * [Load a IMA CSAI stream](#load-a-ima-csai-stream)

## Getting Started
### Install ExoDoris

#### 1. Add repositories

The easiest way to get started using ExoDoris is to add it as a gradle
dependency. You need to make sure you have the Google, JCenter and JitPack
repositories included in the `build.gradle` file in the root of your project:

```gradle
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url "https://jitpack.io"
            credentials { username authToken }
        }
    }
}
```

#### 2. Add JitPack authorisation token

Contact your account manager to obtain your authorisation token. Once
you have your authorisation token, add it to `$HOME/.gradle/gradle.properties`

```gradle
authToken=your_auth_token
```

##### Take note:

Adding your authorisation token directly to the `build.gradle` file in
the root of your project will also work, but this is **not recommended**.
The `build.gradle` file in the root of your project is typically committed
to your source control (Git) repository and this will expose your
authorisation token.

#### 3. Add ExoDoris module dependencies

Next add a dependency in the `build.gradle` file of your app module. The
following will add a dependency to the core ExoDoris library:

```gradle
dependencies {
    ...
    implementation "com.github.DiceTechnology.doris-android:doris:0.X.X"
}
```
where 0.X.X is your preferred version.

The core ExoDoris library includes everything you will need for video
playback like:

* Basic video player UI
* Support for playback adaptive video streaming formats (HLS, DASH) and non-adaptive formats (MP4, AVI, MKV, etc)
* DVR API
* Support for both embedded and external subtitles
* DRM suppprt

In addition to the core ExoDoris library, ExoDoris has extension
modules that provide additional functionality. The available extension
modules are listed below.

* `audio-only`: Adds support for playback of audio only content in a separate Android service that enables background playback of audio.
* `ima-dai`: Adds support for playback of dynamic ad insertion (DAI) IMA streams.
* `ima-csai`: Adds support for playback of client-side ad insertion (CSAI) IMA streams.

#### 4. Turn on Java 8 support

If not enabled already, you also need to turn on Java 8 support in all
`build.gradle` files depending on ExoDoris, by adding the following to the
`android` section:

```gradle
android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

If you have Kotlin code in your app, add the following lines to your
`build.gradle` files **in addition** to the lines above:

```gradle
android {
    ...
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```

#### 5. Add permission to access the Internet

ExoDoris requires access to the Internet to playback adaptive streaming
formats like HLS and DASH. If you just intend to play local media files
with ExoDoris, it would still be preferable to allow ExoDoris to access
the Internet, as we collect anonymous playback data to continuously improve
ExoDoris. To allow your application and, in turn, ExoDoris access to the
Internet, add the following line to your app's `AndroidManifest.xml` file:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    ...
    <uses-permission android:name="android.permission.INTERNET" />
    ...
</manifest>
```

### Instantiation

The [builder pattern](https://en.wikipedia.org/wiki/Builder_pattern) is
used throughout ExoDoris to make the instantiation of large, complex
objects a lot easier. In fact many classes in ExoDoris can only be
instantiated using their associated builder class.

The player class, `ExoDoris`, is one of the classes that can only be
instantiated using it's associated builder class, `ExoDorisBuilder`. It
is very simple to instantiate an instance of ExoDoris with the default configuration,
simply import ExoDoris and create a player object using the `ExoDorisBuilder`
as illustrated below:

**PlayerActivity.java**
```java
import com.diceplatform.doris.ExoDoris;
import com.diceplatform.doris.ExoDorisBuilder;
[...]
ExoDoris player = new ExoDorisBuilder(context).build();
```

where `context` is either your Activity or Application context.

### Add the `DorisPlayerView` element

In order to display video content, we need a UI player view element that
will be attached to the `ExoDoris` player (instantiated above). ExoDoris
comes with a default UI view, which includes basic player controls. This
view can be added to any layout of your choice as illustrated below:

**activity_player.xml**
```xml
<com.diceplatform.doris.ui.DorisPlayerView
   android:id="@+id/playerView"
   android:layout_width="match_parent"
   android:layout_height="match_parent"/>
```

The above code will add a `DorisPlayerView` that stretches across the
entire width and height of the device. Typically video player views are
fullscreen to make the viewing experience more immersive for the user,
however, if you would like to constrain your `DorisPlayerView` to a
smaller window, simply modify the `android:layout_width` and  
`android:layout_height` properties.

### Customising the UI

*TODO: Explain how to add custom UI here*

### Attach `DorisPlayerView` to `ExoDoris`

In order to attach the `ExoDoris` (player) object to the `DorisPlayerView`
(playerView) you need to set the instance of `ExoDoris`, that was created
previously, on the `DorisPlayerView` programmatically. This is illustrated  
below:

**PlayerActivity.java**
```java
private DorisPlayerView playerView;
private ExoDoris player;
[...]
@Override
protected void onCreate(Bundle savedInstanceState) {
    [...]
    playerView = findViewById(R.id.video_view);
    initializePlayer();
}
[...]
private void initializePlayer() {
    player = new ExoDorisBuilder(context).build();
    playerView.setPlayer(player);
}
```

### Playing nice with the Activity lifecycle

Our player can hog a lot of resources including memory, CPU, network
connections and hardware codecs. Many of these resources are in short
supply, particularly for hardware codecs where there may only be one.
It's important that you release those resources for other apps to use
when you're not using them, such as when your app is put into the background.

Put another way, your player's lifecycle should be tied to the lifecycle
of your app. To implement this, you need to override the four methods of
`PlayerActivity`: `onStart`, `onResume`, `onPause`, and `onStop`. This
is illustrated in the code blocks below:

Initialize the player in the `onStart` or `onResume` callback depending on  
the API level.

**PlayerActivity.java**
```java
@Override
public void onStart() {
    super.onStart();
    if (Util.SDK_INT >= 24) {
        initializePlayer();
    }
}

@Override
public void onResume() {
    super.onResume();
    hideSystemUi();
    if ((Util.SDK_INT < 24 || player == null)) {
        initializePlayer();
    }
}

@SuppressLint("InlinedApi")
private void hideSystemUi() {
 playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
     | View.SYSTEM_UI_FLAG_FULLSCREEN
     | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
     | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
     | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
     | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
}
```

Android API level 24 and higher supports multiple windows. As your app
can be visible, but not active in split window mode, you need to
initialise the player in `onStart`. Android API level 24 and lower
requires you to wait as long as possible until you grab resources, so
you wait until `onResume` before initializing the player.

`hideSystemUi` is a helper method called in `onResume`, which allows you
to have a full-screen experience.

Release resources with `releasePlayer` (which will be created shortly)
in `onPause` or `onStop`, depending on the API level.

**PlayerActivity.java**
```java
@Override
public void onPause() {
    super.onPause();
    if (Util.SDK_INT < 24) {
        releasePlayer();
    }
}

@Override
public void onStop() {
    super.onStop();
    if ((Util.SDK_INT >= 24) {
        releasePlayer();
    }
}
```

With API Level 24 and lower, there is no guarantee of onStop being called,
so you have to release the player as early as possible in `onPause`. With
API Level 24 and higher (which brought multi- and split-window mode),
`onStop` is guaranteed to be called. In the paused state, your activity
is still visible, so you wait to release the player until `onStop`.

You now need to create a `releasePlayer` method, which frees the player's
resources and destroys it.

**PlayerActivity.java**
```java
private void releasePlayer() {
    if (player != null) {
        player.release();
        player = null;
    }
}
```

### Load a stream

We load a stream using the `load` method that can be called on the player
instance. This method requires a `Source` object to be passed to it.
More information on how to build a `Source` object can be found in the
[Source configuration](#source-configuration) section. Calling the
`load` method will both load up the stream and start playback
automatically by default (this behaviour can be changed, see
[playWhenReady](#playwhenready)).

**PlayerActivity.java**
```java
player.load(source);
```

### Take control

```java
player.play();

player.seekTo(30);

player.pause()
```

### Listen to player events

In order to listen to the player events you're interested in, such as playback
state changes, playback errors and so on, you need to register a listener
or analytics to receive such events. This is illustrated below:

**PlayerActivity.java**
```java
private ExoDoris player;
[...]
private void initializePlayer() {
    player.addListener(listener);
    player.addAnalyticsListener(analytics);
}
```
where `listener` is [Player.Listener](https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/Player.Listener.html) and `analytics` is [AnalyticsListener](https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/analytics/AnalyticsListener.html).

Please refer to the [Player events page](https://exoplayer.dev/listening-to-player-events.html) and the [analytics page](https://exoplayer.dev/analytics.html) for more details.

## Configuration

### Player configuration

The `ExoDoris` player can only be instantiated using its associated
builder class, `ExoDorisBuilder`. This builder simplifies the
instantiation of the `ExoDoris` player, by providing default values for
many of the player's constructor parameters.

#### context

Type: `Context`

This is the only parameter that is required in order to build an `ExoDoris`
player using the `ExoDorisBuilder`. All the other parameters are optional
and if they are not set the default values will be used.

The context provided to `ExoDorisBuilder` is typically your application
context, but it can also be the context of the activity in which the
player is displayed. The context is provided to the `ExoDorisBuilder` in
its constructor as illustrated below:

```java
Context context = applicationContext;

ExoDorisBuilder builder = new ExoDorisBuilder(context);
```

#### playWhenReady

Type: `boolean`

Default Value: `true`

Boolean that sets whether the player should start/resume playback when
the player is ready to playback the video stream, by default this is set
to `true`. To modify the default value call the method shown below on
your `ExoDorisBuilder` instance:

```java
boolean playWhenReady = false;

builder.setPlayWhenReady(playWhenReady);
```

#### userAgent

Type: `String`

Default Value: `ExoDoris`

Sets the UserAgent that will be used by the player, by default
this is set to `ExoDoris`. To modify the default value call the method
shown below on your `ExoDorisBuilder` instance:

```java
String userAgent = "CustomUserAgent";

builder.setUserAgent(userAgent);
```

### Building `ExoDoris`

Once you have set all the properties you would like to your instance of
`ExoDorisBuilder`, it's time to use the builder to build an instance of
`ExoDoris`. Where custom parameter values have not been set, the builder
will use the default values, indicated above, to build ExoDoris.
Building ExoDoris requires one line of code, illustrated below:

```java
ExoDoris player = builder.build();
```

### Source configuration

The source of the media. A `Source` can be built using the
`SourceBuilder` class. This is illustrated below.

#### id

Type: `String`

Sets the unique identifier for the source.

```java
sourceBuilder.setId(id);
```

#### extension

Type: `String`

Default Value: `null`

Sets the content-type of the media, by default this is set to `null`
(meaning the content-type will be inferred from the `URI`).

```java
String extension = "mpd";
sourceBuilder.setExtension(extension);
```

#### videoType

Type: `VideoType`

Default Value: `VOD`

Sets the `VideoType` of the media, by default this is set to `VOD`. A
`VideoType` is an `enum` consisting of two possible values: `VOD` or
`LIVE`. To modify the default value call the method shown below on your
`SourceBuilder` instance:

```java
VideoType videoType = VideoType.LIVE;
sourceBuilder.setVideoType(videoType);
```

#### mediaItem

Type: `MediaItem`

Sets the stream to be played back, which can be conveniently built using
[MediaItem.Builder](https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/MediaItem.Builder.html). 

##### Simple media item

```java
sourceBuilder.setUrl(videoUrl);
```

##### DRM protected content

```java
sourceBuilder.setDrmParams(actionToken);
```
or
```java
requestHeaders.put("Authorization", "Bearer eyJhbGciOiJIUzI1Ni...");
MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
	.setDrmLicenseUri(licenseUri)
	.setDrmUuid(C.WIDEVINE_UUID)
	.setDrmLicenseRequestHeaders(requestHeaders);
sourceBuilder.setMediaItemBuilder(mediaItemBuilder);
```

##### Sideloading subtitles

```java
sourceBuilder.setTextTracks(textTracks);
```
or
```java
subtitles.add(new MediaItem.Subtitle(subtitleUrl, MimeTypes.TEXT_VTT, "en-US", 0, 0, "English"));
MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
	.setSubtitles(subtitles);
sourceBuilder.setMediaItemBuilder(mediaItemBuilder);
```

##### Client side ad insertion, CSAI

```java
MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
	.setAdTagUri(adTag);
sourceBuilder.setMediaItemBuilder(mediaItemBuilder);
```

##### From an existed media item

```java
sourceBuilder.setMediaItem(mediaItem);
```

#### imaDaiProperties

Type: `ImaDaiProperties`

Default Value: `null`

Sets the IMA DAI properties for the source, which can be conveniently
built using ImaDaiPropertiesBuilder, by default this is set to `null`
(meaning the media is not IMA DAI stream). 

##### Live content

```java
ImaDaiProperties imaDaiProperties = new ImaDaiPropertiesBuilder()
	.setAssetKey(ASSET_KEY)
	.build();
sourceBuilder.setImaDaiProperties(imaDaiProperties);
```

##### VOD content

```java
ImaDaiProperties imaDaiProperties = new ImaDaiPropertiesBuilder()
	.setContentSourceId(contentSourceId)
	.setVideoId(videoId)
	.build();
sourceBuilder.setImaDaiProperties(imaDaiProperties);
```

##### authToken

Type: `String`

Sets the authentication token required to authenticate with the IMA SDK.

```java
imaDaiPropertiesBuilder.setAuthToken(authToken);
```

##### apiKey

Type: `String`

Sets the API key required to authenticate with the IMA SDK.

```java
imaDaiPropertiesBuilder.setApiKey(apiKey);
```

##### fallbackUri

Type: `Uri`

Sets the fallback Url of the IMA stream.

```java
imaDaiPropertiesBuilder.setFallbackUrl(fallbackUri);
```

##### adTagParameters

Type: `AdTagParameters`

Sets the ad tag parameters that will be sent to the IMA SDK when
loading a stream for playback.

```java
imaDaiPropertiesBuilder.setAdTagParameters(adTagParameters);
```

##### adTagParametersValidFrom

Type: `long`

Sets the epoch time from when the provided ad tag parameters are valid.

```java
imaDaiPropertiesBuilder.setAdTagParametersValidFrom(adTagParametersValidFrom);
```

##### adTagParametersValidUntil

Type: `long`

Sets the epoch time until which the provided ad tag parameters are valid.

```java
imaDaiPropertiesBuilder.setAdTagParametersValidUntil(adTagParametersValidUntil);
```

#### muxProperties

Type: `MuxProperties`

Default Value: `null`

Sets the metadata to be sent to Mux. Mux also requires a reference
to the view on which the video content will be rendered.

```java
sourceBuilder.setMuxProperties(data, videoSurfaceView);
```

#### contentType

Type: `C.ContentType`

Sets the content-type of the media.

```java
@C.ContentType int contentType = C.TYPE_DASH;
sourceBuilder.setContentType(contentType);
```

#### shouldPlayOffline

Type: `boolean`

Set this to true if you are playing back downloaded media.

```java
sourceBuilder.setShouldPlayOffline(true);
```

#### offlineLicenseUri

Type: `Uri`

Sets the offline license url of the media.

```java
sourceBuilder.setOfflineLicenseUrl(offlineLicenseUrl);
```

#### audioThumbnailUri

Type: `Uri`

Sets the thumbnail URL of the media. This will be displayed in the
persistent notification, if the audio only extension is used for
playback. By default this is set to `null`. To modify the default value  
call the method shown below on your `SourceBuilder` instance:

```java
String thumbnailUrl = "www.google.com/img/thumbnail.jpg";
sourceBuilder.setThumbnailUrl(thumbnailUrl);
```

#### audioTitle

Type: `String`

Default Value: `null`

Sets the title of the media, by default this is set to `null`. To modify
the default value call the method shown below on your `SourceBuilder`
instance:

```java
String title = "Big Buck Bunny";
sourceBuilder.setTitle(title);
```

#### maxVideoWidth, maxVideoHeight

Type: `int`

Sets the maximum allowed video width and height.

```java
sourceBuilder.setMaxVideoSize(maxVideoWidth, maxVideoHeight);
```

#### initialWindowIndex

Type: `int`

Sets the index of the window that will initially be used by the player.

```java
sourceBuilder.setInitialWindowIndex(windowIndex);
```

#### initialPosition

Type: `long`

Sets the initial position that the player will seek to before
starting playback.

```java
sourceBuilder.setInitialPlaybackPosition(position);
```

### Building a `Source`

Once you have set all the properties you would like to your instance of
`SourceBuilder`, it's time to use the builder to build a `Source`. Where
custom parameter values have not been set, the builder will use the default
values. Building ExoDoris requires one line of code, illustrated below:

```java
Source source = builder.build();
```

### Loading a `Source`

We load a stream using the `load` method that can be called on the player
instance. Calling the `load` method will both load up the stream and
start playback automatically by default (this behaviour can be changed, 
see [playWhenReady](#playwhenready)).

**PlayerActivity.java**
```java
player.load(source);
```

## Seeking and DVR

Seeking for VODs is simple, `0` is the start of the video, and `0 + the video duration` is the end.
This is much trickier for live. _Sometimes_ we know where the start is, but we don't know where the end is... it's live!

### Types of DVR window

* Growing window, event style - This is where video is only being appended to the end of the stream, the beginning remains constant. This can either be by design, with an [event](https://tools.ietf.org/html/draft-pantos-hls-rfc8216bis-04#section-4.4.3.5) style playlist, or because the live stream has not yet exceeded its maximum duration.

* Sliding window - A sliding window is when the live stream has exceeded a predetermined duration, video is not only being added to the end of the stream, but it is also removed from the beginning.

![Screen Shot 2019-08-22 at 15 03 16](https://user-images.githubusercontent.com/230559/63524533-14b54880-c4f4-11e9-8ba5-a0ef4be81fa4.png)

In ExoDoris seeking to `0` always seeks the video to a safe live point
safe being defined by the relevant stream specification (i.e. for Hls 3
target durations). Ten minutes from live is represented by `-600`, i.e.
negative 600 seconds from live. Because ExoDoris uses a safe live point,
the seekable range of the video will always be slightly shorter than the
entire stream, this is to avoid client rebuffering.

## Digital Rights Management (DRM)

ExoDoris ships with inbuilt support for the Widevine DRM system for HLS and DASH
streams.

### Load a DRM protected stream

**PlayerActivity.java**
```java
requestHeaders.put("Authorization", "Bearer eyJhbGciOiJIUzI1Ni...");
MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
	.setUri(videoUrl)
	.setDrmLicenseUri(licenseUri)
	.setDrmUuid(C.WIDEVINE_UUID)
	.setDrmLicenseRequestHeaders(requestHeaders);
Source source = new SourceBuilder()
	.setMediaItemBuilder(mediaItemBuilder)
	.build();
player.load(source);
```

## ExoDoris Audio Only Extension

ExoDoris ships with an extension to add audio only playback support to the player. Audio only playback happens in a foreground Android service, allowing
playback to continue even after the app has been backgrounded or the user has locked their device. 

### Add ExoDoris audio-only extension dependency

To add the ExoDoris audio-only extension to your project, simply add the following line to the `build.gradle` file of your app module: (Note: The ExoDoris audio-only extension needs the core ExoDoris dependency to be added to your `build.gradle` file as well)

```gradle
dependencies {
    ...
    implementation "com.github.DiceTechnology.doris-android:extension-audio-only:0.X.X"
}
```
where 0.X.X is your preferred version.

### Define the audio service in the Android manifest

In order to playback audio-only streams while your app is backgrounded, the ExoDoris audio-only
extension runs in a seperate Android foreground service. This service needs to be defined in
the root `AndroidManifest.xml` file of your app. Add the following lines to your app's 
`AndroidManifest.xml` file to define the audio service:

**AndroidManifest.xml**
```xml
<service
    android:name="com.diceplatform.doris.ext.audioonly.AudioService"
    android:enabled="true"
    android:exported="true"
    tools:ignore="ExportedService">
</service>
```

### Create and start the audio service

In order to create the audio-only foreground service, that will play audio only streams while
your app is backgrounded, you need to add the following lines:

**PlayerActivity.java**
```java
private AudioService audioService;
private ExoDoris player;

private ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        AudioService.AudioServiceBinder binder = (AudioService.AudioServiceBinder) iBinder;
        audioService = binder.getService();
        initializePlayer();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }
};

@Override
protected void onCreate(Bundle savedInstanceState) {
    ...
    Intent intent = new Intent(this, AudioService.class);
    Util.startForegroundService(themedReactContext, intent);
    this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
}

private void initializePlayer() {
    player = audioService.getPlayerInstance();
}

private void releasePlayer() {
    ...
    this.unbindService(serviceConnection);
}
```

The ServiceConnection listens to the service and has callback methods for when the servicde is 
connected and disconnected. We need to wait until the service has successfully been connected
before initialising the player.

The audio service creates its own player instance, so there's no need to create a player
instance in the `initializePlayer()` method. To get a reference to the player instance
that the audio service is using, we can call `audioService.getPlayerInstance()`.

### Load an audio-only stream

Loading an audio-only stream is very similar to loading a standard video stream. Except, instead of using the player itself to load streams, we use the audio-only service to load the streams. The audio service's load method requires a `Source` object to be passed to it.
More information on how to build a `Source` object can be found in the
[Source configuration](#source-configuration) section. Calling the
`load` method will both load up the stream and start playback
automatically by default (this behaviour can be changed, see
[playWhenReady](#playwhenready)).

**PlayerActivity.java**
```java
audioOnlyService.load(source);
```

## ExoDoris IMA DAI Extension

ExoDoris ships with an extension to add IMA DAI support to the player.

### Add ExoDoris IMA DAI extension dependency

To add the ExoDoris IMA DAI extension to your project, simply add the following line to the `build.gradle` file of your app module: (Note: The ExoDoris IMA DAI extension needs the core ExoDoris dependency to be added to your `build.gradle` file as well)

```gradle
dependencies {
    ...
    implementation "com.github.DiceTechnology.doris-android:extension-ima-dai:0.X.X"
}
```
where 0.X.X is your preferred version.

### Create the ExoDoris IMA DAI Player

In order to load IMA DAI streams we require ExoDorisImaDaiPlayer.
These can be instantiated as illustrated in the code snippet below:

**PlayerActivity.java**
```java
private ExoDorisImaDaiPlayer exoDorisImaDaiPlayer;

private void initializePlayer() {
    exoDorisImaDaiPlayer = new ExoDorisImaDaiPlayer(context, playerView, playerView.getAdViewGroup());
}

private void releasePlayer() {
    ...
    if (exoDorisImaDaiPlayer != null) {
        exoDorisImaDaiPlayer.release();
        exoDorisImaDaiPlayer = null;
    }
}
```

### Load a IMA DAI stream

To load a IMA DAI stream we need to create an source with ImaDaiProperties.  

**PlayerActivity.java**
```java
AdTagParameters adTagParameters = new AdTagParametersBuilder()
	.setIu(iu)
	.setCustParams(custParams)
	.setOutput(output)
	.setVpa(vpa)
	.setMsid(msid)
	.setAn(an)
	.setIsLat(isLat)
	.setDescriptionUrl(descriptionUrl)
	.setUrl(url)
	.build();

ImaDaiProperties imaDaiProperties = source.getImaDaiProperties();
imaDaiProperties.setAdTagParameters(adTagParameters);
imaDaiProperties.setAdTagParametersValidFrom(startDate);
imaDaiProperties.setAdTagParametersValidUntil(endDate);

if (imaDaiProperties.isVod()) {
    exoDorisImaDaiPlayer.enableControls(true);
    exoDorisImaDaiPlayer.setCanSeek(true);
}

exoDorisImaDaiPlayer.load(source);
```

## ExoDoris IMA CSAI Extension

ExoDoris ships with an extension to add IMA CSAI support to the player.

### Add ExoDoris IMA CSAI extension dependency

To add the ExoDoris IMA CSAI extension to your project, simply add the following line to the `build.gradle` file of your app module: (Note: The ExoDoris IMA CSAI extension needs the core ExoDoris dependency to be added to your `build.gradle` file as well)

```gradle
dependencies {
    ...
    implementation "com.github.DiceTechnology.doris-android:extension-ima-csai:0.X.X"
}
```
where 0.X.X is your preferred version.

### Create the ExoDoris IMA CSAI Player

In order to load IMA CSAI streams we require ExoDorisImaCsaiPlayer.
These can be instantiated as illustrated in the code snippet below:

**PlayerActivity.java**
```java
private ExoDorisImaCsaiPlayer exoDorisImaCsaiPlayer;

private void initializePlayer() {
    exoDorisImaCsaiPlayer = new ExoDorisImaCsaiPlayer(context, playerView);
}

private void releasePlayer() {
    ...
    if (exoDorisImaCsaiPlayer != null) {
        exoDorisImaCsaiPlayer.release();
        exoDorisImaCsaiPlayer = null;
    }
}
```

### Load a IMA CSAI stream

To load a IMA CSAI stream we need to create an source with MediaItem.AdsConfiguration.  

**PlayerActivity.java**
```java
MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
	.setUri(videoUrl)
	.setAdTagUri(adTag);
Source source = new SourceBuilder()
	.setMediaItemBuilder(mediaItemBuilder)
	.build();
exoDorisImaCsaiPlayer.load(source);
```
