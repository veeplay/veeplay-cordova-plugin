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
    exec(success, error, "veeplay-cordova-plugin", "fullscreenPlayFromObject", [arg0]);
};

exports.playFromUrl = function(arg0, success, error) {
    window.veeplay.timerId = setInterval(window.veeplay.getBounds, 20);
    console.log("Timer:  "+window.veeplay.timerId);
    var rect = document.getElementById(window.veeplay.playerId).getBoundingClientRect();
    exec(success, error, "veeplay-cordova-plugin", "playFromUrl", [arg0, rect.top, rect.right, rect.bottom, rect.left]);
    exec(internalBridgeCall, function() {}, "veeplay-cordova-plugin", "bindInternalBridge");
};

exports.playFromObject = function(arg0, success, error) {
    exec(success, error, "veeplay-cordova-plugin", "playFromObject", [arg0]);
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

function internalBridgeCall(result) {
    if(result == "stopBoundingTimer") {
        console.log("Stopping bounding timers");
        clearInterval(window.veeplay.timerId);
    }
}
