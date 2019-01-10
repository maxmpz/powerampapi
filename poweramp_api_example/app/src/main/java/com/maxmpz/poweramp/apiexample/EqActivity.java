/*
Copyright (C) 2011-2018 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for widgets, plugins, applications and other software
which communicate with Poweramp application on Android platform.

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

package com.maxmpz.poweramp.apiexample;

import java.util.regex.Pattern;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPIHelper;
import com.maxmpz.poweramp.player.TableDefs;

public class EqActivity extends Activity implements OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener, OnItemSelectedListener {
	private static final String TAG = "EqActivity";

	Intent mEquIntent;
	private boolean mEquBuilt;

	private boolean mSettingEqu;
	private boolean mSettingTone;
	private boolean mSettingPreset;

	@SuppressWarnings("resource")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.eq);

		((CheckBox)findViewById(R.id.dynamic)).setOnCheckedChangeListener(this);
		findViewById(R.id.commit_eq).setOnClickListener(this);

		// Create and bind spinner which binds to available Poweramp presets.
		Spinner presetSpinner = (Spinner)findViewById(R.id.preset_spinner);
		String[] cols = new String[] { "_id", "name" };
		Cursor c = getContentResolver().query(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("eq_presets").build(),
				cols, null, null, "name");
		startManagingCursor(c);
		// Add first empty item to the merged cursor via matrix cursor with single row.
		MatrixCursor mc = new MatrixCursor(cols);
		mc.addRow(new Object[]{ PowerampAPI.NO_ID, "" });
		MergeCursor mrgc = new MergeCursor(new Cursor[]{ mc, c });

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_dropdown_item,
				mrgc,
				new String[] { "name" },
				new int[] { android.R.id.text1 },
				0);

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				((TextView)view).setText(cursor.getString(1));
				return true;
			}
		});

		presetSpinner.setAdapter(adapter);
		presetSpinner.setOnItemSelectedListener(this);

		((CheckBox)findViewById(R.id.eq)).setOnCheckedChangeListener(this);
		((CheckBox)findViewById(R.id.tone)).setOnCheckedChangeListener(this);
	}

	/**
	 * NOTE: when screen is rotated, by default android will reapply all saved values to the controls, calling the event handlers, which generate appropriate intents, thus,
	 * on screen rotation some commands could be sent to Poweramp unintentionally.
	 * As this activity always syncs everything with the actual state of Poweramp, this automatic restoring of state is just non needed.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	}


	/**
	 * NOTE: this method unregister all broadcast receivers on activity pause. This is the correct way of handling things - we're
	 * sure no unnecessary event processing will be done for paused activity, when screen is OFF, etc.
 	 */
	@Override
	protected void onPause() {
		unregister();

		super.onPause();
	}

	/**
	 * Register broadcast receivers.
 	 */
	@Override
	protected void onResume() {
		super.onResume();

		registerAndLoadStatus();
	}


	@Override
	protected void onDestroy() {
		unregister();

		mEquReceiver = null;

		super.onDestroy();
	}

	/**
	 * NOTE, it's not necessary to set mStatusIntent/mPlayingModeIntent/mEquIntent this way here,
	 * but this approach can be used with null receiver to get current sticky intent without broadcast receiver
	 */
	private void registerAndLoadStatus() {
		mEquIntent = registerReceiver(mEquReceiver, new IntentFilter(PowerampAPI.ACTION_EQU_CHANGED));
	}

	private void unregister() {
		if(mEquReceiver != null) {
			try {
				unregisterReceiver(mEquReceiver);
			} catch(Exception ex){}
		}
	}

	private BroadcastReceiver mEquReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mEquIntent = intent;

			debugDumpEquIntent(intent);

			updateEqu();
		}

	};

	void updateEqu() {
		if(mEquIntent == null) {
			return;
		}


		final CheckBox eq = (CheckBox)findViewById(R.id.eq);
		boolean equEnabled = mEquIntent.getBooleanExtra(PowerampAPI.EQU, false);
		if(eq.isChecked() != equEnabled) {
			mSettingEqu = true;
			eq.setChecked(equEnabled);
		}

		final CheckBox tone = (CheckBox)findViewById(R.id.tone);
		boolean toneEnabled = mEquIntent.getBooleanExtra(PowerampAPI.TONE, false);
		if(tone.isChecked() != toneEnabled) {
			mSettingTone = true;
			tone.setChecked(toneEnabled);
		}

		String presetString = mEquIntent.getStringExtra(PowerampAPI.VALUE);
		if(presetString == null || presetString.length() == 0) {
			return;
		}

		if(!mEquBuilt) {
			buildEquUI(presetString);
			mEquBuilt = true;
		} else {
			updateEquUI(presetString);
		}

		//String presetName = mEquIntent.getStringExtra(PowerampAPI.NAME);

		long id = mEquIntent.getLongExtra(PowerampAPI.ID, PowerampAPI.NO_ID);
		Log.w(TAG, "updateEqu id=" + id);

		Spinner presetSpinner = (Spinner)findViewById(R.id.preset_spinner);
		int count = presetSpinner.getAdapter().getCount();
		for(int i = 0; i < count; i++) {
			if(presetSpinner.getAdapter().getItemId(i) == id) {
				if(presetSpinner.getSelectedItemPosition() != i) {
					mSettingPreset = true;
					presetSpinner.setSelection(i);
				}
				break;
			}
		}
	}

	private static Pattern sSemicolonSplitRe = Pattern.compile(";");
	private static Pattern sEqualSplitRe = Pattern.compile("=");

	/**
	 * This method parses the equalizer serialized "presetString" and creates appropriate seekbars.
 	 */
	private void buildEquUI(String string) {
		String[] pairs = sSemicolonSplitRe.split(string);
		TableLayout equLayout = (TableLayout)findViewById(R.id.equ_layout);

		for(int i = 0, pairsLength = pairs.length; i < pairsLength; i++) {
			String[] nameValue = sEqualSplitRe.split(pairs[i], 2);
			if(nameValue.length == 2) {
				String name = nameValue[0];

				try {
					float value = Float.parseFloat(nameValue[1]);

					TableRow row = new TableRow(this);

					TextView label = new TextView(this);
					label.setText(name);
					TableRow.LayoutParams lp = new TableRow.LayoutParams();
					lp.height = lp.width = TableRow.LayoutParams.WRAP_CONTENT;
					row.addView(label, lp);

					SeekBar bar = new SeekBar(this);
					bar.setOnSeekBarChangeListener(this);
					bar.setTag(name);
					setBandValue(name, value, bar);
					row.addView(bar, lp);

					equLayout.addView(row);

				} catch(NumberFormatException ex) {
					ex.printStackTrace();
					Log.e(TAG, "failed to parse eq value=" + nameValue[1]);
				}
			}
		}
	}

	/**
	 * Preamp, bass/treble and equ bands have different scalling. This method ensures correct scalling is applied.
 	 */
	void setBandValue(String name, float value, SeekBar bar) {
		//Log.w(TAG, "name=" + name + " value=" + value);
		if("preamp".equals(name)) {
			bar.setMax(200);
			bar.setProgress((int)(value * 100f));
		} else if("bass".equals(name) || "treble".equals(name)) {
			bar.setMax(100);
			bar.setProgress((int)(value * 100f));
		} else {
			bar.setMax(200);
			bar.setProgress((int)(value * 100f + 100f));
		}
	}

	/**
	 * Almost the same as buildEquUI, just do the UI update without building it
	 */
	private void updateEquUI(String string) {
		Log.w(TAG, "updateEquUI!");
		String[] pairs = sSemicolonSplitRe.split(string);
		TableLayout equLayout = (TableLayout)findViewById(R.id.equ_layout);

		for(int i = 0, pairsLength = pairs.length; i < pairsLength; i++) {
			String[] nameValue = sEqualSplitRe.split(pairs[i], 2);
			if(nameValue.length == 2) {
				String name = nameValue[0];
				try {
					float value = Float.parseFloat(nameValue[1]);

					SeekBar bar = (SeekBar)((ViewGroup)equLayout.getChildAt(i)).getChildAt(1);
					//SeekBar bar = (SeekBar)equLayout.findViewWithTag(name);
					if(bar == null) {
						Log.w(TAG, "no bar=" + name);
						continue;
					}
					setBandValue(name, value, bar);
				} catch(NumberFormatException ex) {
					ex.printStackTrace();
					Log.e(TAG, "failed to parse eq value=" + nameValue[1]);
				}
			}
		}
	}

	void debugDumpEquIntent(Intent intent) {
		if(intent != null) {
			String presetName = intent.getStringExtra(PowerampAPI.NAME);
			String presetString = intent.getStringExtra(PowerampAPI.VALUE);
			long id = mEquIntent.getLongExtra(PowerampAPI.ID, PowerampAPI.NO_ID);
			Log.w(TAG, "debugDumpEquIntent presetName=" + presetName + " presetString=" + presetString + " id=" + id);
		} else {
			Log.e(TAG, "debugDumpEquIntent: intent is null");
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.commit_eq:
				commitEq();
				break;
		}
	}

	/**
	 * Event handler for Dynamic Eq checkbox
	 */
	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		Log.w(TAG, "onCheckedChanged=" + view);
		switch(view.getId()) {
			case R.id.dynamic:
				findViewById(R.id.commit_eq).setEnabled(!isChecked);
				break;

			case R.id.eq:
				if(!mSettingEqu) {
					PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
							.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SET_EQU_ENABLED)
							.putExtra(PowerampAPI.EQU, isChecked)
					);
				}
				mSettingEqu = false;
				break;

			case R.id.tone:
				if(!mSettingTone) {
					PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
							.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SET_EQU_ENABLED)
							.putExtra(PowerampAPI.TONE, isChecked)
					);
				}
				mSettingTone = false;
				break;
		}
	}

	/**
	 * Generates and sends presetString to Poweramp
 	 */
	private void commitEq() {
		StringBuilder presetString = new StringBuilder();

		TableLayout equLayout = (TableLayout)findViewById(R.id.equ_layout);
		int count = equLayout.getChildCount();
		for(int i = count - 1; i >= 0; i--) {
			SeekBar bar = (SeekBar)((ViewGroup)equLayout.getChildAt(i)).getChildAt(1);
			String name = (String)bar.getTag();
			float value = seekBarToValue(name, bar.getProgress());
			presetString.append(name).append("=").append(value).append(";");
		}

		PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
				.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SET_EQU_STRING)
				.putExtra(PowerampAPI.VALUE, presetString.toString())
		);
	}

	/**
	 * Applies correct seekBar-to-float scaling
 	 */
	private float seekBarToValue(String name, int progress) {
		float value;
		if("preamp".equals(name) || "bass".equals(name) || "treble".equals(name)) {
			value = (float)progress / 100.f;
		} else {
			value = (float)(progress - 100) / 100.f;
		}
		return value;
	}

	/**
	 * Process Eq band change.
	 */
	@Override
	public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {

		if(((CheckBox)findViewById(R.id.dynamic)).isChecked()) {
			String name = (String)bar.getTag();
			float value = seekBarToValue(name, bar.getProgress());
			PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
					.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SET_EQU_BAND)
					.putExtra(PowerampAPI.NAME, name)
					.putExtra(PowerampAPI.VALUE, value)
			);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	/**
	 * Event handler for Presets spinner
 	 */
	@Override
	public void onItemSelected(AdapterView<?> adapter, View item, int pos, long id) {
		if(!mSettingPreset) {
			PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
					.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SET_EQU_PRESET)
					.putExtra(PowerampAPI.ID, id)
			);
		} else {
			mSettingPreset = false;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
}