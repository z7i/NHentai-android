package moe.feng.nhentai.model;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import java.util.ArrayList;
import moe.feng.nhentai.api.BookApi;

public class Book {

	public String title, other, bookId;

	public String previewImageUrl, bigCoverImageUrl, titleJP, titlePretty, galleryId;
	public int pageCount;

	public int thumbHeight = 0, thumbWidth = 0;

	public String parodies, parodiesID, language, group, groupID;

	public ArrayList<String> tags = new ArrayList<>();
	public ArrayList<String> tagID = new ArrayList<>();

	public ArrayList<String> characters = new ArrayList<>();
	public ArrayList<String> charactersID = new ArrayList<>();

	public ArrayList<String> artists = new ArrayList<>();
	public ArrayList<String> artistsID = new ArrayList<>();

    public String langField = LANG_UNKNOWN;
    public static final String LANG_CN = "29963", LANG_JP = "6346", LANG_GB = "12227", LANG_UNKNOWN = "0";

	public String uploadTime, uploadTimeText;

	public Book() {
		this(null, null, null);
	}

	public Book(String title, String other, String bookId) {
		this.title = title;
		this.other = other;
		this.bookId = bookId;
	}

	public Book(String title, String other, String bookId, String previewImageUrl) {
		this.title = title;
		this.other = other;
		this.bookId = bookId;
		this.previewImageUrl = previewImageUrl;
	}

	public String getAvailableTitle() {

		if (titlePretty== null) {
			Log.d(Book.class.getSimpleName(), "getAvailableTitle: Pretty Title Not Available");
		}

		else if(!titlePretty.equals("")) {
			return titlePretty;
		}

		if( (this.langField.equals(Book.LANG_JP) || this.langField.equals(Book.LANG_CN))){
			if(titleJP != null){
				if(!titlePretty.equals("")){
					return titleJP;
				}
				else{
					return this.title;
				}
			}
			else {
				return this.title;
			}
		}
			return this.title != null ? this.title : this.titleJP;
	}

	public String toJSONString() {
		return new Gson().toJson(this);
	}

	public void updateDataFromOldData(Context context) {
		Log.d("HEY", "updateDataFromOldData: " + this.bookId);
		BaseMessage msg = BookApi.getBook(context,this);
		if (msg.getCode()==0){
			Book result = msg.getData();
			this.title = result.title;
			this.titleJP = result.titleJP;
			this.titlePretty = result.titlePretty;
			this.characters = result.characters;
			this.charactersID = result.charactersID;
			this.parodies = result.parodies;
			this.parodiesID = result.parodiesID;
			this.artists = result.artists;
			this.uploadTime = result.uploadTime;
			this.uploadTimeText = result.uploadTimeText;
			this.artistsID =result.artistsID;
			this.bigCoverImageUrl = result.bigCoverImageUrl;
			this.langField = result.langField;
			this.language = result.language;
			this.tags = result.tags;
			this.tagID = result.tagID;
			this.galleryId = result.galleryId;
			this.bookId = result.bookId;
			this.group = result.group;
			this.groupID = result.groupID;
			this.previewImageUrl = result.previewImageUrl;
			this.other =result.other;
		}
	}

	public static Book toBookFromJson(String json) {
		return  new Gson().fromJson(json, Book.class);
	}

}
