package com.veeplay.cordova;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.appscend.media.APSMediaBuilder;
import com.appscend.media.APSMediaPlayer;
import com.appscend.media.APSMediaTrackingEvents;
import com.appscend.utilities.APSMediaPlayerTrackingEventListener;
import com.appscend.utilities.VPUtilities;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class echoes a string called from JavaScript.
 */
public class VeeplayCordovaPlugin extends CordovaPlugin implements DialogInterface.OnCancelListener, APSMediaPlayerTrackingEventListener {

    private Dialog fullscreenPlayerDialog;
    private CallbackContext eventsCallbackContext;
    private CallbackContext internalBridgeContext;
    ViewGroup cordovaParent;
    RelativeLayout playerContainer;
    private int topOffset = 130;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        cordovaParent = ((ViewGroup) webView.getView().getParent());
        playerContainer = new RelativeLayout(cordova.getActivity());
        playerContainer.setBackgroundColor(Color.BLACK);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // using if instead of switch in order to maintain compatibility with Java 6 projects
        if (action.equals("fullscreenPlayFromUrl")) {
            String message = args.getString(0);
            this.fullscreenPlayFromUrl(message, callbackContext);
            return true;
        } else if (action.equals("fullscreenPlayFromObject")) {
            String message = args.getString(0);
            this.fullscreenPlayFromObject(message, callbackContext);
            return true;
        } else if (action.equals("playFromUrl")) {
            String message = args.getString(0);
            play(message, args.getInt(1), args.getInt(4), 225, 120, callbackContext);
            return true;
        } else //noinspection StatementWithEmptyBody
            if (action.equals("playFromObject")) {
            //TODO add support
        } else if (action.equals("stop")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    APSMediaPlayer.getInstance().finish();
                    if(fullscreenPlayerDialog!=null && fullscreenPlayerDialog.isShowing()) {
                        fullscreenPlayerDialog.dismiss();
                    }
                    if(playerContainer!=null && playerContainer.getParent()!=null) {
                        cordovaParent.removeView(playerContainer);
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
        } else if (action.equals("getBounds")) {
            int newTopOffset = VPUtilities.pixelsToDip(args.getInt(0), cordova.getActivity());
            int diff = topOffset-newTopOffset;
            if(playerContainer != null) {
                playerContainer.setTranslationY(-diff);
            }
            return true;
        } else if(action.equals("bindInternalBridge")) {
            internalBridgeContext = callbackContext;
            return true;
        }

        return false;
    }

    private void fullscreenPlayFromUrl(final String message, final CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            Log.d("CordovaVeeplay", "Starting up");
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
            eventsCallbackContext = callbackContext;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void fullscreenPlayFromObject(final String message, final CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            Log.d("CordovaVeeplay", "Starting up");
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

    private void play(final String message, final int top, final int left, final int width, final int height, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params.leftMargin = VPUtilities.pixelsToDip(left, cordova.getActivity());
                params.topMargin = VPUtilities.pixelsToDip(top, cordova.getActivity());
                topOffset = VPUtilities.pixelsToDip(top, cordova.getActivity());
                params.width = VPUtilities.pixelsToDip(width, cordova.getActivity());
                params.height = VPUtilities.pixelsToDip(height, cordova.getActivity());
                playerContainer.setLayoutParams(params);
                cordovaParent.addView(playerContainer);

                eventsCallbackContext = callbackContext;

                APSMediaPlayer.getInstance().finish();
                APSMediaPlayer.getInstance().init(cordova.getActivity(), true);
                APSMediaPlayer.getInstance().removeAllTrackingEventListeners();
                APSMediaPlayer.getInstance().addTrackingEventListener(VeeplayCordovaPlugin.this);

                //obtain a reference to the main player view
                ViewGroup veeplayViews = APSMediaPlayer.getInstance().viewController();

                //check if the player was previously attached somewhere else
                if(veeplayViews.getParent() != null) {
                    ((ViewGroup)veeplayViews.getParent()).removeView(veeplayViews);
                }
                playerContainer.addView(veeplayViews);
                APSMediaPlayer.getInstance().showHud();

                cordova.getThreadPool().submit(new Runnable() {
                    @Override
                    public void run() {
                        APSMediaBuilder builder = new APSMediaBuilder();
                        try {
                            builder.configureFromURL(new URL(message));
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
                mPlayerContainer.setBackgroundColor(Color.parseColor("#cc2288"));
                fullscreenPlayerDialog.show();

                APSMediaPlayer.getInstance().finish();
                APSMediaPlayer.getInstance().init(cordova.getActivity(), true);
                APSMediaPlayer.getInstance().removeAllTrackingEventListeners();
                APSMediaPlayer.getInstance().addTrackingEventListener(VeeplayCordovaPlugin.this);

                //obtain a reference to the main player view
                ViewGroup veeplayViews = APSMediaPlayer.getInstance().viewController();

                //check if the player was previously attached somewhere else
                if(veeplayViews.getParent() != null) {
                    ((ViewGroup)veeplayViews.getParent()).removeView(veeplayViews);
                }
                mPlayerContainer.addView(veeplayViews);
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
            cordovaParent.removeView(playerContainer);
            if(internalBridgeContext != null) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "stopBoundingTimer");
                pluginResult.setKeepCallback(true);
                eventsCallbackContext.sendPluginResult(pluginResult);

            }
        }
        if(eventsCallbackContext != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, mediaEventType.toString());
            pluginResult.setKeepCallback(true);
            eventsCallbackContext.sendPluginResult(pluginResult);
        }
//        eventsCallbackContext.success(mediaEventType.toString());
    }
}
