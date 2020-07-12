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

import android.content.Context;
import android.util.Log;

import com.maxmpz.poweramp.player.PowerampAPI.Cats;
import com.maxmpz.powerampapi.simplewidgetpack.R;

public class IconsHelper {
	private static final String TAG = "IconsHelper";
	private static final boolean LOG = false;
	
	private int mFolderRes;
	private int mAllSongsRes;
	private int mAlbumRes;
	private int mArtistRes;
	private int mGenreRes;
	private int mPlaylistRes;
	private int mQueueRes;
	private int mCueRes;
	private int mComposerRes;

	private int mMostPlayedRes;
	private int mTopRatedRes;
	private int mRecentlyAddedRes;
	private int mRecentlyPlayedRes;
	
	public IconsHelper(Context context) {
		mFolderRes = R.drawable.matte_folders;
		mCueRes = R.drawable.matte_folder_cue;
		mAllSongsRes = R.drawable.matte_all_songs;
		mAlbumRes = R.drawable.matte_albums;
		mArtistRes = R.drawable.matte_artists;
		mComposerRes = R.drawable.matte_composers;
		mGenreRes = R.drawable.matte_genres;
		mPlaylistRes = R.drawable.matte_playlists;
		mQueueRes = R.drawable.matte_queue;
		mMostPlayedRes = R.drawable.matte_most_played;
		mTopRatedRes = R.drawable.matte_top_rated;
		mRecentlyAddedRes = R.drawable.matte_recently_added;
		mRecentlyPlayedRes = R.drawable.matte_recently_played;
	}
	
	public int getIcon(int matchedUri, boolean isCue) {
		int res = 0;
		switch(matchedUri) {
			case Cats.FOLDERS:
				if(!isCue) {
					res = mFolderRes;
				} else {
					res = mCueRes;
				}
				break;
				
			case Cats.ROOT:
				res = mAllSongsRes;
 				break;
				
			case Cats.GENRES_ID_ALBUMS:
			case Cats.ALBUMS:
			case Cats.ARTISTS_ID_ALBUMS:
			case Cats.COMPOSERS_ID_ALBUMS:				
			case Cats.ARTISTS__ALBUMS:
				res = mAlbumRes;
				break;
				
			case Cats.GENRES:
				res = mGenreRes;
				break;

			case Cats.ARTISTS:
				res = mArtistRes;
				break;

			case Cats.COMPOSERS:
				res = mComposerRes;
				break;

			case Cats.PLAYLISTS:
				res = mPlaylistRes;
				break;
				
			case Cats.QUEUE:
				res = mQueueRes;
				break;
				
			case Cats.MOST_PLAYED:
				res = mMostPlayedRes;
				break;
				
			case Cats.TOP_RATED:
				res = mTopRatedRes;
				break;
				
			case Cats.RECENTLY_ADDED:
				res = mRecentlyAddedRes;
				break;
				
			case Cats.RECENTLY_PLAYED:
				res = mRecentlyPlayedRes;
				break;
			default:
				if(LOG) Log.e(TAG, "DEBUG no icon matched=" + matchedUri);
		}
	
		return res;
	}

	public static final class CatsV140 {
		public static final int UNKNOWN = 0;
		public static final int NONE_RAW_FILE = 1;
		public static final int ARTIST = 11;
		public static final int ALBUM = 12;
		public static final int ARTIST_ALBUM = 13;
		public static final int GENRE = 14;
		public static final int PLAYLIST = 15;
		public static final int GENRE_ALBUM = 16;		
		public static final int FOLDER_PLAYLIST = 101;
		public static final int ALL = 10;
		public static final int FOLDER = 100;
		public static final int QUEUE = 200;
	}

	public int getIconV140(int cat, boolean isCue) {
		int res;
		if(LOG) Log.e(TAG, "getIconV140 cat=" + cat);
		switch(cat) {
			case CatsV140.FOLDER:
				if(!isCue) {
					res = mFolderRes;
				} else {
					res = mCueRes;
				}
				break;
			case CatsV140.ARTIST_ALBUM:
			case CatsV140.ALBUM:
				res = mAlbumRes;
				break;
			case CatsV140.ARTIST:
				res = mArtistRes;
				break;
			case CatsV140.GENRE:
				res = mGenreRes;
				break;
			case CatsV140.FOLDER_PLAYLIST:
			case CatsV140.PLAYLIST:
				res = mPlaylistRes;
				break;
			case CatsV140.ALL:
				res = mAllSongsRes;
				break;
			case CatsV140.QUEUE:
				res = mQueueRes;
				break;
			default:
				if(LOG) Log.e(TAG, "DEBUG no icon matched=" + cat);
				res = 0;
				break;
		}
		return res;
	}
}
