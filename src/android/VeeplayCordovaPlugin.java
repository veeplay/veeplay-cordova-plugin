package com.veeplay.cordova;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.appscend.media.APSMediaBuilder;
import com.appscend.media.APSMediaPlayer;
import com.appscend.media.APSMediaTrackingEvents;
import com.appscend.utilities.APSMediaPlayerTrackingEventListener;
import com.appscend.utilities.VPUtilities;
import com.veeplay.cast.APSMediaRouteButtonOverlayController;
import com.veeplay.cast.GoogleCastRenderer;
import com.veeplay.cast.VeeplayCastConfiguration;
import com.veeplay.cast.VeeplayCastManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * This class echoes a string called from JavaScript.
 */
public class VeeplayCordovaPlugin extends CordovaPlugin implements DialogInterface.OnCancelListener, APSMediaPlayerTrackingEventListener {

    private Dialog fullscreenPlayerDialog;
    private CallbackContext mainCallbackContext;
    private CallbackContext internalBridgeContext;
    private CallbackContext eventsTrackingContext;
    private static String lastAction;
    private static JSONArray lastArgs;
    private static CallbackContext savedEventsTrackingContext;
    private VeeplayCastConfigurationWrapper castConfigurationObject;
    ViewGroup cordovaParent;
    RelativeLayout playerContainer;
    private int topOffset = 0;
    private int leftOffset = 0;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        cordovaParent = (FrameLayout) cordova.getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        if(playerContainer != null && cordovaParent !=null) {
            Log.d("VeeplayPlayer", "old container found");
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cordovaParent.removeView(playerContainer);
                }
            });
        }
        if(savedEventsTrackingContext != null) eventsTrackingContext = savedEventsTrackingContext;
        playerContainer = new RelativeLayout(cordova.getActivity());
        playerContainer.setTag("VeeplayContainer");
        Log.d("VeeplayPlayer", "set tag on container view");
        playerContainer.setBackgroundColor(Color.BLACK);
        if(APSMediaPlayer.getInstance().isRenderingToGoogleCast()) {
            Log.d("VeeplayPlayer", "We should put the player back on screen");
            try {
                execute(lastAction, lastArgs, mainCallbackContext);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("VeeplayPlayer", "We were initialized");
        }
    }

    public void onStart() {
        Log.d("CordovaVeeplay", "Start activity");
        if(APSMediaPlayer.getInstance().isPaused()) {
            APSMediaPlayer.getInstance().resumePlay();
        }
    }

    public void onStop() {
        Log.d("CordovaVeeplay", "Stop activity");
        if(APSMediaPlayer.getInstance().isPlaying() && !APSMediaPlayer.getInstance().isRenderingToGoogleCast()) {
            APSMediaPlayer.getInstance().pause();
        }
    }

    public void onResume(boolean multitasking) {
        Log.d("CordovaVeeplay", "Resume activity");
    }

    public void onPause(boolean multitasking) {
        Log.d("CordovaVeeplay", "Pause activity");
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        //noinspection IfCanBeSwitch using if instead of switch in order to maintain compatibility with Java 6 projects
        if (action.equals("play")) {
            int topBound = args.getInt(2);
            int leftBound = args.getInt(1);
            int width = args.getInt(3);
            int height = args.getInt(4);
            boolean fullscreen = args.getBoolean(5);
            lastAction = action;
            lastArgs = args;
            if(args.getString(0).startsWith("{")) {
                if(!fullscreen) {
                    playFromJsonData(args.getString(0), topBound, leftBound, width, height, callbackContext);
                } else {
                    fullscreenPlayFromObject(args.getString(0), callbackContext);
                }
            } else {
                if(!fullscreen) {
                    playFromJsonUrl(args.getString(0), topBound, leftBound, width, height, callbackContext);
                } else {
                    fullscreenPlayFromUrl(args.getString(0), callbackContext);
                }
            }
            return true;
        } else if (action.equals("stop")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    APSMediaPlayer.getInstance().finish();
                    if(fullscreenPlayerDialog!=null && fullscreenPlayerDialog.isShowing()) {
                        fullscreenPlayerDialog.dismiss();
                    }
                    View container = cordovaParent.findViewWithTag("VeeplayContainer");
                    if(container != null) {
                        Log.d("VeeplayPlayer", "view found - removing container");
                        cordovaParent.removeView(container);
                    } else {
                        Log.d("VeeplayPlayer", "view not found - container not removed");
                    }
                }
            });
            return true;
        } else if (action.equals("pause")) {
            if(APSMediaPlayer.getInstance().isPlaying()) {
                APSMediaPlayer.getInstance().pause();
            }
            return true;
        } else if (action.equals("resume")) {
            if(APSMediaPlayer.getInstance().isPaused()) {
                APSMediaPlayer.getInstance().resumePlay();
            }
            return true;
        } else if (action.equals("setBounds")) {
            int newTopOffset = VPUtilities.pixelsToDip(args.getInt(1), cordova.getActivity());
            int newLeftOffset = VPUtilities.pixelsToDip(args.getInt(0), cordova.getActivity());
            final int yDiff = topOffset-newTopOffset;
            final int xDiff = leftOffset-newLeftOffset;
            if(playerContainer != null && playerContainer.getParent() != null) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playerContainer.setTranslationX(-xDiff);
                        playerContainer.setTranslationY(-yDiff);
                    }
                });
            }
            return true;
        } else if(action.equals("bindInternalBridge")) {
            internalBridgeContext = callbackContext;
            return true;
        } else if(action.equals("bindEventsBridge")) {
            if(callbackContext != null) {
                eventsTrackingContext = callbackContext;
                savedEventsTrackingContext = callbackContext;
            }
            Log.d("VeeplayPlayer", "binding events bridge");
            if(callbackContext == null) {
                Log.d("VeeplayPlayer", "bind called with null");
            }
            return true;
        } else if(action.equals("isPlaying")) {
            mainCallbackContext.success(""+APSMediaPlayer.getInstance().isPlaying());
            return true;
        } else if(action.equals("isSeeking")) {
            mainCallbackContext.success(""+APSMediaPlayer.getInstance().isSeeking());
            return true;
        } else if(action.equals("skip")) {
            APSMediaPlayer.getInstance().skip();
            mainCallbackContext.success("true");
            return true;
        } else if(action.equals("back")) {
            APSMediaPlayer.getInstance().back();
            mainCallbackContext.success("true");
            return true;
        } else if(action.equals("mute")) {
            APSMediaPlayer.getInstance().setMute(true);
            mainCallbackContext.success("true");
            return true;
        } else if(action.equals("unmute")) {
            APSMediaPlayer.getInstance().setMute(false);
            mainCallbackContext.success("ok");
            return true;
        } else if(action.equals("duration")) {
            int duration = APSMediaPlayer.getInstance().duration();
            if(duration==0) {
                mainCallbackContext.error(0);
            } else {
                mainCallbackContext.success(""+duration);
            }
            return true;
        } else if(action.equals("bufferedTime")) {
            int bufferedTime = APSMediaPlayer.getInstance().playableDuration(APSMediaPlayer.getInstance().duration());
            if(bufferedTime==0) {
                mainCallbackContext.error(0);
            } else {
                mainCallbackContext.success(""+bufferedTime);
            }
            return true;
        } else if(action.equals("toggleFullscreen")) {
            APSMediaPlayer.getInstance().toggleFullscreen();
            mainCallbackContext.success("true");
            return true;
        } else if(action.equals("configureCastSettings")) {
            castConfigurationObject = new VeeplayCastConfigurationWrapper(args);
        }
        return false;
    }

    private void fullscreenPlayFromUrl(final String message, final CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            Log.d("CordovaVeeplay", "Loading dialog window");
            cordova.getThreadPool().submit(new Runnable() {
                @Override
                public void run() {
                    APSMediaBuilder builder = new APSMediaBuilder();
                    try {
                        builder.configureFromURL(new URL(message));
                        fullscreenPlay(builder);
                    }
                    catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            });
            mainCallbackContext = callbackContext;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void fullscreenPlayFromObject(final String message, final CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            Log.d("CordovaVeeplay", "Loading dialog window");
            cordova.getThreadPool().submit(new Runnable() {
                @Override
                public void run() {
                    APSMediaBuilder builder = new APSMediaBuilder();
                    builder.configureFromData(message);
                    fullscreenPlay(builder);
                }
            });
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void playFromJsonUrl(final String jsonUrl, final int top, final int left, final int width, final int height, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addPlayerContainer(top, left, width, height);
                mainCallbackContext = callbackContext;
                initVeeplay(playerContainer);

                if(APSMediaPlayer.getInstance().isRenderingToGoogleCast()) {
                    return;
                }

                cordova.getThreadPool().submit(new Runnable() {
                    @Override
                    public void run() {
                        APSMediaBuilder builder = new APSMediaBuilder();
                        try {
                            builder.configureFromURL(new URL(jsonUrl));
                        }
                        catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        APSMediaPlayer.getInstance().playMediaUnits(builder.mediaUnits());
                    }
                });

                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
    }

    private void playFromJsonData(final String jsonData, final int top, final int left, final int width, final int height, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainCallbackContext = callbackContext;

                addPlayerContainer(top, left, width, height);
                initVeeplay(playerContainer);

                APSMediaBuilder builder = new APSMediaBuilder();
                builder.configureFromData(jsonData);
                APSMediaPlayer.getInstance().playMediaUnits(builder.mediaUnits());

                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
    }

    private void fullscreenPlay(final APSMediaBuilder builder) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(fullscreenPlayerDialog != null && fullscreenPlayerDialog.isShowing()) {
                    fullscreenPlayerDialog.dismiss();
                }
                fullscreenPlayerDialog = new Dialog(cordova.getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                fullscreenPlayerDialog.setCanceledOnTouchOutside(false);
                fullscreenPlayerDialog.setOnCancelListener(VeeplayCordovaPlugin.this);

                RelativeLayout mPlayerContainer = new RelativeLayout(cordova.getActivity().getApplicationContext());
                fullscreenPlayerDialog.setContentView(mPlayerContainer);
                mPlayerContainer.setBackgroundColor(Color.BLACK);
                fullscreenPlayerDialog.show();

                initVeeplay(mPlayerContainer);

                APSMediaPlayer.getInstance().showHud();
                APSMediaPlayer.getInstance().playMediaUnits(builder.mediaUnits());

            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                APSMediaPlayer.getInstance().finish();
            }
        });
    }

    @Override
    public void onTrackingEventReceived(APSMediaTrackingEvents.MediaEventType mediaEventType, Bundle bundle) {
        if(mediaEventType == APSMediaTrackingEvents.MediaEventType.PLAYLIST_FINISHED) {
            Log.d("VeeplayPlayer", "finish event received");
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View container = cordovaParent.findViewWithTag("VeeplayContainer");
                    if(container != null) {
                        Log.d("VeeplayPlayer", "container view found");
                    } else {
                        Log.d("VeeplayPlayer", "container view not found");
                    }
                    cordovaParent.removeView(container);
                }
            });

            if(internalBridgeContext != null) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "stopBoundingTimer");
                pluginResult.setKeepCallback(true);
                mainCallbackContext.sendPluginResult(pluginResult);

            }
        }
        if(eventsTrackingContext != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, new JSONObject(generatePlayerEventHashMap(mediaEventType)));
            pluginResult.setKeepCallback(true);
            eventsTrackingContext.sendPluginResult(pluginResult);
            Log.d("VeeplayPlayer", "sent callback");
        } else {
            Log.d("VeeplayPlayer", "events tracking context null");
        }
