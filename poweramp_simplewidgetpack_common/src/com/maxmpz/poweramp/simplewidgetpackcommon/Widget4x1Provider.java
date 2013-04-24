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

import com.maxmpz.powerampapi.simplewidgetpack.R;

import android.view.View;
import android.widget.RemoteViews;

// NOTE: keep in mind that this provider gets instantiated and destroyed for just any update or other calls.
public class Widget4x1Provider extends Widget4x2Provider {
	public Widget4x1Provider() {
		super();
		mWidgetLayout = R.layout.appwidget_4x1;
		mGlueAlbum = true;
	}
	
	protected int setShadow(RemoteViews views, int aaColor, int shadow) {
		int flipperFrameId;
		switch(shadow) {
			case SHADOW_ALBUM_ART:
				views.setViewVisibility(R.id.shadow, View.INVISIBLE);
				flipperFrameId = R.id.flipper_frame_alt;
				break;
			case SHADOW_BOTH_DOWN:
				flipperFrameId = R.id.flipper_frame;
				views.setViewVisibility(R.id.flipper_frame_alt, View.GONE);
				break;
			case SHADOW_BOTH_UP:
			default:
				flipperFrameId = R.id.flipper_frame_alt;
				break;
		}
		return flipperFrameId;
	}
}
