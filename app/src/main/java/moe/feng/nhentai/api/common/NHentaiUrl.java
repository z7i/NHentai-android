package moe.feng.nhentai.api.common;

import moe.feng.nhentai.model.Category;

public class NHentaiUrl {

	public static final String NHENTAI_HOME = "https://nhentai.net";
	public static final String NHENTAI_I = "https://i.nhentai.net";
	public static final String NHENTAI_T = "https://t.nhentai.net";

	public static String getSearchUrl(String content, int page_num) {
		String targetContent = content;
		if (targetContent.contains(" ")) {
			targetContent = targetContent.replaceAll(" ", "+");
		}
		return NHENTAI_HOME + "/api/galleries/search?query=" + targetContent + "&page=" + page_num;
	}

	public static String getBookDetailsUrl(String book_id) {
		return NHENTAI_HOME + "/api/gallery/" + book_id;
	}
	public static String getBookRecommendUrl(String book_id) {
		return NHENTAI_HOME + "/api/gallery/" + book_id + "/related";
	}

	public static String getGalleryUrl(String g_id) {
		return NHENTAI_I + "/galleries/" + g_id;
	}

	public static String getThumbGalleryUrl(String g_id) {
		return NHENTAI_T + "/galleries/" + g_id;
	}

	public static String getOriginPictureUrl(String g_id, String page_num) {
		return getPictureUrl(g_id, page_num, "jpg");
	}

	public static String getThumbPictureUrl(String g_id, String page_num) {
		return getThumbPictureUrl(g_id, page_num + "t", "jpg");
	}

	public static String getThumbUrl(String g_id) {
		return getThumbPictureUrl(g_id, "thumb", "jpg");
	}

	public static String getBigCoverUrl(String g_id) {
		return getThumbPictureUrl(g_id, "cover", "jpg");
	}

	public static String getPictureUrl(String g_id, String page_num, String file_type) {
		return getGalleryUrl(g_id) + "/" + page_num + "." + file_type;
	}

	public static String getThumbPictureUrl(String g_id, String page_num, String file_type) {
		return getThumbGalleryUrl(g_id) + "/" + page_num +  "." + file_type;
	}

	public static String getCategoryUrl(Category category, boolean isPopularType) {
		String targetName = category.id;

		if (!isPopularType)return NHENTAI_HOME + "/api/galleries/tagged?tag_id=" + targetName;
		else return NHENTAI_HOME + "/api/galleries/tagged?tag_id=" + targetName+"&sort=popular";
	}

	public static String getHomePageUrl(int page) {
		return NHENTAI_HOME + "/api/galleries/all?page=" + page;
	}

}
