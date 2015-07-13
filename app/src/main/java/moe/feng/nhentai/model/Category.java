package moe.feng.nhentai.model;

import com.google.gson.Gson;

public class Category {

	public String type, name;

	public Category() {
		this(null, null);
	}

	public Category(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public String toJSONString() {
		return new Gson().toJson(this);
	}

	public static final class Type {

		public static final String PARODY = "parody", CHARACTER = "character", TAG = "tagged",
				ARTIST = "artist", GROUP = "group", LANGUAGE = "language";

	}

}
