package me.anky.connectid.Utils;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import me.anky.connectid.R;

public class DialogUtils {

    public static void showAlertAndThenCancelable(Context context, String title, String message, final AndThen andThen) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title != null ? title : "");
        alertDialogBuilder.setMessage(message != null ? message : "");
        alertDialogBuilder.setPositiveButton(R.string.yes, (dialog, id) -> {
            if (andThen != null) {
                andThen.doThis(null);
            }
        });
        alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    public static void askQuestionAndThenCancelable(Context context, String title, String message, String positiveButtonText, String negativeButtonText, final AndThen andThenPositive, final AndThen andThenNegative) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title != null ? title : "");
        alertDialogBuilder.setMessage(message != null ? message : "");
        alertDialogBuilder.setPositiveButton(positiveButtonText, (dialog, id) -> {
            if (andThenPositive != null) {
                andThenPositive.doThis(null);
            }
        });
        alertDialogBuilder.setNegativeButton(negativeButtonText, (dialog, id) -> {
            if (andThenNegative != null) {
                andThenNegative.doThis(null);
            }
        });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    public interface AndThen {
        void doThis(Object object);
    }
}
