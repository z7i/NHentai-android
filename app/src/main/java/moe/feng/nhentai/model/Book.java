package moe.feng.nhentai.model;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Book {

	/** 必须获取到的数据 */
	public String title, other, bookId;

	/** 次要数据 */
	public String previewImageUrl, bigCoverImageUrl, titleJP, galleryId;
	public int pageCount;

	public int thumbHeight = 0, thumbWidth = 0;

	public String parodies, language, group;
	public ArrayList<String> tags = new ArrayList<>();
	public ArrayList<String> characters = new ArrayList<>();
	public ArrayList<String> artists = new ArrayList<>();

    // cn = Chinese 29963 , jp = Japanese 6346, gb = Great Britain (English) 12227
    public String langField = LANG_UNKNOWN;
    public static final String LANG_CN = "29963", LANG_JP = "6346", LANG_GB = "12227", LANG_UNKNOWN = "0";

    // "More like this" List
    public ArrayList<Book> likes = new ArrayList<>();

	/** 旧数据 */
	private String artist = null;

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
		if( (this.langField.equals(Book.LANG_JP) || this.langField.equals(Book.LANG_CN))){
			return this.titleJP != null ? this.titleJP : this.title;
		}
			return this.title != null ? this.title : this.titleJP;
	}

	public String toJSONString() {
		return new Gson().toJson(this);
	}

	public void updateDataFromOldData() {
        // Update artist tags
		if (this.artist != null && !this.artist.trim().isEmpty()) {
			this.artists.add(this.artist);
		}

        // Update pictures address
		if (this.bigCoverImageUrl.contains("i.nhentai")) {
			this.bigCoverImageUrl = this.bigCoverImageUrl.replace("i.nhentai", "t.nhentai");
		}
        if (this.previewImageUrl.contains("i.nhentai")) {
			this.previewImageUrl = this.previewImageUrl.replace("i.nhentai", "t.nhentai");
		}
        if (this.bigCoverImageUrl.contains("http://t.nhentai")) {
            this.bigCoverImageUrl = this.bigCoverImageUrl.replace("http://t.nhentai", "https://t.nhentai");
        }
        if (this.previewImageUrl.contains("http://t.nhentai")) {
            this.previewImageUrl = this.previewImageUrl.replace("http://t.nhentai", "https://t.nhentai");
        }

        // Add Language Field
        if (this.language != null) {
            if (this.language.equalsIgnoreCase("chinese")) {
                this.langField = LANG_CN;
            } else if (this.language.equalsIgnoreCase("japanese")) {
                this.langField = LANG_JP;
            } else if (this.language.equalsIgnoreCase("english")) {
                this.langField = LANG_GB;
            }
        }
	}

	public static Book toBookFromJson(String json) {
		Book book = new Gson().fromJson(json, Book.class);
		book.updateDataFromOldData();
		return book;
	}

}
