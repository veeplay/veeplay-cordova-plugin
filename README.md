## Veeplay Cordova Plugin

###Overview

The Veeplay Cordova plugin exposes the native functionalities of the Veeplay SDK inside a Cordova application. Currently the Android platform is supported, with iOS being planned for release in April 2016.

The plugin allows content video units and ads to be rendered by configuring the player using the Veeplay JSON configuration DSL (documentation can be found here: [http://veeplay.github.io/json-docs/]()). The JSON configuration can be passed to the player either as a Javascript object or via an URL linking to a JSON configuration file.

###Instalation

    cordova plugin add https://github.com/veeplay/veeplay-cordova-plugin.git

###Usage and Javascript API

All of the plugins methods are grouped under the veeplay clobber. As such, you can call any method using code similar to:
    
    window.veeplay.<method_name>

####Playing videos inline (inside an HTML element)

In order to add itself on the screen, when requested to play, the player will look for an HTML element with the "player" ID and will bind itself to this element. Before calling playFromUrl or playFromObject, add such an element to the DOM. Example:

     <object width="225" height="120" style="background-color: #000000; margin-top: 20px;" id="player">

####fullscreenPlayFromUrl(jsonUrl, successCallback, errorCallback)

Calling this method will display a fullscreen overlay over the Cordova application, with the player centered inside.

The player configuration data is retrieved from the URL specified in the jsonUrl variable.

All player status events are notified inside the successCallback(result) function.

	window.veeplay.playFromUrl("JSONUrl", function(result) {
	console.log("New player event: "+result);
	});

####fullscreenPlayFromObject(jsonObject, successCallback, errorCallback)

Calling this method will display a fullscreen overlay over the Cordova application, with the player centered inside.

The player configuration data is retrieved from the JSON object passed in the jsonObject variable.

All player status events are notified inside the successCallback(result) function.

####playFromUrl(jsonUrl, successCallback, errorCallback)

Calling this method will position the player inside the HTML element with the "player" ID and start playback using the configuration retrieved from jsonUrl.

All player status events are notified inside the successCallback(result) function.

####playFromObject(jsonObject, successCallback, errorCallback)

Calling this method will position the player inside the HTML element with the "player" ID and start playback using the configuration inside the jsonObject object.

All player status events are notified inside the successCallback(result) function.

####stop(successCallback, errorCallback)

Calling this will stop the current playlist and reset the player.

####pause(successCallback, errorCallback)

Calling this will pause playback temporarily, if the player was playing.

####resume(successCallback, errorCallback)

Calling this method will resume playback if it was paused.

####getDuration(successCallback, errorCallback)

Calling this method will return the duration, in ms, of the currently playing video, inside the successCallback function. If the duration is not currently available, 0 will be returned.

####getBufferedTime(successCallback, errorCallback)

Calling this method will return the length of the buffer, in ms, inside the successCallback function. 

####toggleFullscreen(successCallback, errorCallback)

Calling this method will toggle full screen mode on and off.

####mute(successCallback, errorCallback)

This method will mute audio, if playback is running.

####unMute(successCallback, errorCallback)

This method will unmute audio, if playback is running.

####isPlaying(successCallback, errorCallback)

This method will call the successCallback method with "true" or "false", depending if the Veeplay player is currently rendering a video unit.

####isSeeking(successCallback, errorCallback)

This method will call the successCallback method with "true" or "false", depending if the Veeplay player is currently processing a seek operation.

####skip(successCallback, errorCallback)

This method will stop the playback of the current unit and start playback of the next unit in the playlist.

####back(successCallback, errorCallback)

This method will stop the playback of the current unit and start playback of the previous unit in the playlist.

###Uninstall

    cordova plugin remove veeplay-cordova-plugin

###Licensing

The Veeplay Cordova plugin is available as an open-source component, under the Apache 2.0 license. Usage of the Veeplay SDK however is dependant on having a valid Veeplay license (you can sign up for a trial license at https://panel.veeso.co).