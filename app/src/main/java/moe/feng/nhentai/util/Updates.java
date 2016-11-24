package moe.feng.nhentai.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;

import moe.feng.nhentai.R;

public class Updates {
    private static final String LATEST_RELEASE = "https://api.github.com/repos/FengMoeTeam/NHentai-android/releases/latest";
    private static final String LATEST_GRADLE_FILE = "https://raw.githubusercontent.com/FengMoeTeam/NHentai-android/master/app/build.gradle";
    private static final String KEY_IGNORE_UPDATE = "ignoreUpdate";
    private static int prog = 0;

    public static void check(final Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String version = HttpTools.getStringFromUrl(LATEST_GRADLE_FILE).trim().replace(" ", "");

                    Log.d("Update", "doInBackground: " + getAPKversionCode(activity));
                    final int code = Integer.parseInt(version.substring(version.indexOf("versionCode") + 11, version.indexOf("versionName")).trim());
                    if (code > getAPKversionCode(activity) && !isIgnored(activity, code)) {// >= Test
                        final JSONObject object = new JSONObject(HttpTools.getStringFromUrl(LATEST_RELEASE));
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new MaterialDialog.Builder(activity)
                                            .title(activity.getResources().getString(R.string.new_version_available) + " " + object.getString("tag_name"))
                                            .content(object.getString("body"))
                                            .positiveText(R.string.button_update)
                                            .negativeText(R.string.close)
                                            .neutralText(R.string.ignore)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    new AsyncTask<Void, Void, Void>() {
                                                        @Override
                                                        protected Void doInBackground(Void... params) {
                                                            Looper.prepare();
                                                            Settings.getInstance(activity).putInt(KEY_IGNORE_UPDATE, -1);
                                                            final File downloadedUpdate = new File(activity.getExternalCacheDir() + "/updates/", "app-update.apk");
                                                            if (!downloadedUpdate.getParentFile().exists())
                                                                downloadedUpdate.getParentFile().mkdirs();
                                                            if (downloadedUpdate.exists())
                                                                downloadedUpdate.delete();
                                                            try {
                                                                HttpURLConnection connection = HttpTools.openConnection(object.getJSONArray("assets").getJSONObject(0).getString("browser_download_url"));
                                                                connection.connect();
                                                                BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                                                                FileOutputStream outputStream = new FileOutputStream(downloadedUpdate);
                                                                int lenghtOfFile = connection.getContentLength();
                                                                byte data[] = new byte[1024 * 4];
                                                                long total = 0;
                                                                int count;
                                                                final MaterialDialog progress = new MaterialDialog.Builder(activity)
                                                                        .title(R.string.updating)
                                                                        .progress(false, 100, false)
                                                                        .build();

                                                                activity.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        progress.show();
                                                                    }
                                                                });
                                                                while ((count = inputStream.read(data)) != -1) {
                                                                    total += count;
                                                                    final int tprog = (int) ((total * 100) / lenghtOfFile);
                                                                    if (tprog > prog) {
                                                                        activity.runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                progress.setProgress(tprog);
                                                                                progress.show();
                                                                            }
                                                                        });
                                                                        prog = tprog;
                                                                    }
                                                                    outputStream.write(data, 0, count);
                                                                    outputStream.flush();

                                                                }
                                                                outputStream.close();
                                                                connection.disconnect();
                                                                activity.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        progress.dismiss();
                                                                    }
                                                                });

                                                                Intent promptInstall = new Intent(Intent.ACTION_VIEW);
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                                    Uri uri = FileProvider.getUriForFile(activity, "moe.feng.nhentai.ui.HomeActivity", downloadedUpdate);
                                                                    activity.grantUriPermission("com.android.packageinstaller", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                                    promptInstall.setDataAndType(uri,
                                                                            "application/vnd.android.package-archive");
                                                                } else {
                                                                    promptInstall.setDataAndType(Uri.fromFile(downloadedUpdate),
                                                                            "application/vnd.android.package-archive");
                                                                }
                                                                activity.finish();
                                                                activity.startActivity(promptInstall);
                                                            } catch (Exception e) {
                                                                activity.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Toast.makeText(activity, R.string.updated_apk_download_error, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                                e.printStackTrace();
                                                            }
                                                            return null;
                                                        }
                                                    }.execute();
                                                }
                                            })
                                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    Settings.getInstance(activity).putInt(KEY_IGNORE_UPDATE, code);
                                                }
                                            }).build().show();
                                } catch (Exception e) {
                                    Toast.makeText(activity, R.string.updated_apk_download_error, Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        if (code == getAPKversionCode(activity)) {
                            Log.d("Update", "Is Updated");
                        }
                        if (isIgnored(activity, code)) {
                            Log.d("Update", "Ignored: " + code);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public static void check(final Activity activity, final UpdateInterface updateInterface) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String version = HttpTools.getStringFromUrl(LATEST_GRADLE_FILE).trim().replace(" ", "");
                    Log.d("Update", "doInBackground: " + getAPKversionCode(activity));
                    final int code = Integer.parseInt(version.substring(version.indexOf("versionCode") + 11, version.indexOf("versionName")).trim());
                    if (code > getAPKversionCode(activity)) {// >= Test
                        updateInterface.onChecked(false);
                        final JSONObject object = new JSONObject(HttpTools.getStringFromUrl(LATEST_RELEASE));
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new MaterialDialog.Builder(activity)
                                            .title(activity.getResources().getString(R.string.new_version_available) + " " + object.getString("tag_name"))
                                            .content(object.getString("body"))
                                            .positiveText(R.string.button_update)
                                            .negativeText(R.string.close)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    new AsyncTask<Void, Void, Void>() {
                                                        @Override
                                                        protected Void doInBackground(Void... params) {
                                                            Looper.prepare();
                                                            Settings.getInstance(activity).putInt(KEY_IGNORE_UPDATE, -1);
                                                            final File downloadedUpdate = new File(activity.getExternalCacheDir() + "/updates/", "app-update.apk");
                                                            if (!downloadedUpdate.getParentFile().exists())
                                                                downloadedUpdate.getParentFile().mkdirs();
                                                            if (downloadedUpdate.exists())
                                                                downloadedUpdate.delete();
                                                            try {
                                                                HttpURLConnection connection = HttpTools.openConnection(object.getJSONArray("assets").getJSONObject(0).getString("browser_download_url"));
                                                                connection.connect();
                                                                BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                                                                FileOutputStream outputStream = new FileOutputStream(downloadedUpdate);
                                                                int lenghtOfFile = connection.getContentLength();
                                                                byte data[] = new byte[1024 * 4];
                                                                long total = 0;
                                                                int count;
                                                                final MaterialDialog progress = new MaterialDialog.Builder(activity)
                                                                        .title(R.string.updating)
                                                                        .progress(false, 100, false)
                                                                        .build();

                                                                activity.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        progress.show();
                                                                    }
                                                                });
                                                                while ((count = inputStream.read(data)) != -1) {
                                                                    total += count;
                                                                    final int tprog = (int) ((total * 100) / lenghtOfFile);
                                                                    if (tprog > prog) {
                                                                        activity.runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                progress.setProgress(tprog);
                                                                                progress.show();
                                                                            }
                                                                        });
                                                                        prog = tprog;
                                                                    }
                                                                    outputStream.write(data, 0, count);
                                                                    outputStream.flush();

                                                                }
                                                                outputStream.close();
                                                                connection.disconnect();
                                                                activity.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        progress.dismiss();
                                                                    }
                                                                });

                                                                Intent promptInstall = new Intent(Intent.ACTION_VIEW);
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                                    Uri uri = FileProvider.getUriForFile(activity, "moe.feng.nhentai.ui.HomeActivity", downloadedUpdate);
                                                                    activity.grantUriPermission("com.android.packageinstaller", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                                    promptInstall.setDataAndType(uri,
                                                                            "application/vnd.android.package-archive");
                                                                } else {
                                                                    promptInstall.setDataAndType(Uri.fromFile(downloadedUpdate),
                                                                            "application/vnd.android.package-archive");
                                                                }
                                                                activity.finish();
                                                                activity.startActivity(promptInstall);
                                                            } catch (Exception e) {
                                                                activity.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Toast.makeText(activity, R.string.updated_apk_download_error, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                                e.printStackTrace();
                                                            }
                                                            return null;
                                                        }
                                                    }.execute();
                                                }
                                            }).build().show();
                                } catch (Exception e) {
                                    Toast.makeText(activity, R.string.updated_apk_download_error, Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        updateInterface.onChecked(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    private static boolean isIgnored(Context context, int gitVersion) {
        return Settings.getInstance(context).getInt(KEY_IGNORE_UPDATE, -1) == gitVersion;
    }

    private static int getAPKversionCode(Context context) throws PackageManager.NameNotFoundException {
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    }

    public interface UpdateInterface {
        void onChecked(boolean isUpdated);
    }

}