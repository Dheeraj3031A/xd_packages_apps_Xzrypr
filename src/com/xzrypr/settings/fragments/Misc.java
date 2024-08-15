/*
 * Copyright (C) 2019-2021 The xzryprOS Project
 * Copyright (C) 2021 xdroid, xyzprjkt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xzrypr.settings.fragments;

import android.os.Bundle;
import android.content.Context;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.nano.MetricsProto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import android.content.Intent;
import android.app.Activity;
import android.os.SystemProperties;

import android.content.Intent;
import android.app.Activity;
import android.os.SystemProperties;

public class Misc extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_PIF_JSON_FILE_PREFERENCE = "pif_json_file_preference";
    private Preference mPifJsonFilePreference;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

        addPreferencesFromResource(R.xml.xzrypr_settings_misc);
        mPifJsonFilePreference = findPreference(KEY_PIF_JSON_FILE_PREFERENCE);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XD_ZONE;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mPifJsonFilePreference) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/json");
            startActivityForResult(intent, 10001);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10001 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Log.d(TAG, "URI received: " + uri.toString());
            try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, length);
                    }
                    String json = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);

                    Log.d(TAG, "JSON data: " + json);
                    JSONObject jsonObject = new JSONObject(json);
                    for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                        String key = it.next();
                        String value = jsonObject.getString(key);
                        Log.d(TAG, "Setting property: persist.sys.pihooks_" + key + " = " + value);
                        SystemProperties.set("persist.sys.pihooks_" + key, value);
                    }
                    Toast.makeText(
                        getContext(),
                        getContext().getResources().getString(R.string.pif_json_select_success),
                        Toast.LENGTH_LONG
                    ).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading JSON or setting properties", e);
            }
        }
    }

}