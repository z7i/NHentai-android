package moe.feng.nhentai.model;

import com.google.gson.Gson;

public class Category {

	public String type, name, id;

	public Category() {
		this(null, null,null);
	}

	public Category(String type, String name, String id) {
		this.type = type;
		this.name = name;
		this.id = id;
	}

	public String toJSONString() {
		return new Gson().toJson(this);
	}

	public static final class Type {

		public static final String PARODY = "parody", CHARACTER = "character", TAG = "tag",
				ARTIST = "artist", GROUP = "group", LANGUAGE = "language";

	}

}
