package moe.feng.nhentai.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class FilesUtil {

	public static String PATH_SD = Environment.getExternalStorageDirectory().getPath();
	public static String NOMEDIA_FILE = FilesUtil.PATH_SD + "/NHBooks/Books/.nomedia";

	public static boolean isFile(String url) {
		File f = new File(url);
		return f.exists() && f.isFile();
	}

	public static boolean delete(String url) {
		File f = new File(url);
		return f.delete();
	}

	public static boolean createNewFile(String url) {
		File f = new File(url);
		try {
			return f.createNewFile();
		} catch (IOException e) {
			return false;
		}
	}

}
