
package app.demo.garagecardetection.utils;


import android.content.Context;
import android.support.v7.app.AlertDialog;


public class InterfaceUtils {

    /**
     * Show an alert dialog (Uses codes for message and title).
     *
     * @param context
     * @param message
     * @param title
     */
    public static void showAlert(Context context, int message, int title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setTitle(title);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Show an alert dialog (Uses strings for message and title).
     *
     * @param context
     * @param message
     * @param title
     */
    public static void showAlert(Context context, String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setTitle(title);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}