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

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import static com.maxmpz.poweramp.simplewidgetpackcommon.Widget4x4Provider.*;

public class WidgetKeyguardConfigure extends Widget4x4Configure {
	private static final String TAG = "WidgetKeyguardConfigure";

	public WidgetKeyguardConfigure() {
		super();
		mWidgetProvider = new WidgetKeyguardProvider();
		mBgColor = 0x00FFFFFF;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	protected void setDeckBG(int color, ImageView deck) {
		int id;
		switch(mShadow) {
			case STYLE_3:
				id = R.id.deck_bg3;
				break;
			case STYLE_2:
				id = R.id.deck_bg2;
				break;
			case STYLE_1:
			default:
				id = R.id.deck_bg;
				break;
		}
		deck = (ImageView)mWidgetFrame.findViewById(id);
		
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		deck.setColorFilter(Color.rgb(r, g, b));
		int alpha = Color.alpha(color);
		deck.setAlpha(alpha);
		deck.setVisibility(View.VISIBLE);
	}
}
