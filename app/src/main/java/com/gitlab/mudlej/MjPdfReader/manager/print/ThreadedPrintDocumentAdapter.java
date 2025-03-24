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
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *  --------------------------
 *  This code was previously licensed under
 *
 *  MIT License
 *
 *  Copyright (c) 2018 Gokul Swaminathan
 *  Copyright (c) 2023 Mudlej
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.gitlab.mudlej.MjPdfReader.manager.print;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class ThreadedPrintDocumentAdapter extends PrintDocumentAdapter {

    private final Context context;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    ThreadedPrintDocumentAdapter(Context context) {
        this.context = context;
    }

    abstract LayoutJob buildLayoutJob(
        PrintAttributes oldAttributes,
        PrintAttributes newAttributes,
        CancellationSignal cancellationSignal,
        LayoutResultCallback callback,
        Bundle extras
    );

    abstract WriteJob buildWriteJob(
        PageRange[] pages,
        ParcelFileDescriptor destination,
        CancellationSignal cancellationSignal,
        WriteResultCallback callback,
        Context context
    );

    @Override
    public void onLayout(
        PrintAttributes oldAttributes,
        PrintAttributes newAttributes,
        CancellationSignal cancellationSignal,
        LayoutResultCallback callback,
        Bundle extras
    ) {
        threadPool.submit(buildLayoutJob(oldAttributes, newAttributes, cancellationSignal, callback, extras));
    }

    @Override
    public void onWrite(
        PageRange[] pages,
        ParcelFileDescriptor destination,
        CancellationSignal cancellationSignal,
        WriteResultCallback callback
    ) {
        threadPool.submit(buildWriteJob(pages, destination, cancellationSignal, callback, context));
    }

    @Override
    public void onFinish() {
        threadPool.shutdown();
        super.onFinish();
    }

    protected abstract static class LayoutJob implements Runnable {
        PrintAttributes oldAttributes;
        PrintAttributes newAttributes;
        CancellationSignal cancellationSignal;
        LayoutResultCallback callback;
        Bundle extras;

        LayoutJob(
            PrintAttributes oldAttributes,
            PrintAttributes newAttributes,
            CancellationSignal cancellationSignal,
            LayoutResultCallback callback,
            Bundle extras
        ) {
            this.oldAttributes = oldAttributes;
            this.newAttributes = newAttributes;
            this.cancellationSignal = cancellationSignal;
            this.callback = callback;
            this.extras = extras;
        }
    }

    protected abstract static class WriteJob implements Runnable {
        PageRange[] pages;
        ParcelFileDescriptor destination;
        CancellationSignal cancellationSignal;
        WriteResultCallback callback;
        Context context;

        WriteJob(
            PageRange[] pages,
            ParcelFileDescriptor destination,
            CancellationSignal cancellationSignal,
            WriteResultCallback callback,
            Context context
        ) {
            this.pages = pages;
            this.destination = destination;
            this.cancellationSignal = cancellationSignal;
            this.callback = callback;
            this.context = context;
        }
    }
}

