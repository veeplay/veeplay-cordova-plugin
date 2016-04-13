var exec = require('cordova/exec');

exports.playerId = "player";
exports.timerId = 0;

exports.getBounds = function() {
    var rect = document.getElementById(window.veeplay.playerId).getBoundingClientRect();
    exec(null, null, "veeplay-cordova-plugin", "getBounds", [rect.top, rect.right, rect.bottom, rect.left]);
}

exports.fullscreenPlayFromUrl = function(jsonUrl, success, error) {
//    document.addEventListener("touchmove", window.veeplay.getBounds, false);
    exec(success, error, "veeplay-cordova-plugin", "fullscreenPlayFromUrl", [jsonUrl]);
};

exports.fullscreenPlayFromObject = function(arg0, success, error) {
    exec(success, error, "veeplay-cordova-plugin", "fullscreenPlayFromObject", [JSON.stringify(arg0)]);
};

exports.playFromUrl = function(arg0, success, error) {
    internalBridgeCall("stopBoundingTimer");
    window.veeplay.timerId = setInterval(window.veeplay.getBounds, 20);
    console.log("Added bounded timer with id:  "+window.veeplay.timerId);
    var rect = document.getElementById(window.veeplay.playerId).getBoundingClientRect();
    exec(success, error, "veeplay-cordova-plugin", "playFromUrl", [arg0, rect.top, rect.right, rect.bottom, rect.left]);
    exec(internalBridgeCall, function() {}, "veeplay-cordova-plugin", "bindInternalBridge");
};

exports.playFromObject = function(arg0, success, error) {
    internalBridgeCall("stopBoundingTimer");
    window.veeplay.timerId = setInterval(window.veeplay.getBounds, 20);
    console.log("Added bounded timer with id:  "+window.veeplay.timerId);
    var rect = document.getElementById(window.veeplay.playerId).getBoundingClientRect();
    exec(success, error, "veeplay-cordova-plugin", "playFromObject", [JSON.stringify(arg0), rect.top, rect.right, rect.bottom, rect.left]);
    exec(internalBridgeCall, function() {}, "veeplay-cordova-plugin", "bindInternalBridge");
};

exports.stop = function(success, error) {
    internalBridgeCall("stopBoundingTimer");
    exec(success, error, "veeplay-cordova-plugin", "stop", []);
};

exports.pause = function(success, error) {
    exec(success, error, "veeplay-cordova-plugin", "pause", []);
};

exports.resume = function(success, error) {
    exec(success, error, "veeplay-cordova-plugin", "resume", []);
};

exports.getDuration = function(success, error) {
	exec(success, error, "veeplay-cordova-plugin", "duration", []);
};

exports.getBufferedTime = function(success, error) {
	exec(success, error, "veeplay-cordova-plugin", "bufferedTime", []);
};

exports.toggleFullscreen = function(success, error) {
	exec(success, error, "veeplay-cordova-plugin", "toggleFullscreen", []);
};

exports.mute = function(success, error) {
	exec(success, error, "veeplay-cordova-plugin", "mute", []);
};

exports.unMute = function(success, error) {
	exec(success, error, "veeplay-cordova-plugin", "unMute", []);
};

exports.isPlaying = function(success, error) {
	exec(success, error, "veeplay-cordova-plugin", "isPlaying", []);
};

exports.isSeeking = function(success, error) {
	exec(success, error, "veeplay-cordova-plugin", "isSeeking", []);
};

exports.skip = function(success, error) {
	exec(success, error, "veeplay-cordova-plugin", "skip", []);
};

exports.back = function(success, error) {
	exec(success, error, "veeplay-cordova-plugin", "back", []);
};

function internalBridgeCall(result) {
    if(result == "stopBoundingTimer" && window.veeplay.timerId != 0) {
        console.log("Stopping bounding timers");
        clearInterval(window.veeplay.timerId);
        window.veeplay.timerId = 0;
    }
}