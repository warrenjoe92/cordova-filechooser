package com.megster.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.content.Context;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final int PICK_FILE_REQUEST = 1;
    CallbackContext callback;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            chooseFile(callbackContext);
            return true;
        }

        return false;
    }

    public void chooseFile(CallbackContext callbackContext) {

        Log.v(TAG, "choosing file");

        // type and title should be configurable

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooser = Intent.createChooser(intent, "Select File");
        cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

        // Log.v(TAG, "activity started");

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);

        // Log.v(TAG, "sending plugin result");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_FILE_REQUEST && callback != null) {

            if (resultCode == Activity.RESULT_OK) {

                Uri uri = data.getData();

                if (uri != null) {

                  try{

                    String mimeType = getMimeType(uri);
                    JSONObject json = new JSONObject();
                    json.put("uri", uri.toString());
                    json.put("mimeType", mimeType);

                    Log.v(TAG, "got all info: " + " mime " + mimeType);
                    Log.v(TAG, "executing callback");

                    callback.success(json);
                  }catch(JSONException e){
                    callback.error("JSON error");
                  }

                } else {
                    callback.error("File uri was null");
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

              Log.v(TAG, "activity result cancelled");

                // TODO NO_RESULT or error callback?
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                callback.sendPluginResult(pluginResult);

            } else {

              Log.v(TAG, "activity result error");

                callback.error(resultCode);
            }
        }
    }

    private String getMimeType(Uri uri){

      Log.v(TAG, "get mime type");

      Context c = this.cordova.getActivity().getApplicationContext();

      return c.getContentResolver().getType(uri);

    }

}
