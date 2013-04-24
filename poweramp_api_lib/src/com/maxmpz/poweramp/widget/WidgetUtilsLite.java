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

package com.maxmpz.poweramp.widget;

import android.database.CharArrayBuffer;


public class WidgetUtilsLite {
	private static StringBuilder sFormatBuilder2 = new StringBuilder();
	
	public static void formatTimeBuffer(CharArrayBuffer buffer, int secs, boolean showPlaceholderForZero) {
		if(secs == 0 && showPlaceholderForZero) {
			buffer.data = new char[]{ '-', ':', '-', '-' };
			buffer.sizeCopied = buffer.data.length;
			return;
		}
		
		sFormatBuilder2.setLength(0);
		int seconds = secs % 60;
		
		
		if(secs < 3600) { // min:sec
			int minutes = secs / 60;
			sFormatBuilder2.append(minutes).append(':');

		} else { // hour:min:sec
			int hours = secs / 3600;
			int minutes = (secs / 60) % 60;
			
			sFormatBuilder2.append(hours).append(':');
			if(minutes < 10) {
				sFormatBuilder2.append('0');
			}
			sFormatBuilder2.append(minutes).append(':');
		}
		if(seconds < 10) {
			sFormatBuilder2.append('0');
		}
		sFormatBuilder2.append(seconds);

		int len = buffer.sizeCopied = buffer.data.length > sFormatBuilder2.length() ? sFormatBuilder2.length() : buffer.data.length;
		sFormatBuilder2.getChars(0, len, buffer.data, 0);
		
	}
}
