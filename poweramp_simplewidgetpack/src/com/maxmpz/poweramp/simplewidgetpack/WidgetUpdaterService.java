/*
Copyright (C) 2011-2013 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for widgets, plugins, applications and other software
which communicate with PowerAMP application on Android platform.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.maxmpz.poweramp.simplewidgetpack;

import com.maxmpz.poweramp.widgetpackcommon.BaseWidgetUpdaterService;
import com.maxmpz.poweramp.simplewidgetpackcommon.Widget2x2Provider;
import com.maxmpz.poweramp.simplewidgetpackcommon.Widget4x1Provider;
import com.maxmpz.poweramp.simplewidgetpackcommon.Widget4x2Provider;
import com.maxmpz.poweramp.simplewidgetpackcommon.Widget4x4Provider;
import com.maxmpz.poweramp.simplewidgetpackcommon.WidgetKeyguardProvider;

import android.os.Build;

/**
 * Simple Serivce which updates widget(s). This service actually processes PowerampAPI intents, but throttles them, so widget updates are
 * as rare as possible. 
 */
public class WidgetUpdaterService extends BaseWidgetUpdaterService {
	private static final String TAG = "WidgetUpdaterService";
	private static final boolean LOG = false;
	
	public WidgetUpdaterService() {
		// NOTE: remove/add Widget providers for widgets supported by your widgetpack.
		
		mProviders.add(new Widget4x1Provider());
		mProviders.add(new Widget4x4Provider());
		mProviders.add(new Widget4x2Provider());
		mProviders.add(new Widget2x2Provider());
		if(Build.VERSION.SDK_INT >= 17) {
			mProviders.add(new WidgetKeyguardProvider());
		}
	}
}