//        mainCallbackContext.success(mediaEventType.toString());
    }

    private void addPlayerContainer(int top, int left, int width, int height) {

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = VPUtilities.pixelsToDip(left, cordova.getActivity());
        params.topMargin = VPUtilities.pixelsToDip(top, cordova.getActivity());
        topOffset = VPUtilities.pixelsToDip(top, cordova.getActivity());
        leftOffset = VPUtilities.pixelsToDip(left, cordova.getActivity());
        params.width = VPUtilities.pixelsToDip(width, cordova.getActivity());
        params.height = VPUtilities.pixelsToDip(height, cordova.getActivity());
        playerContainer.setLayoutParams(params);
        if(playerContainer.getParent()!=null) {
            ((ViewGroup)playerContainer.getParent()).removeView(playerContainer);
        }
        cordovaParent.addView(playerContainer);
    }

    private void initVeeplay(ViewGroup playerParent) {
        if(!APSMediaPlayer.getInstance().isRenderingToGoogleCast()) {
            APSMediaPlayer.getInstance().finish();
            APSMediaPlayer.getInstance().init(cordova.getActivity(), true);
            APSMediaPlayer.getInstance().removeAllTrackingEventListeners();
            APSMediaPlayer.getInstance().addTrackingEventListener(VeeplayCordovaPlugin.this);
            if(eventsTrackingContext == null && savedEventsTrackingContext != null) {
                eventsTrackingContext = savedEventsTrackingContext;
            }
            initCastSupport();
        }
        //obtain a reference to the main player view
        ViewGroup veeplayViews = APSMediaPlayer.getInstance().viewController();

        //check if the player was previously attached somewhere else
        if(veeplayViews.getParent() != null) {
            ((ViewGroup)veeplayViews.getParent()).removeView(veeplayViews);
        }
        playerParent.addView(veeplayViews);
        APSMediaPlayer.getInstance().showHud();
    }

    private void initCastSupport() {
        if(castConfigurationObject==null) castConfigurationObject = new VeeplayCastConfigurationWrapper();
        VeeplayCastConfiguration castConfiguration = new VeeplayCastConfiguration.Builder()
        .setCastNotificationPlayText(castConfigurationObject.getPlayText())
        .setCastNotificationPauseText(castConfigurationObject.getPauseText())
        .setCastNotificationDisconnectText(castConfigurationObject.getDisconnectText())
//        .setCastNotificationSmallIcon(R.drawable.ic_cast_on_dark)
//        .setCastNotificationLargeIcon(R.mipmap.icon)
        .setCastNotificationTitle(castConfigurationObject.getAppName())
        .build();
        VeeplayCastManager.getInstance().init(cordova.getActivity(), castConfigurationObject.getAppId(), castConfiguration);

        APSMediaPlayer.getInstance().registerClassInGroup(
            GoogleCastRenderer.class,
            APSMediaPlayer.kAPSMediaPlayerRenderersGroup,
            GoogleCastRenderer.rendererIdentifier
        );

        APSMediaPlayer.getInstance().registerClassInGroup(
            APSMediaRouteButtonOverlayController.class,
            APSMediaPlayer.kAPSMediaPlayerOverlayControllersGroup,
            APSMediaRouteButtonOverlayController.APSMediaRouteOverlay
        );

        APSMediaPlayer.getInstance().setGoogleCastEnabled();
    }

    private HashMap<String, Object> generatePlayerEventHashMap(APSMediaTrackingEvents.MediaEventType eventType) {
        //noinspection Convert2Diamond - the Cordova project isn't yet compatible with Java 7 features.
        HashMap<String, Object> playerEvent = new HashMap<String, Object>();
        playerEvent.put("type", eventType.toString());

        if(eventType == APSMediaTrackingEvents.MediaEventType.ERROR) {
            playerEvent.put("error", true);
        } else {
            playerEvent.put("error", false);
        }

        if(eventType == APSMediaTrackingEvents.MediaEventType.REWIND || eventType == APSMediaTrackingEvents.MediaEventType.FORWARD) {
            playerEvent.put("seek_start", true);
        } else {
            playerEvent.put("seek_start", false);
        }

        if(APSMediaPlayer.getInstance().isPlaying() || APSMediaPlayer.getInstance().isPaused()) {
            playerEvent.put("playback_time", APSMediaPlayer.getInstance().currentPlaybackTime());
        } else {
            playerEvent.put("playback_time", null);
        }

        if(APSMediaPlayer.getInstance()._currentUnit==null) {
            playerEvent.put("current_unit_is_ad", false);
        } else {
            if(APSMediaPlayer.getInstance()._currentUnit.metadata==null) {
                playerEvent.put("current_unit_is_ad", false);
            } else {
                if(APSMediaPlayer.getInstance()._currentUnit.metadata.containsKey("aps_vast_adbreak_index")) {
                    playerEvent.put("current_unit_is_ad", true);
                } else {
                    playerEvent.put("current_unit_is_ad", false);
                }
            }
        }

        return playerEvent;
    }
}
