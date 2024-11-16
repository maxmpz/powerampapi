/*
Copyright (C) 2011-2021 Maksim Petrov

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
import android.os.Bundle;
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
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPIHelper;

public class EqActivity extends AppCompatActivity
	implements OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener, OnItemSelectedListener {
	private static final String TAG = "EqActivity";
	private static final boolean LOG = false;

	private static final Pattern sSemicolonSplitRe = Pattern.compile(";");
	private static final Pattern sEqualSplitRe = Pattern.compile("=");

	Intent mEquIntent;
	private boolean mEquBuilt;

	private boolean mSettingEqu;
	private boolean mSettingTone;
	private boolean mSettingPreset;

	@SuppressWarnings({ "resource", "deprecation" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Ask for PA equ state update immediately by sending "empty" SET_EQU_ENABLED command.
		// The reply may be delayed up to 250ms, so UI should accommodate for that (update asynchronously)
		requestEqStatus();

		setContentView(R.layout.activity_eq);

		((CheckBox)findViewById(R.id.dynamic)).setOnCheckedChangeListener(this);
		findViewById(R.id.commit_eq).setOnClickListener(this);

		// Create and bind spinner which binds to available Poweramp presets.
		Spinner presetSpinner = (Spinner)findViewById(R.id.preset_spinner);
		String[] cols = new String[] { "_id", "name" };
		Cursor c = getContentResolver().query(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("eq_presets").build(),
				cols, null, null, "name");
		if(c != null) startManagingCursor(c);
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

	private void requestEqStatus() {
		PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
						.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SET_EQU_ENABLED));
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

		// Ask PA for eq state as, while on background, we probably were denied of intent processing due to
		// Android 8+ background limitations
		requestEqStatus();
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
	 *
	 * NOTE: For Poweramp v3 this intent is not sticky anymore
	 */
	private void registerAndLoadStatus() {
		mEquIntent = ContextCompat.registerReceiver(
			this,
			mEquReceiver,
			new IntentFilter(PowerampAPI.ACTION_EQU_CHANGED),
			ContextCompat.RECEIVER_EXPORTED
		);
		if(LOG) Log.w(TAG, "registerAndLoadStatus mEquIntent=>" + mEquIntent);
	}

	private void unregister() {
		if(mEquReceiver != null) {
			try {
				unregisterReceiver(mEquReceiver);
			} catch(Exception ignored) {
			}
		}
	}

	private BroadcastReceiver mEquReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mEquIntent = intent;

			if(LOG) debugDumpEquIntent(intent);

			updateEqu();
		}

	};

	void updateEqu() {
		if(mEquIntent == null) {
			if(LOG) Log.e(TAG, "updateEqu IGNORE !mEquIntent");
			return;
		}
		if(LOG) Log.w(TAG, "updateEqu", new Exception());

		final CheckBox eq = (CheckBox)findViewById(R.id.eq);
		boolean equEnabled = mEquIntent.getBooleanExtra(PowerampAPI.EXTRA_EQU, false);
		if(eq.isChecked() != equEnabled) {
			mSettingEqu = true;
			eq.setChecked(equEnabled);
		}

		final CheckBox tone = (CheckBox)findViewById(R.id.tone);
		boolean toneEnabled = mEquIntent.getBooleanExtra(PowerampAPI.EXTRA_TONE, false);
		if(tone.isChecked() != toneEnabled) {
			mSettingTone = true;
			tone.setChecked(toneEnabled);
		}

		String presetString = mEquIntent.getStringExtra(PowerampAPI.EXTRA_VALUE);
		if(presetString == null || presetString.length() == 0) {
			if(LOG) Log.w(TAG, "updateEqu !presetString");
			return;
		}

		if(!mEquBuilt) {
			buildEquUI(presetString);
			mEquBuilt = true;
		} else {
			updateEquUI(presetString);
		}

		long id = mEquIntent.getLongExtra(PowerampAPI.EXTRA_ID, PowerampAPI.NO_ID);
		if(LOG) Log.w(TAG, "updateEqu id=" + id);

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

	/**
	 * This method parses the equalizer serialized "presetString" and creates appropriate seekbars.
 	 */
	private void buildEquUI(String string) {
		if(LOG) Log.w(TAG, "buildEquUI string=" + string);
		String[] pairs = sSemicolonSplitRe.split(string);
		TableLayout equLayout = (TableLayout)findViewById(R.id.equ_layout);

		for(String pair : pairs) {
			String[] nameValue = sEqualSplitRe.split(pair, 2);
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
	 * Preamp, bass/treble and equ bands have different scaling. This method ensures correct scaling is applied.
 	 */
	void setBandValue(String name, float value, SeekBar bar) {
		if(LOG) Log.w(TAG, "setBandValue name=" + name + " value=" + value);
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
		if(LOG) Log.w(TAG, "updateEquUI string=" + string);
		String[] pairs = sSemicolonSplitRe.split(string);
		TableLayout equLayout = (TableLayout)findViewById(R.id.equ_layout);

		for(int i = 0, pairsLength = pairs.length; i < pairsLength; i++) {
			String[] nameValue = sEqualSplitRe.split(pairs[i], 2);
			if(nameValue.length == 2) {
				String name = nameValue[0];
				try {
					float value = Float.parseFloat(nameValue[1]);

					SeekBar bar = (SeekBar)((ViewGroup)equLayout.getChildAt(i)).getChildAt(1);
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
			String presetName = intent.getStringExtra(PowerampAPI.EXTRA_NAME);
			String presetString = intent.getStringExtra(PowerampAPI.EXTRA_VALUE);
			long id = mEquIntent.getLongExtra(PowerampAPI.EXTRA_ID, PowerampAPI.NO_ID);
			Log.w(TAG, "debugDumpEquIntent presetName=" + presetName + " presetString=" + presetString + " id=" + id);
		} else {
			Log.e(TAG, "debugDumpEquIntent: intent is null");
		}
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.commit_eq) {
			commitEq();
		}
	}

	/**
	 * Event handler for the checkboxes
	 */
	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		Log.w(TAG, "onCheckedChanged=" + view);
		int id = view.getId();
		if(id == R.id.dynamic) {
			findViewById(R.id.commit_eq).setEnabled(!isChecked);
		} else if(id == R.id.eq) {
			if(!mSettingEqu) {
				PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
								.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SET_EQU_ENABLED)
								.putExtra(PowerampAPI.EXTRA_EQU, isChecked),
						MainActivity.FORCE_API_ACTIVITY
				);
			}
			mSettingEqu = false;
		} else if(id == R.id.tone) {
			if(!mSettingTone) {
				PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
								.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SET_EQU_ENABLED)
								.putExtra(PowerampAPI.EXTRA_TONE, isChecked),
						MainActivity.FORCE_API_ACTIVITY
				);
			}
			mSettingTone = false;
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

		PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
				.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SET_EQU_STRING)
				.putExtra(PowerampAPI.EXTRA_VALUE, presetString.toString()),
				MainActivity.FORCE_API_ACTIVITY
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
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
					.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SET_EQU_BAND)
					.putExtra(PowerampAPI.EXTRA_NAME, name)
					.putExtra(PowerampAPI.EXTRA_VALUE, value),
					MainActivity.FORCE_API_ACTIVITY
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
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
					.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SET_EQU_PRESET)
					.putExtra(PowerampAPI.EXTRA_ID, id),
					MainActivity.FORCE_API_ACTIVITY
			);
		} else {
			mSettingPreset = false;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
}