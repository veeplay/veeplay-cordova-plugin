var exec = require('cordova/exec');

exports.playerId = "player";
exports.timerId = 0;

exports.getBounds = function() {
    var rect = document.getElementById(window.veeplay.playerId).getBoundingClientRect();
    exec(null, null, "veeplay-cordova-plugin", "getBounds", [rect.top, rect.right, rect.bottom, rect.left]);
}

exports.appStarted = function() {
//    document.addEventListener("touchmove", window.veeplay.getBounds, false);
    exec(null, null, "veeplay-cordova-plugin", "appStarted", []);
};

exports.configureCastSettings = function(castConfiguration, success, error) {
    var playText;
    if(castConfiguration.hasOwnProperty('playText')) {
        playText = castConfiguration.playText;
    }
    var pauseText;
    if(castConfiguration.hasOwnProperty('pauseText')) {
        pauseText = castConfiguration.pauseText;
    }
    var disconnectText;
    if(castConfiguration.hasOwnProperty('disconnectText')) {
        disconnectText = castConfiguration.disconnectText;
    }
    var appName;
    if(castConfiguration.hasOwnProperty('appName')) {
        appName = castConfiguration.appName;
    }
    var appId;
    if(castConfiguration.hasOwnProperty('appId')) {
        appId = castConfiguration.appId;
    }
    exec(null, null, "veeplay-cordova-plugin", "configureCastSettings", [playText, pauseText, disconnectText, appName, appId]);
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
    var jsonUrl = arg0;
    var rect;
    if(typeof arg0 === 'string') {
        console.log("Playing URL by bounding a div");
        window.veeplay.timerId = setInterval(window.veeplay.getBounds, 20);
        console.log("Added bounded timer with id:  "+window.veeplay.timerId);
        rect = document.getElementById(window.veeplay.playerId).getBoundingClientRect();
        exec(internalBridgeCall, function() {}, "veeplay-cordova-plugin", "bindInternalBridge", []);
    } else {
        if(!arg0.hasOwnProperty('xPosition') || !arg0.hasOwnProperty('yPosition') || !arg0.hasOwnProperty('width') || !arg0.hasOwnProperty('height') || !arg0.hasOwnProperty('jsonUrl')) {
            console.log("The object should have the following properties: xPosition, yPosition, width, height, jsonUrl");
            error();
            return;
        }
        jsonUrl = arg0.jsonUrl;
        rect = {
            'top': arg0.yPosition,
            'right': arg0.xPosition+arg0.width,
            'bottom': arg0.yPosition+arg0.height,
            'left': arg0.xPosition
        };
        console.log("Playing URL with XYWH coordinates: "+JSON.stringify(rect));
    }
    exec(success, error, "veeplay-cordova-plugin", "playFromUrl", [jsonUrl, rect.top, rect.right, rect.bottom, rect.left]);
};

exports.playFromObject = function(arg0, success, error) {
    internalBridgeCall("stopBoundingTimer");
    var rect;
    if(!arg0.hasOwnProperty('cordovaConfig')) {
        window.veeplay.timerId = setInterval(window.veeplay.getBounds, 20);
        console.log("Added bounded timer with id:  "+window.veeplay.timerId);
        rect = document.getElementById(window.veeplay.playerId).getBoundingClientRect();
        exec(internalBridgeCall, function() {}, "veeplay-cordova-plugin", "bindInternalBridge", []);
    } else {
        var cordovaConfig = arg0.cordovaConfig;
        if( !cordovaConfig.hasOwnProperty('xPosition') ||
            !cordovaConfig.hasOwnProperty('yPosition') ||
            !cordovaConfig.hasOwnProperty('width') ||
            !cordovaConfig.hasOwnProperty('height')) {
                console.log("The cordovaConfig object should have the following required properties: xPosition, yPosition, width, height");
                error();
                return;
        }
        rect = {
            'top': cordovaConfig.yPosition,
            'right': cordovaConfig.xPosition+cordovaConfig.width,
            'bottom': cordovaConfig.yPosition+cordovaConfig.height,
            'left': cordovaConfig.xPosition
        };
        console.log("Playing URL with XYWH coordinates: "+JSON.stringify(rect));
    }

    exec(success, error, "veeplay-cordova-plugin", "playFromObject", [JSON.stringify(arg0), rect.top, rect.right, rect.bottom, rect.left]);
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