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

package com.maxmpz.poweramp.simplewidgetpackcommon;

import android.content.res.Resources;

import com.maxmpz.powerampapi.simplewidgetpack.R;

public class Widget2x2Configure extends Widget4x2Configure {
	private static final String TAG = "Widget2x2Configure";
	
	public Widget2x2Configure() {
		super();
		mWidgetProvider = new Widget2x2Provider();
	}
	
	@Override
	protected void setWidgetFrameSize() {
		final Resources r = getResources();
		mWidgetFrameHeight = r.getDimensionPixelSize(R.dimen.appwidget_small_configure_height);
		mWidgetFrameWidth = r.getDimensionPixelSize(R.dimen.appwidget_2x2_configure_width);
	}
}
