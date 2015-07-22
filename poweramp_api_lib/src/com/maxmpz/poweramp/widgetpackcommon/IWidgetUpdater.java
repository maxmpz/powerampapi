package com.maxmpz.poweramp.widgetpackcommon;

import android.content.Context;
import android.content.SharedPreferences;

public interface IWidgetUpdater {
	public WidgetUpdateData pushUpdate(Context context, SharedPreferences prefs, int[] ids, boolean mediaRemoved, boolean trackChanged, boolean updateByOs, WidgetUpdateData data);
}
