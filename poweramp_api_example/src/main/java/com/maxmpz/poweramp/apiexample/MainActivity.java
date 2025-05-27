/*
Copyright (C) 2011-2025 Maksim Petrov

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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPI.Commands;
import com.maxmpz.poweramp.player.PowerampAPIHelper;
import com.maxmpz.poweramp.player.RemoteTrackTime;
import com.maxmpz.poweramp.player.RemoteTrackTime.TrackTimeListener;
import com.maxmpz.poweramp.player.TableDefs;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import java.util.Set;
import static android.view.View.VISIBLE;


public class MainActivity extends AppCompatActivity implements
		OnClickListener,
		OnLongClickListener,
		OnTouchListener,
		OnCheckedChangeListener,
		OnSeekBarChangeListener,
		OnItemSelectedListener,
		TrackTimeListener
{
	private static final String TAG = "MainActivity";
	private static final boolean LOG_VERBOSE = false;

	/** If set to true, we send all our intents to API activity. Use for Poweramp build 862+ */
	static final boolean FORCE_API_ACTIVITY = true;

	private static final char[] NO_TIME = new char[]{ '-', ':', '-', '-' };
	private static final int SEEK_THROTTLE = 500;

	protected Intent mTrackIntent;
	private Intent mStatusIntent;
	protected Intent mPlayingModeIntent;

	private Bundle mCurrentTrack;

	private RemoteTrackTime mRemoteTrackTime;
	private SeekBar mSongSeekBar;

	private TextView mDuration;
	private TextView mElapsed;
	private boolean mSettingPreset;

	private long mLastSeekSentTime;

	private final StringBuilder mDurationBuffer = new StringBuilder();
	private final StringBuilder mElapsedBuffer = new StringBuilder();
	private @Nullable Uri mLastCreatedPlaylistFilesUri;
	private static boolean sPermissionAsked;
	/** Use getPowerampBuildNumber to get the build number */
	private int mPowerampBuildNumber;
	private boolean mProcessingLongPress;
	private int mLastSentSeekPosition;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(LOG_VERBOSE) Log.w(TAG, "onCreate");

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		findViewById(R.id.play).setOnClickListener(this);
		findViewById(R.id.play).setOnLongClickListener(this);
		findViewById(R.id.pause).setOnClickListener(this);

		findViewById(R.id.prev).setOnClickListener(this);
		findViewById(R.id.prev).setOnLongClickListener(this);
		findViewById(R.id.prev).setOnTouchListener(this);

		findViewById(R.id.next).setOnClickListener(this);
		findViewById(R.id.next).setOnLongClickListener(this);
		findViewById(R.id.next).setOnTouchListener(this);

		findViewById(R.id.prev_in_cat).setOnClickListener(this);
		findViewById(R.id.next_in_cat).setOnClickListener(this);
		findViewById(R.id.repeat).setOnClickListener(this);
		findViewById(R.id.shuffle).setOnClickListener(this);
		findViewById(R.id.repeat_all).setOnClickListener(this);
		findViewById(R.id.repeat_off).setOnClickListener(this);
		findViewById(R.id.shuffle_all).setOnClickListener(this);
		findViewById(R.id.shuffle_off).setOnClickListener(this);
		findViewById(R.id.eq).setOnClickListener(this);

		mSongSeekBar = findViewById(R.id.song_seekbar);
		mSongSeekBar.setOnSeekBarChangeListener(this);

		mDuration = findViewById(R.id.duration);
		mElapsed = findViewById(R.id.elapsed);

		mRemoteTrackTime = new RemoteTrackTime(this);
		mRemoteTrackTime.setTrackTimeListener(this);

		//((TextView)findViewById(R.id.play_file_path)).setText(findFirstMP3(Environment.getExternalStorageDirectory())); // This can be slow, disabled
		findViewById(R.id.play_file).setOnClickListener(this);

		findViewById(R.id.folders).setOnClickListener(this);

		findViewById(R.id.play_album).setOnClickListener(this);
		findViewById(R.id.play_all_songs).setOnClickListener(this);
		findViewById(R.id.play_second_artist_first_album).setOnClickListener(this);

		findViewById(R.id.pa_current_list).setOnClickListener(this);
		findViewById(R.id.pa_folders).setOnClickListener(this);
		findViewById(R.id.pa_all_songs).setOnClickListener(this);
		((SeekBar)findViewById(R.id.sleep_timer_seekbar)).setOnSeekBarChangeListener(this);

		// Ask Poweramp for a permission to access its data provider. Needed only if we want to make queries against Poweramp database, e.g. in FilesActivity/FoldersActivity
		// NOTE: this will work only if Poweramp process is alive.
		// This actually should be done once per this app installation, but for the simplicity, we use per-process static field here
		if(!sPermissionAsked) {
			if(LOG_VERBOSE) Log.w(TAG, "onCreate skin permission");
			Intent intent = new Intent(PowerampAPI.ACTION_ASK_FOR_DATA_PERMISSION);
			intent.setPackage(PowerampAPIHelper.getPowerampPackageName(this));
			intent.putExtra(PowerampAPI.EXTRA_PACKAGE, getPackageName());
			if(FORCE_API_ACTIVITY) {
				intent.setComponent(PowerampAPIHelper.getApiActivityComponentName(this));
				startActivitySafe(intent);
			} else {
				sendBroadcast(intent);
			}
			sPermissionAsked = true;
		}

		getComponentNames();

		if(PowerampAPIHelper.getPowerampBuild(this) == Integer.MAX_VALUE) {
			var topHint = (TextView)findViewById(R.id.top_hint);
			topHint.setText("-Poweramp build number inaccessible - assuming the latest build-");
			topHint.setVisibility(VISIBLE);
		}

		if(LOG_VERBOSE) Log.w(TAG, "onCreate DONE");
	}

	/**
	 * When screen is rotated, by default Android will reapply all saved values to the controls, calling the event handlers, which generate appropriate intents, thus
	 * on screen rotation some commands could be sent to Poweramp unintentionally.
	 * As this activity always syncs everything with the actual state of Poweramp, the automatic restoring of state is non needed and harmful.
	 * <br><br>
	 * Nevertheless, the actual implementation should probably manipulate per view View.setSaveEnabled() for specific controls, use some Model pattern, or manage
	 * state otherwise, as empty onSaveInstanceState here denies save for everything
	 */
	@Override
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
	}

	/**
	 * @see #onSaveInstanceState
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	}

	/**
	 * This method unregister all broadcast receivers on activity pause. This is the correct way of handling things - we're
	 * sure no unnecessary event processing will be done for paused activity, when screen is OFF, etc.
	 * Alternatively, we may do this in onStop/onStart, esp. for latest Android versions and things like split screen
	 */
	@Override
	protected void onPause() {
		unregister();
		mRemoteTrackTime.unregister();

		super.onPause();
	}

	/**
	 * Register broadcast receivers
 	 */
	@Override
	protected void onResume() {
		super.onResume();

		registerAndLoadStatus();
		mRemoteTrackTime.registerAndLoadStatus();
	}


	@Override
	protected void onDestroy() {
		Log.w(TAG, "onDestroy");
		try {
			unregister();
			mRemoteTrackTime.setTrackTimeListener(null);
			mRemoteTrackTime.unregister();

			mRemoteTrackTime = null;
			mTrackReceiver = null;
			mStatusReceiver = null;
			mPlayingModeReceiver = null;
		} catch(Exception ex) {
			Log.e(TAG, "", ex);
		}

		super.onDestroy();
	}

	/**
	 * NOTE: it's not necessary to set mStatusIntent/mPlayingModeIntent this way here,
	 * but this approach can be used with a null receiver to get current sticky intent without broadcast receiver.
	 */
	private void registerAndLoadStatus() {
		mTrackIntent = ContextCompat.registerReceiver(this, mTrackReceiver, new IntentFilter(PowerampAPI.ACTION_TRACK_CHANGED), ContextCompat.RECEIVER_EXPORTED);
		mStatusIntent = ContextCompat.registerReceiver(this, mStatusReceiver, new IntentFilter(PowerampAPI.ACTION_STATUS_CHANGED), ContextCompat.RECEIVER_EXPORTED);
		mPlayingModeIntent = ContextCompat.registerReceiver(this, mPlayingModeReceiver, new IntentFilter(PowerampAPI.ACTION_PLAYING_MODE_CHANGED), ContextCompat.RECEIVER_EXPORTED);
		ContextCompat.registerReceiver(this, mMediaButtonIgnoredReceiver, new IntentFilter(PowerampAPI.ACTION_MEDIA_BUTTON_IGNORED), ContextCompat.RECEIVER_EXPORTED);
	}

	private void unregister() {
		if(mTrackIntent != null) {
			try {
				unregisterReceiver(mTrackReceiver);
			} catch(Exception ignored){} // Can throw exception if for some reason broadcast receiver wasn't registered.
		}
		if(mStatusReceiver != null) {
			try {
				unregisterReceiver(mStatusReceiver);
			} catch(Exception ignored){}
		}
		if(mPlayingModeReceiver != null) {
			try {
				unregisterReceiver(mPlayingModeReceiver);
			} catch(Exception ignored){}
		}
		if(mMediaButtonIgnoredReceiver != null) {
			try {
				unregisterReceiver(mMediaButtonIgnoredReceiver);
			} catch(Exception ignored){}
		}
	}

	private BroadcastReceiver mTrackReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mTrackIntent = intent;
			processTrackIntent();
			if(LOG_VERBOSE) Log.w(TAG, "mTrackReceiver " + intent);
		}
	};

	private final BroadcastReceiver mMediaButtonIgnoredReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			debugDumpIntent(TAG, "mMediaButtonIgnoredReceiver", intent);
			Toast.makeText(MainActivity.this, intent.getAction() + " " + dumpBundle(intent.getExtras()), Toast.LENGTH_SHORT).show();
		}
	};

	void processTrackIntent() {
		mCurrentTrack = null;

		if(mTrackIntent != null) {
			mCurrentTrack = mTrackIntent.getBundleExtra(PowerampAPI.EXTRA_TRACK);
			if(mCurrentTrack != null) {
				int duration = mCurrentTrack.getInt(PowerampAPI.Track.DURATION);
				mRemoteTrackTime.updateTrackDuration(duration); // Let RemoteTrackTime know about the current song duration.
			}

			int pos = mTrackIntent.getIntExtra(PowerampAPI.Track.POSITION, -1); // Poweramp build-700+ sends position along with the track intent
			if(pos != -1) {
				mRemoteTrackTime.updateTrackPosition(pos);
			}

			updateTrackUI();

			updateAlbumArt(mCurrentTrack);
		}
	}

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
		@SuppressWarnings("synthetic-access")
		@Override
		public void onReceive(Context context, Intent intent) {
			mStatusIntent = intent;

			if(LOG_VERBOSE) debugDumpIntent(TAG, "mStatusReceiver", intent);

			updateStatusUI();
		}
	};

	private BroadcastReceiver mPlayingModeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mPlayingModeIntent = intent;

			if(LOG_VERBOSE) debugDumpIntent(TAG, "mPlayingModeReceiver", intent);

			updatePlayingModeUI();
		}
	};

	// This method updates track related info, album art.
	@SuppressLint("SetTextI18n")
	private void updateTrackUI() {
		Log.w(TAG, "updateTrackUI");

		if(mTrackIntent != null) {
			if(mCurrentTrack != null) {
				((TextView)findViewById(R.id.cat)).setText(Integer.toString(mCurrentTrack.getInt(PowerampAPI.Track.CAT)));
				((TextView)findViewById(R.id.uri)).setText(mCurrentTrack.getParcelable(PowerampAPI.Track.CAT_URI).toString());
				((TextView)findViewById(R.id.id)).setText(Long.toString(mCurrentTrack.getLong(PowerampAPI.Track.ID)));
				((TextView)findViewById(R.id.title)).setText(mCurrentTrack.getString(PowerampAPI.Track.TITLE));
				((TextView)findViewById(R.id.album)).setText(mCurrentTrack.getString(PowerampAPI.Track.ALBUM));
				((TextView)findViewById(R.id.artist)).setText(mCurrentTrack.getString(PowerampAPI.Track.ARTIST));
				((TextView)findViewById(R.id.path)).setText(mCurrentTrack.getString(PowerampAPI.Track.PATH));
				((EditText)findViewById(R.id.rating)).setText(String.valueOf(mCurrentTrack.getInt(PowerampAPI.Track.RATING, 0)));

				StringBuilder info = new StringBuilder();
				info.append("Codec: ").append(mCurrentTrack.getString(PowerampAPI.Track.CODEC)).append(" ");
				info.append("Bitrate: ").append(mCurrentTrack.getInt(PowerampAPI.Track.BITRATE, -1)).append(" ");
				info.append("Sample Rate: ").append(mCurrentTrack.getInt(PowerampAPI.Track.SAMPLE_RATE, -1)).append(" ");
				info.append("Channels: ").append(mCurrentTrack.getInt(PowerampAPI.Track.CHANNELS, -1)).append(" ");
				info.append("Duration: ").append(mCurrentTrack.getInt(PowerampAPI.Track.DURATION, -1)).append("sec ");

				((TextView)findViewById(R.id.info)).setText(info);
				return;
			}
		}
		// Else clean everything.
		((TextView)findViewById(R.id.info)).setText("");
		((TextView)findViewById(R.id.title)).setText("");
		((TextView)findViewById(R.id.album)).setText("");
		((TextView)findViewById(R.id.artist)).setText("");
		((TextView)findViewById(R.id.path)).setText("");
	}

	void updateStatusUI() {
		Log.w(TAG, "updateStatusUI");
		if(mStatusIntent != null) {
			boolean paused;

			int state = mStatusIntent.getIntExtra(PowerampAPI.EXTRA_STATE, PowerampAPI.STATE_NO_STATE); // NOTE: not used here, provides STATE_* int

			// Each status update can contain track position update as well
			int pos = mStatusIntent.getIntExtra(PowerampAPI.Track.POSITION, -1);
			if(pos != -1) {
				mRemoteTrackTime.updateTrackPosition(pos);
			}

			switch(state) {
				case PowerampAPI.STATE_PAUSED:
					paused = true;
					startStopRemoteTrackTime(true);
					break;

				case PowerampAPI.STATE_PLAYING:
					paused = false;
					startStopRemoteTrackTime(false);
					break;

				default:
				case PowerampAPI.STATE_NO_STATE:
				case PowerampAPI.STATE_STOPPED:
					mRemoteTrackTime.stopSongProgress();
					paused = true;
					break;
			}
			((Button)findViewById(R.id.play)).setText(paused ? ">" : "||");
		}
	}

	/**
	 * Updates shuffle/repeat UI
 	 */
	void updatePlayingModeUI() {
		Log.w(TAG, "updatePlayingModeUI");
		if(mPlayingModeIntent != null) {
			int shuffle = mPlayingModeIntent.getIntExtra(PowerampAPI.EXTRA_SHUFFLE, -1);
			String shuffleStr;
			switch(shuffle) {
				case PowerampAPI.ShuffleMode.SHUFFLE_ALL:
					shuffleStr = "Shuffle All";
					break;
				case PowerampAPI.ShuffleMode.SHUFFLE_CATS:
					shuffleStr = "Shuffle Categories";
					break;
				case PowerampAPI.ShuffleMode.SHUFFLE_SONGS:
					shuffleStr = "Shuffle Songs";
					break;
				case PowerampAPI.ShuffleMode.SHUFFLE_SONGS_AND_CATS:
					shuffleStr = "Shuffle Songs And Categories";
					break;
				default:
					shuffleStr = "Shuffle OFF";
					break;
			}
			((Button)findViewById(R.id.shuffle)).setText(shuffleStr);

			int repeat = mPlayingModeIntent.getIntExtra(PowerampAPI.EXTRA_REPEAT, -1);
			String repeatStr;
			switch(repeat) {
				case PowerampAPI.RepeatMode.REPEAT_ON:
					repeatStr = "Repeat List";
					break;
				case PowerampAPI.RepeatMode.REPEAT_ADVANCE:
					repeatStr = "Advance List";
					break;
				case PowerampAPI.RepeatMode.REPEAT_SONG:
					repeatStr = "Repeat Song";
					break;
				default:
					repeatStr = "Repeat OFF";
					break;
			}

			((Button)findViewById(R.id.repeat)).setText(repeatStr);
		}
	}

	/**
	 * Commands RemoteTrackTime to start or stop showing the song progress
 	 */
	void startStopRemoteTrackTime(boolean paused) {
		if(!paused) {
			mRemoteTrackTime.startSongProgress();
		} else {
			mRemoteTrackTime.stopSongProgress();
		}
	}

	@SuppressLint("SetTextI18n") void updateAlbumArt(Bundle track) {
		Log.w(TAG, "updateAlbumArt");

		ImageView aaImage = findViewById(R.id.album_art);
		TextView albumArtInfo = findViewById(R.id.album_art_info);

		if(track == null) {
			Log.w(TAG, "no track");
			aaImage.setImageBitmap(null);
			albumArtInfo.setText("no AA");
			return;
		}

		Bitmap b = PowerampAPIHelper.getAlbumArt(this, track, 1024, 1024);
		if(b != null) {
			aaImage.setImageBitmap(b);
			albumArtInfo.setText("scaled w: " + b.getWidth() + " h: " + b.getHeight());
		} else {
			albumArtInfo.setText("no AA");
			aaImage.setImageBitmap(null);
		}
	}


	/**
	 * Process a button press. Demonstrates sending various commands to Poweramp
 	 */
	@Override
	public void onClick(View v) {
		Log.w(TAG, "onClick v=" + v);
		int id = v.getId();
		if(id == R.id.play) {
			Log.w(TAG, "play");
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.TOGGLE_PLAY_PAUSE),
					FORCE_API_ACTIVITY);
		} else if(id == R.id.pause) {
			Log.w(TAG, "pause");
			// NOTE: since 867. Sending String command instead of int
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, "PAUSE"), FORCE_API_ACTIVITY);
		} else if(id == R.id.prev) {
			Log.w(TAG, "prev");
			// NOTE: since 867. Sending lowcase String command instead of int
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, "previous"), FORCE_API_ACTIVITY);
		} else if(id == R.id.next) {
			Log.w(TAG, "next");
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.NEXT),
					FORCE_API_ACTIVITY);
		} else if(id == R.id.prev_in_cat) {
			Log.w(TAG, "prev_in_cat");
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.PREVIOUS_IN_CAT),
					FORCE_API_ACTIVITY);
		} else if(id == R.id.next_in_cat) {
			Log.w(TAG, "next_in_cat");
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.NEXT_IN_CAT),
					FORCE_API_ACTIVITY);
		} else if(id == R.id.repeat) {
			Log.w(TAG, "repeat");
			// No toast for this button just for demo.
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.REPEAT),
					FORCE_API_ACTIVITY);
		} else if(id == R.id.shuffle) {
			Log.w(TAG, "shuffle");
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SHUFFLE),
					FORCE_API_ACTIVITY);
		} else if(id == R.id.repeat_all) {
			Log.w(TAG, "repeat_all");
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.REPEAT)
					.putExtra(PowerampAPI.EXTRA_REPEAT, PowerampAPI.RepeatMode.REPEAT_ON), FORCE_API_ACTIVITY);
		} else if(id == R.id.repeat_off) {
			Log.w(TAG, "repeat_off");
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.REPEAT)
					.putExtra(PowerampAPI.EXTRA_REPEAT, PowerampAPI.RepeatMode.REPEAT_NONE), FORCE_API_ACTIVITY);
		} else if(id == R.id.shuffle_all) {
			Log.w(TAG, "shuffle_all");
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SHUFFLE)
					.putExtra(PowerampAPI.EXTRA_SHUFFLE, PowerampAPI.ShuffleMode.SHUFFLE_ALL), FORCE_API_ACTIVITY);
		} else if(id == R.id.shuffle_off) {
			Log.w(TAG, "shuffle_all");
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SHUFFLE)
					.putExtra(PowerampAPI.EXTRA_SHUFFLE, PowerampAPI.ShuffleMode.SHUFFLE_NONE), FORCE_API_ACTIVITY);
		} else if(id == R.id.commit_eq) {
			Log.w(TAG, "commit_eq");
			commitEq();
		} else if(id == R.id.play_file) {
			Log.w(TAG, "play_file");
			try {
				String uri = ((TextView) findViewById(R.id.play_file_path)).getText().toString();
				if(uri.length() > "content://".length()) {
					PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
							.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
							//.putExtra(PowerampAPI.Track.POSITION, 10) // Play from 10th second.
							.setData(Uri.parse(uri)), FORCE_API_ACTIVITY);
				}
			} catch(Throwable th) {
				Log.e(TAG, "", th);
				Toast.makeText(this, th.getMessage(), Toast.LENGTH_LONG).show();
			}
		} else if(id == R.id.folders) {
			startActivitySafe(new Intent(this, FoldersActivity.class));
		} else if(id == R.id.play_album) {
			playAlbum();
		} else if(id == R.id.play_all_songs) {
			playAllSongs();
		} else if(id == R.id.play_second_artist_first_album) {
			playSecondArtistFirstAlbum();
		} else if(id == R.id.eq) {
			startActivitySafe(new Intent(this, EqActivity.class));
		} else if(id == R.id.pa_current_list) {
			startActivitySafe(new Intent(PowerampAPI.ACTION_SHOW_CURRENT));
		} else if(id == R.id.pa_folders) {
			startActivitySafe(new Intent(PowerampAPI.ACTION_OPEN_LIBRARY).setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("folders").build()));
		} else if(id == R.id.pa_all_songs) {
			startActivitySafe(new Intent(PowerampAPI.ACTION_OPEN_LIBRARY).setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("files").build()));
		} else if(id == R.id.create_playlist) {
			createPlaylistAndAddToIt();
		} else if(id == R.id.create_playlist_w_streams) {
			createPlaylistWStreams();
		} else if(id == R.id.goto_created_playlist) {
			gotoCreatedPlaylist();
		} else if(id == R.id.add_to_q_and_goto_q) {
			addToQAndGotoQ();
		} else if(id == R.id.queue) {
			startActivitySafe(new Intent(PowerampAPI.ACTION_OPEN_LIBRARY).setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("queue").build()));
		} else if(id == R.id.get_all_prefs) {
			getAllPrefs();
		} else if(id == R.id.get_pref) {
			getPref();
		}
	}

	private void startActivitySafe(@NonNull Intent intent) {
		try {
			startActivity(intent);
		} catch(Throwable th) {
			Log.e(TAG, "FAIL intent=" + intent, th);
		}
	}

	public void openNowPlayingTracks(View view) {
		if(mCurrentTrack != null) {
			Uri catUri = mCurrentTrack.getParcelable(PowerampAPI.Track.CAT_URI);
			if(catUri != null) {
				// NOTE: Poweramp may include query parameters such as shs=[SHUFFLE MODE], etc. into the CAT_URI
				// To avoid shuffled and otherwise modified list, we're clearing query parameters here
				Uri uri = catUri.buildUpon().clearQuery().build();

				Log.w(TAG, "openNowPlayingTracks catUri=" + catUri + " uri=>" + uri);

				startActivitySafe(new Intent(this, TrackListActivity.class).setData(uri));
				return; // Done here
			}
		}
		Toast.makeText(this, "No current category available", Toast.LENGTH_SHORT).show();
	}

	public void seekBackward10s(View view) {
		PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
						.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SEEK)
						.putExtra(PowerampAPI.EXTRA_RELATIVE_POSITION, -10)
						.putExtra(PowerampAPI.EXTRA_LOCK, true) // If EXTRA_LOCK=true, we don't change track by seeking past start/end
						,
				FORCE_API_ACTIVITY);
	}

	public void seekForward10s(View view) {
		PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
						.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SEEK)
						.putExtra(PowerampAPI.EXTRA_RELATIVE_POSITION, 10)
						.putExtra(PowerampAPI.EXTRA_LOCK, true) // If EXTRA_LOCK=true, we don't change track by seeking past start/end
						,
				FORCE_API_ACTIVITY);
	}

	public void like(View view) {
		PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
			                                     .putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.LIKE),
													FORCE_API_ACTIVITY);
	}

	public void unlike(View view) {
		PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
			                                     .putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.LIKE),
													FORCE_API_ACTIVITY);
	}

	public void setRating(View view) {
		var ratingText = ((EditText)findViewById(R.id.rating)).getText().toString();
		try {
			var rating = Integer.parseInt(ratingText, 10);
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
				                                     .putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SET_RATING)
													 .putExtra(PowerampAPI.EXTRA_RATING, rating),
													FORCE_API_ACTIVITY);
		} catch(Exception e) {
			Log.e(TAG, "", e);
		}
	}

	/** Get the specified preference and show its name, type, value */
	@SuppressLint("SetTextI18n") private void getPref() {
		EditText prefET = findViewById(R.id.pref);
		String prefName = prefET.getText().toString();
		TextView prefsTV = findViewById(R.id.prefs);

		if(prefName.length() > 0) {
			Bundle bundle = new Bundle();
			bundle.putString(prefName, null);

			Bundle resultPrefs = null;
			try {
				resultPrefs = getContentResolver().call(PowerampAPI.ROOT_URI, PowerampAPI.CALL_PREFERENCE, null, bundle);
			} catch(IllegalArgumentException ex) {
				Log.e(TAG, "FAIL Poweramp not installed", ex);
			}

			if(resultPrefs != null) {

				Object value = resultPrefs.get(prefName);
				if(value != null) {
					prefsTV.setText(prefName + " (" + value.getClass().getSimpleName() + "): " + value);
					prefsTV.setBackground(null);
				} else {
					prefsTV.setText(prefName + ": <no value>");
					prefsTV.setBackgroundColor(0x55FF0000);
				}
				prefsTV.getParent().requestChildFocus(prefsTV, prefsTV);
			} else {
				prefsTV.setText("Call failed");
				prefsTV.setBackgroundColor(0x55FF0000);
			}
		}
	}

	/** Get all available preferences and dump the resulting bundle */
	private void getAllPrefs() {
		TextView prefsTV = findViewById(R.id.prefs);

		try {
			Bundle resultPrefs = getContentResolver().call(PowerampAPI.ROOT_URI, PowerampAPI.CALL_PREFERENCE, null, null);

			prefsTV.setText(dumpBundle(resultPrefs));
			prefsTV.getParent().requestChildFocus(prefsTV, prefsTV);
		} catch(IllegalArgumentException ex) {
			Log.e(TAG, "FAIL Poweramp not installed", ex);
		}
	}

	public void setPref(View view) {
		EditText pref = findViewById(R.id.pref);
		String name = pref.getText().toString().trim();
		if(TextUtils.isEmpty(name)) {
			pref.setError("Empty");
			return;
		}

		pref.setError(null);

		EditText prefValue = findViewById(R.id.pref_value);
		String value = prefValue.getText().toString().trim();

		Bundle request = new Bundle();
		prefValue.setError(null);
		boolean failed = false;
		// Guess the type from the value
		if(TextUtils.isEmpty(value)) {
			request.putString(name, value); // Empty value is possible only for the String
		} else if("true".equals(value)) {
			request.putBoolean(name, true);
		} else if("false".equals(value)) {
			request.putBoolean(name, false);
		} else {
			try {
				int intValue = Integer.parseInt(value);
				// We are able to parse this as int, though preference can be any type.
				// Real code should decide the type based on existing knowledge of the preference type, which don't have here
				request.putInt(name, intValue);
			} catch(NumberFormatException ex) {
				try {
					float intValue = Float.parseFloat(value);
					// We are able to parse this as float, though actual preference can by any type.
					// Real code should decide the type based on existing knowledge of the preference type, which don't have here
					request.putFloat(name, intValue);
				} catch(NumberFormatException ex2) {
					prefValue.setError("Failed to guess type");
					failed = true;
				}
			}
		}
		if(!failed) {
			TextView prefsTV = findViewById(R.id.prefs);

			// OK, let's call it

			Bundle resultPrefs = getContentResolver().call(PowerampAPI.ROOT_URI, PowerampAPI.CALL_SET_PREFERENCE, null, request);

			prefsTV.setText(dumpBundle(resultPrefs));
			prefsTV.getParent().requestChildFocus(prefsTV, prefsTV);
		}
	}

	/**
	 * Process some long presses
 	 */
	@Override
	public boolean onLongClick(View v) {
		int id = v.getId();
		if(id == R.id.play) {
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.STOP),
					FORCE_API_ACTIVITY);
			return true;
		} else if(id == R.id.next) {
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.BEGIN_FAST_FORWARD),
					FORCE_API_ACTIVITY);
			mProcessingLongPress = true;
			return true;
		} else if(id == R.id.prev) {
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.BEGIN_REWIND),
					FORCE_API_ACTIVITY);
			mProcessingLongPress = true;
			return true;
		}

		return false;
	}

	/**
	 * Process touch up event to stop ff/rw
 	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getActionMasked()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL: {
				int id = v.getId();
				if(id == R.id.next) {
					Log.e(TAG, "onTouch next ACTION_UP");
					if(mProcessingLongPress) {
						PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND,
								PowerampAPI.Commands.END_FAST_FORWARD), FORCE_API_ACTIVITY);
						mProcessingLongPress = false;
					}
					return false;
				} else if(id == R.id.prev) {
					if(mProcessingLongPress) {
						PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.EXTRA_COMMAND,
								PowerampAPI.Commands.END_REWIND), FORCE_API_ACTIVITY);
						mProcessingLongPress = false;
					}
					return false;
				}
			}
		}

		return false;
	}

	/**
	 * Just play all library songs (starting from the first)
 	 */
	private void playAllSongs() {
		PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
				.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
				.setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("files").build()),
				FORCE_API_ACTIVITY);
	}

	/**
	 * Get first album id and play it
	 */
	private void playAlbum() {
		Cursor c = getContentResolver().query(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("albums").build(), new String[]{ "albums._id", "album" }, null, null, "album");
		if(c != null) {
			if(c.moveToNext()) {
				long albumId = c.getLong(0);
				String name = c.getString(1);
				Toast.makeText(this, "Playing album: " + name, Toast.LENGTH_SHORT).show();

				PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
						.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
						.setData(PowerampAPI.ROOT_URI.buildUpon()
								.appendEncodedPath("albums")
								.appendEncodedPath(Long.toString(albumId))
								.appendEncodedPath("files")
								.build()),
						FORCE_API_ACTIVITY);
			}
			c.close();
		}
	}

	/**
	 * Play first available album from the first available artist in ARTIST_ALBUMs
 	 */
	private void playSecondArtistFirstAlbum() {
		// Get first artist.
		Cursor c = getContentResolver().query(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("artists").build(),
				new String[]{ "artists._id", "artist" },
				null, null, "artist_sort COLLATE NOCASE");
		if(c != null) {
			c.moveToNext(); // First artist.
			if(c.moveToNext()) { // Second artist.
				long artistId = c.getLong(0);
				String artist = c.getString(1);
				Cursor c2 = getContentResolver().query(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("artists_albums").build(),
						new String[] { "albums._id", "album" },
						"artists._id=?", new String[]{ Long.toString(artistId) }, "album_sort COLLATE NOCASE");
				if(c2 != null) {
					if(c2.moveToNext()) {
						long albumId = c2.getLong(0);
						String album = c2.getString(1);

						Toast.makeText(this, "Playing artist: " + artist + " album: " + album, Toast.LENGTH_SHORT).show();

						PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
								.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
								.setData(PowerampAPI.ROOT_URI.buildUpon()
										.appendEncodedPath("artists")
										.appendEncodedPath(Long.toString(artistId))
										.appendEncodedPath("albums")
										.appendEncodedPath(Long.toString(albumId))
										.appendEncodedPath("files")
										.build()
								), FORCE_API_ACTIVITY);
					}
					c2.close();
				}

			}
			c.close();
		}
	}


	/**
	 * Event handler for Dynamic Eq checkbox
 	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		findViewById(R.id.commit_eq).setEnabled(!isChecked);
	}

	/**
	 * Generates and sends presetString to Poweramp Eq
	 */
	private void commitEq() {
		StringBuilder presetString = new StringBuilder();

		TableLayout equLayout = findViewById(R.id.equ_layout);
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
				FORCE_API_ACTIVITY);
	}

	/**
	 * Applies correct seekBar-to-float scaling
 	 */
	private float seekBarToValue(String name, int progress) {
		float value;
		if("preamp".equals(name) || "bass".equals(name) || "treble".equals(name)) {
			value = progress / 100.f;
		} else {
			value = (progress - 100) / 100.f;
		}
		return value;
	}

	/**
	 * Event handler for both song progress seekbar and equalizer bands
 	 */
	@Override
	public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
		int id = bar.getId();
		if(id == R.id.song_seekbar) {
			if(fromUser) {
				sendSeek(false);
			}
		} else if(id == R.id.sleep_timer_seekbar) {
			updateSleepTimer(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	/**
	 * Force seek when user ends seeking
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		sendSeek(true);

	}

	/**
	 * Send a seek command
 	 */
	private void sendSeek(boolean ignoreThrottling) {

		int position = mSongSeekBar.getProgress();
		mRemoteTrackTime.updateTrackPosition(position);

		// Apply some throttling to avoid too many intents to be generated.
		if((mLastSeekSentTime == 0 || System.currentTimeMillis() - mLastSeekSentTime > SEEK_THROTTLE)
			|| ignoreThrottling && mLastSentSeekPosition != position // Do not send same position for cases like quick seekbar touch
		) {
			mLastSeekSentTime = System.currentTimeMillis();
			PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
					.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SEEK)
					.putExtra(PowerampAPI.Track.POSITION, position),
					FORCE_API_ACTIVITY);
			mLastSentSeekPosition = position;
			Log.w(TAG, "sendSeek sent position=" + position);
		} else {
			Log.w(TAG, "sendSeek throttled");
		}
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
					FORCE_API_ACTIVITY);
		} else {
			mSettingPreset = false;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	/**
	 * Callback from RemoteTrackTime. Updates durations (both seekbar max value and duration label)
 	 */
	@Override
	public void onTrackDurationChanged(int duration) {
		mDurationBuffer.setLength(0);

		formatTimeS(mDurationBuffer, duration, true);

		mDuration.setText(mDurationBuffer);

		mSongSeekBar.setMax(duration);
	}

	/**
	 * Callback from RemoteTrackTime. Updates the current song progress. Ensures extra event is not processed (mUpdatingSongSeekBar).
 	 */
	@Override
	public void onTrackPositionChanged(int position) {
		mElapsedBuffer.setLength(0);

		formatTimeS(mElapsedBuffer, position, false);

		mElapsed.setText(mElapsedBuffer);

		if(mSongSeekBar.isPressed()) {
			return;
		}

		mSongSeekBar.setProgress(position);
	}


	public void setSleepTimer(View view) {
		PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
						.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SLEEP_TIMER)
						.putExtra(PowerampAPI.EXTRA_SECONDS, ((SeekBar)findViewById(R.id.sleep_timer_seekbar)).getProgress())
						.putExtra(PowerampAPI.EXTRA_PLAY_TO_END, ((CheckBox)findViewById(R.id.sleep_timer_play_to_end)).isChecked()),
				FORCE_API_ACTIVITY);
	}

	public void rescan(View view) {
		var componentName = PowerampAPIHelper.getScannerServiceComponentName(this);
		if(componentName == null) {
			Log.e(TAG, "FAIL !componentName");
			return;
		}
		Intent intent = new Intent(PowerampAPI.Scanner.ACTION_SCAN_DIRS)
				.setComponent(componentName)
				.putExtra(PowerampAPI.Scanner.EXTRA_CAUSE, getPackageName() + " user requested");
		startService(intent);
	}

	public void milkRescan(View view) {
		final int powerampBuild = PowerampAPIHelper.getPowerampBuild(this);
		if(powerampBuild <= 0) {
			Log.e(TAG, "FAIL !powerampBuild");
			return;
		}

		Intent intent = new Intent(PowerampAPI.MilkScanner.ACTION_SCAN)
				.putExtra(PowerampAPI.MilkScanner.EXTRA_CAUSE, getPackageName() + " user requested");

		if(powerampBuild >= 868) {

			PowerampAPIHelper.sendPAIntent(this, intent, FORCE_API_ACTIVITY); // Since 868

		} else {
			final ComponentName milkScannerServiceComponentName = PowerampAPIHelper.getMilkScannerServiceComponentName(this);
			intent.setComponent(milkScannerServiceComponentName); // Used prior build 868
			startService(intent);
		}
	}


	// =================================================

	@SuppressLint("SetTextI18n") private void updateSleepTimer(int progress) {
		((TextView)findViewById(R.id.sleep_timer_value)).setText("Seep in " + progress + "s");
	}


	/** Retrieves Poweramp build number and normalizes it to ### form, e.g. 846002 => 846 */
	private int getPowerampBuildNumber() {
		int code = mPowerampBuildNumber;
		if(code == 0) {
			try {
				code = getPackageManager().getPackageInfo(PowerampAPIHelper.getPowerampPackageName(this), 0).versionCode;
			} catch(PackageManager.NameNotFoundException ex) {
				// code==0 here
				Log.e(TAG, "", ex);
			}
			if(code > 1000) {
				code = code / 1000;
			}
			mPowerampBuildNumber = code;
		}
		return code;
	}


	/**
	 * NOTE: real code should run on some worker thread
 	 */
	private void createPlaylistAndAddToIt() {
		int buildNumber = getPowerampBuildNumber();
		if(buildNumber == 0) {
			Log.e(TAG, "createPlaylistAndAddToIt FAIL !buildNumber");
			return;
		}

		ContentResolver cr = getContentResolver();
		Uri playlistsUri = PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("playlists").build();

		// NOTE: we need raw column names for an insert query (without table name), thus using getRawColName()

		ContentValues values = new ContentValues();
		values.put(getRawColName(TableDefs.Playlists.PLAYLIST), "Sample Playlist " + System.currentTimeMillis());
		Uri playlistInsertedUri = cr.insert(playlistsUri, values);

		if(playlistInsertedUri != null) {
			Log.w(TAG, "createPlaylistAndAddToIt inserted=" + playlistInsertedUri);

			// NOTE: we are inserting into /playlists/#/files, playlistInsertedUri (/playlists/#) is not valid for entries insertion
			Uri playlistEntriesUri = playlistInsertedUri.buildUpon().appendEncodedPath("files").build();

			mLastCreatedPlaylistFilesUri = playlistEntriesUri;

			// Select up to 10 random files
			final int numFilesToInsert = 10;
			Uri filesUri = PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("files").build();
			Cursor c = getContentResolver().query(filesUri, new String[]{ TableDefs.Files._ID, TableDefs.Files.NAME, TableDefs.Folders.PATH }, null, null,
					"RANDOM() LIMIT " + numFilesToInsert);

			int sort = 0;

			if(c != null) {
				while(c.moveToNext()) {
					long fileId = c.getLong(0);
					String fileName = c.getString(1);
					String folderPath = c.getString(2);

					values.clear();
					values.put(getRawColName(TableDefs.PlaylistEntries.FOLDER_FILE_ID), fileId);

					// Playlist behavior changed in Poweramp build 842 - now each playlist entry should contain full path
					// This restriction was uplifted in build 846, but anyway, it's preferable to fill playlist entry folder_path and file_name columns to allow
					// easy resolution of playlist entries in case user changes music folders, storage, etc.
					if(buildNumber >= 842) {
						values.put(getRawColName(TableDefs.PlaylistEntries.FOLDER_PATH), folderPath);
						values.put(getRawColName(TableDefs.PlaylistEntries.FILE_NAME), fileName);
					}

					// Playlist entries are always sorted by "sort" fields, so if we want them to be in order, we should provide it.
					// If we're adding entries to existing playlist, it's a good idea to get MAX(sort) first from the given playlist

					values.put(getRawColName(TableDefs.PlaylistEntries.SORT), sort);

					Uri entryUri = cr.insert(playlistEntriesUri, values);
					if(entryUri != null) {
						Log.w(TAG, "createPlaylistAndAddToIt inserted entry fileId=" + fileId + " sort=" + sort + " folderPath=" + folderPath + " fileName=" + fileName +
								" entryUri=" + entryUri);
						sort++;
					} else {
						Log.e(TAG, "createPlaylistAndAddToIt FAILED to insert entry fileId=" + fileId);
					}
				}

				c.close();

				Toast.makeText(this, "Inserted files=" + sort, Toast.LENGTH_SHORT).show();
			}

			if(sort > 0) {
				// Force Poweramp to reload data in UI / PlayerService as we changed something
				Intent intent = new Intent(PowerampAPI.ACTION_RELOAD_DATA);
				intent.setPackage(PowerampAPIHelper.getPowerampPackageName(this));
				intent.putExtra(PowerampAPI.EXTRA_PACKAGE, getPackageName());
				// NOTE: important to send the changed table for an adequate UI / PlayerService reloading
				intent.putExtra(PowerampAPI.EXTRA_TABLE, TableDefs.PlaylistEntries.TABLE);
				if(FORCE_API_ACTIVITY) {
					intent.setComponent(PowerampAPIHelper.getApiActivityComponentName(this));
					startActivitySafe(intent);
				} else {
					sendBroadcast(intent);
				}
			}

			// Make open playlist button active
			findViewById(R.id.goto_created_playlist).setEnabled(true);

		} else {
			Log.e(TAG, "createPlaylistAndAddToIt FAILED");
		}
	}

	/**
	 * Demonstrates a playlist with the http stream entries<br>
	 * NOTE: real code should run on some worker thread
	 */
	private void createPlaylistWStreams() {
		int buildNumber = getPowerampBuildNumber();
		// We need at least 842 build
		if(buildNumber < 842) {
			Toast.makeText(this, "Poweramp build is too old", Toast.LENGTH_SHORT).show();
			return;
		}

		ContentResolver cr = getContentResolver();
		Uri playlistsUri = PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("playlists").build();

		// NOTE: we need raw column names for an insert query (without table name), thus using getRawColName()
		// NOTE: playlist with a stream doesn't differ from other (track based) playlists. Only playlist entries differ vs usual file tracks

		String playlistName = "Stream Playlist " + System.currentTimeMillis();
		ContentValues values = new ContentValues();
		values.put(getRawColName(TableDefs.Playlists.PLAYLIST), playlistName);
		Uri playlistInsertedUri = cr.insert(playlistsUri, values);

		if(playlistInsertedUri == null) {
			Toast.makeText(this, "Failed to create playlist", Toast.LENGTH_SHORT).show();
			return;
		}

		Log.w(TAG, "createPlaylistAndAddToIt inserted=" + playlistInsertedUri);

		// NOTE: we are inserting into /playlists/#/files, playlistInsertedUri (/playlists/#) is not valid for the entries insertion
		Uri playlistEntriesUri = playlistInsertedUri.buildUpon().appendEncodedPath("files").build();
		mLastCreatedPlaylistFilesUri = playlistEntriesUri;

		// To create stream entry, we just provide the url. Entry is added as the last one
		values.clear();
		values.put(getRawColName(TableDefs.PlaylistEntries.FILE_NAME), "http://64.71.77.150:8000/stream");
		Uri entryUri1 = cr.insert(playlistEntriesUri, values);

		values.clear();
		values.put(getRawColName(TableDefs.PlaylistEntries.FILE_NAME), "http://94.23.205.82:5726/;stream/1");
		Uri entryUri2 = cr.insert(playlistEntriesUri, values);

		if(entryUri1 != null && entryUri2 != null) {
			Toast.makeText(this, "Inserted streams OK, playlist=" + playlistName, Toast.LENGTH_SHORT).show();

			// Force Poweramp to reload data in UI / PlayerService as we changed something
			Intent intent = new Intent(PowerampAPI.ACTION_RELOAD_DATA);
			intent.setPackage(PowerampAPIHelper.getPowerampPackageName(this));
			intent.putExtra(PowerampAPI.EXTRA_PACKAGE, getPackageName());
			// NOTE: important to send the changed table for an adequate UI / PlayerService reloading
			intent.putExtra(PowerampAPI.EXTRA_TABLE, TableDefs.PlaylistEntries.TABLE);
			if(FORCE_API_ACTIVITY) {
				intent.setComponent(PowerampAPIHelper.getApiActivityComponentName(this));
				startActivitySafe(intent);
			} else {
				sendBroadcast(intent);
			}
		}

		// Make open playlist button active
		findViewById(R.id.goto_created_playlist).setEnabled(true);
	}

	private void gotoCreatedPlaylist() {
		if(mLastCreatedPlaylistFilesUri != null) {
			startActivitySafe(new Intent(PowerampAPI.ACTION_OPEN_LIBRARY).setData(mLastCreatedPlaylistFilesUri));
		}
	}

	private void addToQAndGotoQ() {
		ContentResolver cr = getContentResolver();
		Uri queueUri = PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("queue").build();
		ContentValues values = new ContentValues();

		// Get max sort from queue
		int maxSort = 0;
		Cursor c = getContentResolver().query(queueUri, new String[]{ "MAX(" + TableDefs.Queue.SORT + ")" }, null, null, null);
		if(c != null) {
			if(c.moveToFirst()) {
				maxSort = c.getInt(0);
			}
			c.close();
		}

		// Select up to 10 random files
		final int numFilesToInsert = 10;
		Uri filesUri = PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("files").build();
		c = getContentResolver().query(filesUri, new String[]{ TableDefs.Files._ID, TableDefs.Files.NAME }, null, null, "RANDOM() LIMIT " + numFilesToInsert);

		int inserted = 0;

		if(c != null) {
			int sort = maxSort + 1; // Start from maxSort + 1
			while(c.moveToNext()) {
				long fileId = c.getLong(0);
				String name = c.getString(1);

				values.clear();
				values.put(getRawColName(TableDefs.Queue.FOLDER_FILE_ID), fileId);
				values.put(getRawColName(TableDefs.Queue.SORT), sort);

				Uri entryUri = cr.insert(queueUri, values);
				if(entryUri != null) {
					Log.w(TAG, "addToQAndGotoQ inserted entry fileId=" + fileId + " sort=" + sort + " name=" + name + " entryUri=" + entryUri);
					sort++;
					inserted++;
				} else {
					Log.e(TAG, "addToQAndGotoQ FAILED to insert entry fileId=" + fileId);
				}
			}

			c.close();

			Toast.makeText(this, "Inserted files=" + sort, Toast.LENGTH_SHORT).show();
		}

		if(inserted > 0) {
			// Force Poweramp to reload data in UI / PlayerService as we changed something
			Intent intent = new Intent(PowerampAPI.ACTION_RELOAD_DATA);
			intent.setPackage(PowerampAPIHelper.getPowerampPackageName(this));
			intent.putExtra(PowerampAPI.EXTRA_PACKAGE, getPackageName());
			// NOTE: important to send changed table for the adequate UI / PlayerService reloading. This can also make Poweramp to go to Queue
			intent.putExtra(PowerampAPI.EXTRA_TABLE, TableDefs.Queue.TABLE);
			if(FORCE_API_ACTIVITY) {
				intent.setComponent(PowerampAPIHelper.getApiActivityComponentName(this));
				startActivitySafe(intent);
			} else {
				sendBroadcast(intent);
			}

			startActivitySafe(new Intent(PowerampAPI.ACTION_OPEN_LIBRARY).setData(queueUri));
		}
	}


	private void getComponentNames() {
		if(LOG_VERBOSE) Log.w(TAG, "getComponentNames");
		TextView tv = findViewById(R.id.component_names);
		SpannableStringBuilder sb = new SpannableStringBuilder();
		appendWithSpan(sb, "Component Names\n", new StyleSpan(Typeface.BOLD));
		appendWithSpan(sb, "Package: ", new StyleSpan(Typeface.BOLD))
				.append(orEmpty(PowerampAPIHelper.getPowerampPackageName(this)))
				.append("\n");
		appendWithSpan(sb, "PlayerService: ", new StyleSpan(Typeface.BOLD))
				.append(toStringOrEmpty(PowerampAPIHelper.getPlayerServiceComponentName(this)))
				.append("\n");
		appendWithSpan(sb, "MediaBrowserService: ", new StyleSpan(Typeface.BOLD))
				.append(toStringOrEmpty(PowerampAPIHelper.getBrowserServiceComponentName(this)))
				.append("\n");
		appendWithSpan(sb, "API Receiver: ", new StyleSpan(Typeface.BOLD))
				.append(toStringOrEmpty(PowerampAPIHelper.getApiReceiverComponentName(this)))
				.append("\n");
		appendWithSpan(sb, "Scanner: ", new StyleSpan(Typeface.BOLD))
				.append(toStringOrEmpty(PowerampAPIHelper.getScannerServiceComponentName(this)))
				.append("\n");
		appendWithSpan(sb, "Milk Scanner: ", new StyleSpan(Typeface.BOLD))
				.append(toStringOrEmpty(PowerampAPIHelper.getMilkScannerServiceComponentName(this)))
				.append("\n");
		tv.setText(sb);
		if(LOG_VERBOSE) Log.w(TAG, "getComponentNames DONE");
	}

	private @NonNull String orEmpty(@Nullable String s) {
		if(s == null) return "";
		return s;
	}

	private @NonNull String toStringOrEmpty(@Nullable ComponentName name) {
		return name != null ? name.toString() : "";
	}

	public static @NonNull String getRawColName(@NonNull String col) {
		int dot = col.indexOf('.');
		if(dot >= 0 && dot + 1 <= col.length()) {
			return col.substring(dot + 1);
		}
		return col;
	}

	public static void formatTimeS(@NonNull StringBuilder sb, int secs, boolean showPlaceholderForZero) {
		if(secs < 0 || secs == 0 && showPlaceholderForZero) {
			sb.append(NO_TIME);
			return;
		}

		int seconds = secs % 60;

		if(secs < 3600) { // min:sec
			int minutes = secs / 60;
			sb.append(minutes).append(':');
		} else { // hour:min:sec
			int hours = secs / 3600;
			int minutes = (secs / 60) % 60;

			sb.append(hours).append(':');
			if(minutes < 10) {
				sb.append('0');
			}
			sb.append(minutes).append(':');
		}
		if(seconds < 10) {
			sb.append('0');
		}
		sb.append(seconds);
	}

	public static void debugDumpIntent(@NonNull String tag, @NonNull String description, @Nullable Intent intent) {
		if(intent != null) {
			Log.w(tag, description + " debugDumpIntent action=" + intent.getAction() + " extras=" + dumpBundle(intent.getExtras()));
			Bundle track = intent.getBundleExtra(PowerampAPI.EXTRA_TRACK);
			if(track != null) {
				Log.w(tag, "track=" + dumpBundle(track));
			}
		} else {
			Log.e(tag, description + " debugDumpIntent intent is null");
		}
	}

	@SuppressWarnings("null")
	public static @NonNull String dumpBundle(@Nullable Bundle bundle) {
		if(bundle == null) {
			return "null bundle";
		}
		StringBuilder sb = new StringBuilder();
		Set<String> keys = bundle.keySet();
		sb.append("\n");
		for(String key : keys) {
			sb.append('\t').append(key).append("=");
			Object val = bundle.get(key);
			sb.append(val);
			if(val != null) {
				sb.append(" ").append(val.getClass().getSimpleName());
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private static @NonNull SpannableStringBuilder appendWithSpan(@NonNull SpannableStringBuilder sb, @Nullable CharSequence str, @NonNull Object span) {
		int start = sb.length();
		sb.append(str != null ? str : "");
		sb.setSpan(span, start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return sb;
	}
}