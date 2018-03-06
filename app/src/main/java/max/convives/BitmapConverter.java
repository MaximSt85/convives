package max.convives;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by Max on 07.12.2017.
 */

public class BitmapConverter {
    public static byte [] bitmapToByteArrayConverter (Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public static Bitmap byteArrayToBitmapConverter (byte [] byteArray) {
        if (byteArray == null) {
            return null;
        }
        else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            return bitmap;
        }
    }
}
