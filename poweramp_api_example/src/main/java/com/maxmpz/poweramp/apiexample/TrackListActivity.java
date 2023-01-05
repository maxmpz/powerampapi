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

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPIHelper;
import com.maxmpz.poweramp.player.TableDefs;

@SuppressWarnings("deprecation")
public class TrackListActivity extends ListActivity implements OnItemClickListener {
	private static final String TAG = "TrackListActivity";
	private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tracklist);

		mUri = getIntent().getData();

		Cursor c = this.getContentResolver().query(mUri,
				new String[]{ "folder_files._id AS _id",
						TableDefs.Files.TITLE_TAG + " AS title_tag", // NOTE: AS ... is needed for SimpleCursorAdapter, we probably don't need for our real custom adapters
						TableDefs.Artists.ARTIST + " AS artist"
				},
				null,
				null,
				null); // NOTE: using null as order argument - this results in user selected sorting - the same way as shown in Poweramp list
		if(c != null) startManagingCursor(c);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this, // Context.
				android.R.layout.two_line_list_item,
				c,
				new String[] { "title_tag", "artist" },
				new int[] {android.R.id.text1, android.R.id.text2},
				0);
		setListAdapter(adapter);

		ListView list = (ListView)findViewById(android.R.id.list);
		list.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
		Uri uri = mUri.buildUpon()
				.appendEncodedPath(Long.toString(id))
				.build();
		Log.w(TAG, "onItemClick uri=>" + uri);

		PowerampAPIHelper.sendPAIntent(this, new Intent(PowerampAPI.ACTION_API_COMMAND)
				.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.OPEN_TO_PLAY)
				.setData(uri),
				MainActivity.FORCE_API_ACTIVITY
		);
	}
}
