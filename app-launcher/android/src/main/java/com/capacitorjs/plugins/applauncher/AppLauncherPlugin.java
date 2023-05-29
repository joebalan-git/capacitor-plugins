package com.capacitorjs.plugins.applauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AppLauncher")
public class AppLauncherPlugin extends Plugin {

    @PluginMethod
    public void canOpenUrl(PluginCall call) {
        String url = call.getString("url");
        if (url == null) {
            call.reject("Must supply a url");
            return;
        }

        Context ctx = this.getActivity().getApplicationContext();
        final PackageManager pm = ctx.getPackageManager();

        JSObject ret = new JSObject();
        try {
            pm.getPackageInfo(url, PackageManager.GET_ACTIVITIES);
            ret.put("value", true);
            call.resolve(ret);
            return;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.error(getLogTag(), "Package name '" + url + "' not found!", null);
        }

        ret.put("value", false);
        call.resolve(ret);
    }

    @PluginMethod
    public void openUrl(PluginCall call) {
        String url = call.getString("url");
        if (url == null) {
            call.reject("Must provide a url to open");
            return;
        }

        JSObject ret = new JSObject();
        final PackageManager manager = getContext().getPackageManager();
        Intent launchIntent = new Intent(Intent.ACTION_VIEW);
        launchIntent.setData(Uri.parse(url));

        try {
            startActivityForResult(call, launchIntent, "activityResult");
        } catch (Exception ex) {
            launchIntent = manager.getLaunchIntentForPackage(url);
            try {
                startActivityForResult(call, launchIntent, "activityResult");
            } catch (Exception expgk) {
                ret.put("completed", false);
                call.resolve(ret);
            }
        }
    }

    @ActivityCallback
    private void activityResult(PluginCall call, ActivityResult result) {
        JSObject ret = new JSObject();
        ret.put("completed", result.getResultCode() == Activity.RESULT_OK);
        call.resolve(ret);
    }
}
