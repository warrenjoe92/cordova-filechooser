package com.megster.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.provider.OpenableColumns;
import android.database.Cursor;
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

      Log.v(TAG, "on activity result");

        if (requestCode == PICK_FILE_REQUEST && callback != null) {

          Log.v(TAG, "pick file request");

            if (resultCode == Activity.RESULT_OK) {

              Log.v(TAG, "activity result ok");

                Uri uri = data.getData();

                Log.v(TAG, "got data");

                if (uri != null) {

                  try{

                    Log.v(TAG, "uri not null, getting info");

                    String displayName = getFileName(uri);
                    String mimeType = getMimeType(uri);
                    JSONObject json = new JSONObject();
                    json.put("uri", uri.toString());
                    json.put("filename", displayName);
                    json.put("mimeType", mimeType);

                    Log.v(TAG, "got all info: " + displayName + " " + mimeType);
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

    private String getFileName(Uri uri){

      Log.v(TAG, "get file name");

      Context c = this.cordova.getActivity().getApplicationContext();
      Cursor cursor = c.getContentResolver().query(uri, null, null, null, null, null);
      try {
      // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
      // "if there's anything to look at, look at it" conditionals.
          if (cursor != null && cursor.moveToFirst()) {

              // Note it's called "Display Name".  This is
              // provider-specific, and might not necessarily be the file name.
              String displayName = cursor.getString(
                      cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)); //causes NULL_POINTER exception on some devices

              // int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
              // If the size is unknown, the value stored is null.  But since an
              // int can't be null in Java, the behavior is implementation-specific,
              // which is just a fancy term for "unpredictable".  So as
              // a rule, check if it's null before assigning to an int.  This will
              // happen often:  The storage API allows for remote files, whose
              // size might not be locally known.
              // String size = null;
              // if (!cursor.isNull(sizeIndex)) {
              //     // Technically the column stores an int, but cursor.getString()
              //     // will do the conversion automatically.
              //     size = cursor.getString(sizeIndex);
              // } else {
              //     size = "Unknown";
              // }

              return displayName;
          }
      } catch (NullPointerException exc) {
          Log.w(TAG, "null pointer exception");
      }finally {
          try{
              cursor.close();
          }
          catch (NullPointerException exc) {
              Log.w(TAG, "null pointer exception");
          }
      }
      return null;
    }
}
