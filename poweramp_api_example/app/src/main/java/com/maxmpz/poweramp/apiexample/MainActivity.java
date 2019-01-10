/*
Copyright (C) 2011-2019 Maksim Petrov

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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPIHelper;
import com.maxmpz.poweramp.player.RemoteTrackTime;
import com.maxmpz.poweramp.player.RemoteTrackTime.TrackTimeListener;
import com.maxmpz.poweramp.player.TableDefs;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;


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


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		findViewById(R.id.play).setOnClickListener(this);
		findViewById(R.id.play).setOnLongClickListener(this);
		findViewById(R.id.pause).setOnClickListener(this);
		findViewById(R.id.prev).setOnClickListener(this);
		findViewById(R.id.prev).setOnLongClickListener(this);
		findViewById(R.id.next).setOnClickListener(this);
		findViewById(R.id.next).setOnLongClickListener(this);
		findViewById(R.id.prev).setOnTouchListener(this);
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

		mSongSeekBar = (SeekBar)findViewById(R.id.song_seekbar);
		mSongSeekBar.setOnSeekBarChangeListener(this);

		mDuration = (TextView)findViewById(R.id.duration);
		mElapsed = (TextView)findViewById(R.id.elapsed);

		mRemoteTrackTime = new RemoteTrackTime(this);
		mRemoteTrackTime.setTrackTimeListener(this);

		((TextView)findViewById(R.id.play_file_path)).setText(findFirstMP3(Environment.getExternalStorageDirectory()));
		findViewById(R.id.play_file).setOnClickListener(this);

		findViewById(R.id.folders).setOnClickListener(this);

		findViewById(R.id.play_album).setOnClickListener(this);
		findViewById(R.id.play_all_songs).setOnClickListener(this);
		findViewById(R.id.play_second_artist_first_album).setOnClickListener(this);

		findViewById(R.id.pa_current_list).setOnClickListener(this);
		findViewById(R.id.pa_folders).setOnClickListener(this);
		findViewById(R.id.pa_all_songs).setOnClickListener(this);

		// Ask Poweramp for permission to access its data provider. Needed only if we want to make queries against Poweramp database, e.g. in FilesActivity/FoldersActivity
		// NOTE: this will work only if Poweramp process is alive
		// This actually should be done once per this app installation, for simplicity, we use per-process static field here
		if(!sPermissionAsked) {
			Intent intent = new Intent(PowerampAPI.ACTION_ASK_FOR_DATA_PERMISSION);
			intent.setPackage(PowerampAPI.PACKAGE_NAME);
			intent.putExtra(PowerampAPI.PACKAGE, getPackageName());
			sendBroadcast(intent);
			sPermissionAsked = true;
		}
	}


	/**
	 * When screen is rotated, by default Android will reapply all saved values to the controls, calling the event handlers, which generate appropriate intents, thus,
	 * on screen rotation some commands could be sent to Poweramp unintentionally.
	 * As this activity always syncs everything with the actual state of Poweramp, this automatic restoring of state is just non needed.
	 * <br><br>
	 * Nevertheless, the actual implementation should probably manipulate per view View.setSaveEnabled() for specific controls, as empty onSaveInstanceState here denies save
	 * for everything
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
	 * but this approach can be used with null receiver to get current sticky intent without broadcast receiver.
	 */
	private void registerAndLoadStatus() {
		registerReceiver(mAAReceiver, new IntentFilter(PowerampAPI.ACTION_AA_CHANGED));
		mTrackIntent = registerReceiver(mTrackReceiver, new IntentFilter(PowerampAPI.ACTION_TRACK_CHANGED));
		mStatusIntent = registerReceiver(mStatusReceiver, new IntentFilter(PowerampAPI.ACTION_STATUS_CHANGED));
		mPlayingModeIntent = registerReceiver(mPlayingModeReceiver, new IntentFilter(PowerampAPI.ACTION_PLAYING_MODE_CHANGED));
	}

	private void unregister() {
		if(mTrackIntent != null) {
			try {
				unregisterReceiver(mTrackReceiver);
			} catch(Exception ex){} // Can throw exception if for some reason broadcast receiver wasn't registered.
		}
		try {
			unregisterReceiver(mAAReceiver);
		} catch(Exception ex){} // Can throw exception if for some reason broadcast receiver wasn't registered.
		if(mStatusReceiver != null) {
			try {
				unregisterReceiver(mStatusReceiver);
			} catch(Exception ex){}
		}
		if(mPlayingModeReceiver != null) {
			try {
				unregisterReceiver(mPlayingModeReceiver);
			} catch(Exception ex){}
		}
	}

	private BroadcastReceiver mTrackReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mTrackIntent = intent;
			processTrackIntent();
			Log.w(TAG, "mTrackReceiver " + intent);
		}
	};

	private BroadcastReceiver mAAReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateAlbumArt(mCurrentTrack);

			Log.w(TAG, "mAAReceiver " + intent);
		}
	};

	void processTrackIntent() {
		mCurrentTrack = null;

		if(mTrackIntent != null) {
			mCurrentTrack = mTrackIntent.getBundleExtra(PowerampAPI.TRACK);
			if(mCurrentTrack != null) {
				int duration = mCurrentTrack.getInt(PowerampAPI.Track.DURATION);
				mRemoteTrackTime.updateTrackDuration(duration); // Let RemoteTrackTime know about current song duration.
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

			debugDumpIntent(TAG, "mStatusReceiver", intent);

			updateStatusUI();
		}
	};

	private BroadcastReceiver mPlayingModeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mPlayingModeIntent = intent;

			debugDumpIntent(TAG, "mPlayingModeReceiver", intent);

			updatePlayingModeUI();
		}
	};

	// This method updates track related info, album art.
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

			int state = mStatusIntent.getIntExtra(PowerampAPI.STATE, PowerampAPI.STATE_NO_STATE); // NOTE: not used here, provides STATE_* int

			// Each status update can contain track position update as well.
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
			int shuffle = mPlayingModeIntent.getIntExtra(PowerampAPI.SHUFFLE, -1);
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

			int repeat = mPlayingModeIntent.getIntExtra(PowerampAPI.REPEAT, -1);
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
	 * Commands RemoteTrackTime to start or stop showing song progress
 	 */
	void startStopRemoteTrackTime(boolean paused) {
		if(!paused) {
			mRemoteTrackTime.startSongProgress();
		} else {
			mRemoteTrackTime.stopSongProgress();
		}
	}

	/**
	 * Find first mp3 in dir or in any sub-folder of it
	 */
	private String findFirstMP3(File dir) {
		try {
			findFirstMP3InFolder(dir);
		} catch(FileFoundException ex) {
			return ex.mFile.getPath();
		}
		return "";
	}

	@SuppressWarnings("serial")
	private static class FileFoundException extends RuntimeException {
		File mFile;
		FileFoundException(File file) {
			mFile = file;
		}
	}

	void findFirstMP3InFolder(File dir) {
		dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File child) {
				if(child.isDirectory()) {
					findFirstMP3InFolder(child);
				} else {
					String fileName = child.getName();
					if(fileName.regionMatches(true, fileName.length() - "mp3".length(), "mp3", 0, "mp3".length())){
						throw new FileFoundException(child);
					}
				}
				return false;
			}
		});
	}
	void updateAlbumArt(Bundle track) {
		Log.w(TAG, "updateAlbumArt");

		ImageView aaImage = ((ImageView)findViewById(R.id.album_art));
		TextView albumArtInfo = (TextView)findViewById(R.id.album_art_info);

		if(track == null) {
			Log.w(TAG, "no AA");
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
	 * Process button press. Demonstrates sending various commands to Poweramp
 	 */
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.play:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.TOGGLE_PLAY_PAUSE));
				break;

			case R.id.pause:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.PAUSE));
				break;

			case R.id.prev:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.PREVIOUS));
				break;

			case R.id.next:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.NEXT));
				break;

			case R.id.prev_in_cat:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.PREVIOUS_IN_CAT));
				break;

			case R.id.next_in_cat:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.NEXT_IN_CAT));
				break;

			case R.id.repeat:
				// No toast for this button just for demo.
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.REPEAT).putExtra(PowerampAPI.SHOW_TOAST, false));
				break;

			case R.id.shuffle:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SHUFFLE));
				break;

			case R.id.repeat_all:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.REPEAT)
						.putExtra(PowerampAPI.REPEAT, PowerampAPI.RepeatMode.REPEAT_ON));
				break;

			case R.id.repeat_off:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.REPEAT)
						.putExtra(PowerampAPI.REPEAT, PowerampAPI.RepeatMode.REPEAT_NONE));
				break;

			case R.id.shuffle_all:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SHUFFLE)
						.putExtra(PowerampAPI.SHUFFLE, PowerampAPI.ShuffleMode.SHUFFLE_ALL));
				break;

			case R.id.shuffle_off:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SHUFFLE)
						.putExtra(PowerampAPI.SHUFFLE, PowerampAPI.ShuffleMode.SHUFFLE_NONE));
				break;

			case R.id.commit_eq:
				commitEq();
				break;

			case R.id.play_file:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
						.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
						//.putExtra(PowerampAPI.Track.POSITION, 10) // Play from 10th second.
						.setData(Uri.parse("file://" + ((TextView)findViewById(R.id.play_file_path)).getText().toString())));
				break;

			case R.id.folders:
				startActivity(new Intent(this, FoldersActivity.class));
				break;

			case R.id.play_album:
				playAlbum();
				break;

			case R.id.play_all_songs:
				playAllSongs();
				break;

			case R.id.play_second_artist_first_album:
				playSecondArtistFirstAlbum();
				break;

			case R.id.eq:
				startActivity(new Intent(this, EqActivity.class));
				break;

			case R.id.pa_current_list:
				startActivity(new Intent(PowerampAPI.ACTION_SHOW_CURRENT));
				break;

			case R.id.pa_folders:
				startActivity(new Intent(PowerampAPI.ACTION_OPEN_LIBRARY).setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("folders").build()));
				break;

			case R.id.pa_all_songs:
				startActivity(new Intent(PowerampAPI.ACTION_OPEN_LIBRARY).setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("files").build()));
				break;

			case R.id.create_playlist:
				createPlaylistAndAddToIt();
				break;

			case R.id.goto_created_playlist:
				gotoCreatedPlaylist();
				break;

			case R.id.add_to_q_and_goto_q:
				addToQAndGotoQ();
				break;


			case R.id.queue:
				startActivity(new Intent(PowerampAPI.ACTION_OPEN_LIBRARY).setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("queue").build()));
				break;
		}
	}

	/**
	 * Process some long presses
 	 */
	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()) {
			case R.id.play:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.STOP));
				return true;

			case R.id.next:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.BEGIN_FAST_FORWARD));
				return true;

			case R.id.prev:
				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.BEGIN_REWIND));
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
		if(event.getAction() == MotionEvent.ACTION_UP) {
			switch(v.getId()) {
				case R.id.next:
					PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.END_FAST_FORWARD));
					break;

				case R.id.prev:
					PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.END_REWIND));
					break;
			}
		}

		return false;
	}

	/**
	 * Just play all library songs (starting from first)
 	 */
	private void playAllSongs() {
		PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
				.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
				.setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("files").build()));
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

				PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
						.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
						.setData(PowerampAPI.ROOT_URI.buildUpon()
								.appendEncodedPath("albums")
								.appendEncodedPath(Long.toString(albumId))
								.appendEncodedPath("files")
								.build()));
			}
			c.close();
		}
	}

	/**
	 * Play first available album from first available artist in ARTIST_ALBUMs
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

						PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
								.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
								.setData(PowerampAPI.ROOT_URI.buildUpon()
										.appendEncodedPath("artists")
										.appendEncodedPath(Long.toString(artistId))
										.appendEncodedPath("albums")
										.appendEncodedPath(Long.toString(albumId))
										.appendEncodedPath("files")
										.build()
								));
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

		TableLayout equLayout = (TableLayout)findViewById(R.id.equ_layout);
		int count = equLayout.getChildCount();
		for(int i = count - 1; i >= 0; i--) {
			SeekBar bar = (SeekBar)((ViewGroup)equLayout.getChildAt(i)).getChildAt(1);
			String name = (String)bar.getTag();
			float value = seekBarToValue(name, bar.getProgress());
			presetString.append(name).append("=").append(value).append(";");
		}

		PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SET_EQU_STRING).putExtra(PowerampAPI.VALUE, presetString.toString()));
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
		switch(bar.getId()) {
			case R.id.song_seekbar:
				if(fromUser) {
					sendSeek(false);
				}
				break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	/**
	 * Force seek when user ends seeking.
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		sendSeek(true);

	}

	/**
	 * Send seek command
 	 */
	private void sendSeek(boolean ignoreThrottling) {

		int position = mSongSeekBar.getProgress();
		mRemoteTrackTime.updateTrackPosition(position);

		// Apply some throttling to avoid too many intents to be generated.
		if(ignoreThrottling || mLastSeekSentTime == 0 || System.currentTimeMillis() - mLastSeekSentTime > SEEK_THROTTLE) {
			mLastSeekSentTime = System.currentTimeMillis();
			PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SEEK).putExtra(PowerampAPI.Track.POSITION, position));
			Log.w(TAG, "sent");
		} else {
			Log.w(TAG, "throttled");
		}
	}


	/**
	 * Event handler for Presets spinner
 	 */
	@Override
	public void onItemSelected(AdapterView<?> adapter, View item, int pos, long id) {
		if(!mSettingPreset) {
			PowerampAPIHelper.startPAService(this, new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SET_EQU_PRESET).putExtra(PowerampAPI.ID, id));
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
	 * Callback from RemoteTrackTime. Updates current song progress. Ensures extra event is not processed (mUpdatingSongSeekBar).
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

	/**
	 * NOTE: real code should run on some worker thread
 	 */
	private void createPlaylistAndAddToIt() {
		ContentResolver cr = getContentResolver();
		Uri playlistsUri = PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("playlists").build();

		// NOTE: we need raw column names for insert queries, thus, using getRawColName()

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
			Cursor c = getContentResolver().query(filesUri, new String[]{ TableDefs.Files._ID, TableDefs.Files.NAME }, null, null, "RANDOM() LIMIT " + numFilesToInsert);

			int sort = 0;

			if(c != null) {
				while(c.moveToNext()) {
					long fileId = c.getLong(0);
					String name = c.getString(1);

					values.clear();
					values.put(getRawColName(TableDefs.PlaylistEntries.FOLDER_FILE_ID), fileId);

					// Playlist entries are always sorted by "sort" fields, so if we want them to be in order, we should provide it.
					// If we're adding entries to existing playlist, it's a good idea to get MAX(sort) first from the given playlist

					values.put(getRawColName(TableDefs.PlaylistEntries.SORT), sort);

					Uri entryUri = cr.insert(playlistEntriesUri, values);
					if(entryUri != null) {
						Log.w(TAG, "createPlaylistAndAddToIt inserted entry fileId=" + fileId + " sort=" + sort + " name=" + name + " entryUri=" + entryUri);
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
				intent.setPackage(PowerampAPI.PACKAGE_NAME);
				intent.putExtra(PowerampAPI.PACKAGE, getPackageName());
				intent.putExtra(PowerampAPI.TABLE, TableDefs.PlaylistEntries.TABLE); // NOTE: important to send changed table for adequate UI / PlayerService reloading
				sendBroadcast(intent);
			}

			// Make open playlist button active
			findViewById(R.id.goto_created_playlist).setEnabled(true);

		} else {
			Log.e(TAG, "createPlaylistAndAddToIt FAILED");
		}
	}

	private void gotoCreatedPlaylist() {
		if(mLastCreatedPlaylistFilesUri != null) {
			startActivity(new Intent(PowerampAPI.ACTION_OPEN_LIBRARY).setData(mLastCreatedPlaylistFilesUri));
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
			intent.setPackage(PowerampAPI.PACKAGE_NAME);
			intent.putExtra(PowerampAPI.PACKAGE, getPackageName());
			intent.putExtra(PowerampAPI.TABLE, TableDefs.Queue.TABLE); // NOTE: important to send changed table for adequate UI / PlayerService reloading. This can also make Poweramp to go to Queue
			sendBroadcast(intent);

			startActivity(new Intent(PowerampAPI.ACTION_OPEN_LIBRARY).setData(queueUri));
		}

	}


	public static final @NonNull String getRawColName(@NonNull String col) {
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
			Bundle track = intent.getBundleExtra(PowerampAPI.TRACK);
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
}