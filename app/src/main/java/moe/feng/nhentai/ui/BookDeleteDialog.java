package moe.feng.nhentai.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

public class BookDeleteDialog extends AlertDialog {

	protected BookDeleteDialog(Context context) {
		super(context, false, new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {

			}
		});
	}

}
