/*
Copyright (C) 2011-2013 Maksim Petrov

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

import java.io.File;
import java.io.FileFilter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
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
import com.maxmpz.poweramp.player.RemoteTrackTime;
import com.maxmpz.poweramp.player.RemoteTrackTime.TrackTimeListener;
import com.maxmpz.poweramp.widget.WidgetUtilsLite;

public class MainActivity extends Activity implements OnClickListener, OnLongClickListener, OnTouchListener, OnCheckedChangeListener, OnSeekBarChangeListener, OnItemSelectedListener, TrackTimeListener {
	private static final String TAG = "MainActivity";

	private Intent mTrackIntent;
	private Intent mAAIntent;
	private Intent mStatusIntent;
	private Intent mPlayingModeIntent;
	
	private Bundle mCurrentTrack;
	
	private RemoteTrackTime mRemoteTrackTime;
	private SeekBar mSongSeekBar;
	
	private TextView mDuration;
	private TextView mElapsed;
	private boolean mSettingPreset;
	
	
	/** Called when the activity is first created. */
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
    }
    
 
	/*
	 * NOTE: when screen is rotated, by default android will reapply all saved values to the controls, calling the event handlers, which generate appropriate intents, thus,
	 * on screen rotation some commands could be sent to PowerMAP unintentionally. 
	 * As this activity always syncs everything with the actual state of PowerAMP, this automatic restoring of state is just non needed.  
	 */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    }
    
	// NOTE: this method unregister all broadcast receivers on activity pause. This is the correct way of handling things - we're
	// sure no unnecessary event processing will be done for paused activity, when screen is OFF, etc.
    @Override
    protected void onPause() {
    	unregister();
    	mRemoteTrackTime.unregister();
    	
    	super.onPause();
    }
    // Register broadcast receivers.
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
    
	private void registerAndLoadStatus() {
		// Note, it's not necessary to set mStatusIntent/mPlayingModeIntent this way here,
		// but this approach can be used with null receiver to get current sticky intent without broadcast receiver.
		mAAIntent = registerReceiver(mAAReceiver, new IntentFilter(PowerampAPI.ACTION_AA_CHANGED));
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
		if(mAAIntent != null) {
			try {
				unregisterReceiver(mAAReceiver);
			} catch(Exception ex){} // Can throw exception if for some reason broadcast receiver wasn't registered.
		}
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
			mAAIntent = intent;
			
			updateAlbumArt();

			Log.w(TAG, "mAAReceiver " + intent);
		}
	};

	private void processTrackIntent() {
		mCurrentTrack = null;
		
		if(mTrackIntent != null) {
			mCurrentTrack = mTrackIntent.getBundleExtra(PowerampAPI.TRACK);
			if(mCurrentTrack != null) {
				int duration = mCurrentTrack.getInt(PowerampAPI.Track.DURATION);
				mRemoteTrackTime.updateTrackDuration(duration); // Let ReomoteTrackTime know about current song duration. 
			}
			
			updateTrackUI();
		}
	}

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mStatusIntent = intent;
			
			debugDumpStatusIntent(mStatusIntent);
			
			updateStatusUI();
		}
	};
	
	private BroadcastReceiver mPlayingModeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mPlayingModeIntent = intent;
			
			debugDumpPlayingModeIntent(intent);
			
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
	
	private void updateStatusUI() {
		Log.w(TAG, "updateStatusUI");
		if(mStatusIntent != null) {
			boolean paused = true;
			
			int status = mStatusIntent.getIntExtra(PowerampAPI.STATUS, -1);
			
			// Each status update can contain track position update as well.
			int pos = mStatusIntent.getIntExtra(PowerampAPI.Track.POSITION, -1);
			if(pos != -1) {
				mRemoteTrackTime.updateTrackPosition(pos);
			}
			
			switch(status) {
				case PowerampAPI.Status.TRACK_PLAYING:
					paused = mStatusIntent.getBooleanExtra(PowerampAPI.PAUSED, false);
					startStopRemoteTrackTime(paused);
					break;
		
				case PowerampAPI.Status.TRACK_ENDED:
				case PowerampAPI.Status.PLAYING_ENDED:
					mRemoteTrackTime.stopSongProgress();
					break;
			}
			((Button)findViewById(R.id.play)).setText(paused ? ">" : "||");
		}
	}

	// Updates shuffle/repeat UI.
	private void updatePlayingModeUI() {
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

	// Commands RemoteTrackTime to start or stop showing song progress.
	void startStopRemoteTrackTime(boolean paused) {
		if(!paused) {
			mRemoteTrackTime.startSongProgress();
		} else {
			mRemoteTrackTime.stopSongProgress();
		}
	}

    /**
     * Find first mp3 in dir or in any subdir of it.
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
	private void findFirstMP3InFolder(File dir) {
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

	private void updateAlbumArt() {
		Log.w(TAG, "updateAlbumArt");
		String directAAPath = mAAIntent.getStringExtra(PowerampAPI.ALBUM_ART_PATH);
		
		if(!TextUtils.isEmpty(directAAPath)) {
			Log.w(TAG, "has AA, albumArtPath=" + directAAPath);			
			
			((ImageView)findViewById(R.id.album_art)).setImageURI(Uri.parse(directAAPath));
		} else if(mAAIntent.hasExtra(PowerampAPI.ALBUM_ART_BITMAP)) {
			
			Bitmap albumArtBitmap = mAAIntent.getParcelableExtra(PowerampAPI.ALBUM_ART_BITMAP);
			if(albumArtBitmap != null) {
				Log.w(TAG, "has AA, bitmap");
				((ImageView)findViewById(R.id.album_art)).setImageBitmap(albumArtBitmap);
			}
		} else {
			Log.w(TAG, "no AA");
			((ImageView)findViewById(R.id.album_art)).setImageBitmap(null);
		}
	}

	private void debugDumpStatusIntent(Intent intent) {
		if(intent != null) {
			int status = intent.getIntExtra(PowerampAPI.STATUS, -1);
			boolean paused = intent.getBooleanExtra(PowerampAPI.PAUSED, false);
			boolean failed = intent.getBooleanExtra(PowerampAPI.FAILED, false);
			Log.w(TAG, "statusIntent status=" + status + " paused=" + paused + " failed=" + failed);
		} else {
			Log.e(TAG, "statusIntent: intent is null");
		}
	}

	private void debugDumpPlayingModeIntent(Intent intent) {
		if(intent != null) {
			int shuffle = intent.getIntExtra(PowerampAPI.SHUFFLE, -1);
			int repeat = intent.getIntExtra(PowerampAPI.REPEAT, -1);
			Log.w(TAG, "debugDumpPlayingModeIntent shuffle=" + shuffle + " repeat=" + repeat);
		} else {
			Log.e(TAG, "debugDumpPlayingModeIntent: intent is null");
		}
	}
	
	// Process button press. Demonstrates sending various commands to PowerAMP.
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.play:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.TOGGLE_PLAY_PAUSE));
				break;
				
			case R.id.pause:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.PAUSE));
				break;

			case R.id.prev:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.PREVIOUS));
				break;

			case R.id.next:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.NEXT));
				break;
				
			case R.id.prev_in_cat:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.PREVIOUS_IN_CAT));
				break;

			case R.id.next_in_cat:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.NEXT_IN_CAT));
				break;

			case R.id.repeat:
				// No toast for this button just for demo.
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.REPEAT).putExtra(PowerampAPI.SHOW_TOAST, false));
				break;

			case R.id.shuffle:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SHUFFLE));
				break;

			case R.id.repeat_all:
				// No toast for this button just for demo.
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.REPEAT)
						.putExtra(PowerampAPI.REPEAT, PowerampAPI.RepeatMode.REPEAT_ON));
				break;

			case R.id.repeat_off:
				// No toast for this button just for demo.
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.REPEAT)
						.putExtra(PowerampAPI.REPEAT, PowerampAPI.RepeatMode.REPEAT_NONE));
				break;

			case R.id.shuffle_all:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SHUFFLE)
						.putExtra(PowerampAPI.SHUFFLE, PowerampAPI.ShuffleMode.SHUFFLE_ALL));
				break;

			case R.id.shuffle_off:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SHUFFLE)
						.putExtra(PowerampAPI.SHUFFLE, PowerampAPI.ShuffleMode.SHUFFLE_NONE));
				break;
				
			case R.id.commit_eq:
				commitEq();
				break;
				
			case R.id.play_file:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND)
						.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
//						.putExtra(PowerampAPI.Track.POSITION, 10) // Play from 10th second.
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
				startActivity(new Intent(PowerampAPI.ACTION_SHOW_LIST).setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("folders").build()));
				break;
				
			case R.id.pa_all_songs:
				startActivity(new Intent(PowerampAPI.ACTION_SHOW_LIST).setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("files").build()));
				break;
		}
	}

	// Process few long presses.
	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()) {
			case R.id.play:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.STOP));
				return true;
			
			case R.id.next:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.BEGIN_FAST_FORWARD));
				return true;
				
			case R.id.prev:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.BEGIN_REWIND));
				return true;
		}
		
		return false;
	}

	// Process touch up event to stop ff/rw.
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP) {
			switch(v.getId()) {
			case R.id.next:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.END_FAST_FORWARD));
				break;
				
			case R.id.prev:
				startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.END_REWIND));
				break;
			}
		}

		return false;
	}
	
	// Just play all library songs (starting from first).
	private void playAllSongs() {
		startService(new Intent(PowerampAPI.ACTION_API_COMMAND)
				.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
				.setData(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("files").build()));
	}

	private void playAlbum() {
		// Get first album id.
		Cursor c = getContentResolver().query(PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("albums").build(), new String[]{ "albums._id", "album" }, null, null, "album");
		if(c != null) {
			if(c.moveToNext()) {
				long albumId = c.getLong(0);
				String name = c.getString(1);
				Toast.makeText(this, "Playing album: " + name, Toast.LENGTH_SHORT).show();

				startService(new Intent(PowerampAPI.ACTION_API_COMMAND)
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
	
	// Play first available album from first available artist in ARTIST_ALBUMs.
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
						
						startService(new Intent(PowerampAPI.ACTION_API_COMMAND)
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
	

	// Event handler for Dynamic Eq checkbox.
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		findViewById(R.id.commit_eq).setEnabled(!isChecked);
	}
	
	// Generates and sends presetString to PowerAMP Eq.
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
		
		startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SET_EQU_STRING).putExtra(PowerampAPI.VALUE, presetString.toString()));
	}
	
	// Applies correct seekBar-to-float scaling. 
	private float seekBarToValue(String name, int progress) {
		float value;
		if("preamp".equals(name) || "bass".equals(name) || "treble".equals(name)) {
			value = (float)progress / 100.f;
		} else {
			value = (float)(progress - 100) / 100.f;
		}
		return value;
	}

	// Event handler for both song progress seekbar and equalizer bands.
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

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		sendSeek(true); // Force seek when user ends seeking.
		
	}

	private long mLastSeekSentTime;
	private static int SEEK_THROTTLE = 500;
	
	final private CharArrayBuffer mDurationBuffer = new CharArrayBuffer(16);
	final private CharArrayBuffer mElapsedBuffer = new CharArrayBuffer(16);

	// Send seek command.
	private void sendSeek(boolean ignoreThrottling) {

		int position = mSongSeekBar.getProgress();
		mRemoteTrackTime.updateTrackPosition(position);
		
		// Apply some throttling to avoid too many intents to be generated.
		if(ignoreThrottling || mLastSeekSentTime == 0 || System.currentTimeMillis() - mLastSeekSentTime > SEEK_THROTTLE) {
			mLastSeekSentTime = System.currentTimeMillis();
			startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SEEK).putExtra(PowerampAPI.Track.POSITION, position));
			Log.w(TAG, "sent");
		} else {
			Log.w(TAG, "throttled");
		}
	}
	
	

	// Event handler for Presets spinner.
	@Override
	public void onItemSelected(AdapterView<?> adapter, View item, int pos, long id) {
		if(!mSettingPreset) {
			startService(new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SET_EQU_PRESET).putExtra(PowerampAPI.ID, id));
		} else {
			mSettingPreset = false;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}


	// Callback from RemoteTrackTime. Updates durations (both seekbar max value and duration label).
	@Override
	public void onTrackDurationChanged(int duration) {
		WidgetUtilsLite.formatTimeBuffer(mDurationBuffer, duration, true);
		mDuration.setText(mDurationBuffer.data, 0, mDurationBuffer.sizeCopied);
		
		mSongSeekBar.setMax(duration);
	}

	// Callback from RemoteTrackTime. Updates current song progress. Ensures extra event is not processed (mUpdatingSongSeekBar).
	@Override
	public void onTrackPositionChanged(int position) {
		WidgetUtilsLite.formatTimeBuffer(mElapsedBuffer, position, false);
		mElapsed.setText(mElapsedBuffer.data, 0, mElapsedBuffer.sizeCopied);
		
		if(mSongSeekBar.isPressed()) {
			return;
		}

		mSongSeekBar.setProgress(position);
	}
}