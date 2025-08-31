/*
 *   MJ PDF
 *   Copyright (C) 2023 Mudlej
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If
/*
 * MIT License
 *
 * Copyright (c) 2018 Gokul Swaminathan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ishacker.mjpdf.system.util;

import static com.ishacker.mjpdf.system.util.UtilKt.readBytesToEnd;
import static java.net.HttpURLConnection.HTTP_OK;

import android.os.AsyncTask;
import android.util.Log;

import com.ishacker.mjpdf.system.R;
import com.ishacker.mjpdf.system.databinding.ActivityMainBinding;
import com.ishacker.mjpdf.system.ui.main.MainActivity;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLException;

/**
 * This class is used to get a PDF File from an URL
 */
public class DownloadPDFFile extends AsyncTask<String, Void, Object> {

    private final WeakReference<MainActivity> mainActivityWR;
    private final WeakReference<ActivityMainBinding> bindingWR;

    public DownloadPDFFile(MainActivity activity, ActivityMainBinding binding) {
        mainActivityWR = new WeakReference<>(activity);
        bindingWR = new WeakReference<>(binding);
    }

    @Override
    protected Object doInBackground(String... strings) {
        String url = strings[0];
        HttpURLConnection httpConnection = null;

        try {
            httpConnection = (HttpURLConnection) new URL(url).openConnection();
            httpConnection.connect();
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HTTP_OK) {
                return readBytesToEnd(httpConnection.getInputStream());
            } else {
                Log.e("DownloadPDFFile", "Error during http request, response code : " + responseCode);
                return responseCode;
            }
        } catch (IOException e) {
            Log.e("DownloadPDFFile", "Error cannot get file at URL : " + url, e);
            return e;
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        MainActivity activity = mainActivityWR.get();
        ActivityMainBinding binding = bindingWR.get();

        if (activity != null) {
            activity.hideProgressBar();

            if (result == null) {
                //Toast.makeText(activity, R.string.toast_generic_download_error, Toast.LENGTH_LONG).show();
                Snackbar.make(binding.getRoot(), R.string.toast_generic_download_error, Snackbar.LENGTH_LONG).show();
            } else if (result instanceof Integer) {
                //Toast.makeText(activity, R.string.toast_http_code_error, Toast.LENGTH_LONG).show();
                Snackbar.make(binding.getRoot(), R.string.toast_http_code_error, Snackbar.LENGTH_LONG).show();
            } else if (result instanceof SSLException) {
                //Toast.makeText(activity, R.string.toast_ssl_error, Toast.LENGTH_LONG).show();
                Snackbar.make(binding.getRoot(), R.string.toast_ssl_error, Snackbar.LENGTH_LONG).show();
            } else if (result instanceof IOException) {
                //Toast.makeText(activity, R.string.toast_generic_download_error, Toast.LENGTH_LONG).show();
                Snackbar.make(binding.getRoot(), R.string.toast_generic_download_error, Snackbar.LENGTH_LONG).show();
            } else if (result instanceof byte[]) {
                activity.saveToFileAndDisplay((byte[]) result);
            }
        }
    }
}
