package peter.util.searcher.utils;

import android.app.Application;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * A utility class containing helpful methods
 * pertaining to file storage.
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

//    public static final String DEFAULT_DOWNLOAD_PATH =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

    /**
     * Writes a bundle to persistent storage in the files directory
     * using the specified file name. This method is a blocking
     * operation.
     *
     * @param app    the application needed to obtain the file directory.
     * @param bundle the bundle to store in persistent storage.
     * @param name   the name of the file to store the bundle in.
     */
    public static void writeBundleToStorage(final @NonNull Application app, final Bundle bundle, final @NonNull String name) {
        Observable.create(em -> {
            File outputFile = new File(app.getExternalFilesDir(null), name);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(outputFile);
                Parcel parcel = Parcel.obtain();
                parcel.writeBundle(bundle);
                outputStream.write(parcel.marshall());
                outputStream.flush();
                parcel.recycle();
            } catch (IOException e) {
                Log.e(TAG, "Unable to write bundle to storage");
            } finally {
                Utils.close(outputStream);
            }
            em.onNext("success save");
            em.onComplete();
        }).subscribeOn(AndroidSchedulers.mainThread()).observeOn(Schedulers.io()).subscribe(o -> {});
    }

    /**
     * Use this method to delete the bundle with the specified name.
     * This is a blocking call and should be used within a worker
     * thread unless immediate deletion is necessary.
     *
     * @param app  the application object needed to get the file.
     * @param name the name of the file.
     */
    public static void deleteBundleInStorage(final @NonNull Application app, final @NonNull String name) {
        File outputFile = new File(app.getExternalFilesDir(null), name);
        if (outputFile.exists()) {
            boolean suc = outputFile.delete();
            if (!suc) {
                Log.e(">>>>>", "deleteBundleInStorage fail");
            }
        }
    }

    /**
     * Reads a bundle from the file with the specified
     * name in the peristent storage files directory.
     * This method is a blocking operation.
     *
     * @param app  the application needed to obtain the files directory.
     * @param name the name of the file to read from.
     * @return a valid Bundle loaded using the system class loader
     * or null if the method was unable to read the Bundle from storage.
     */
    @Nullable
    public static Bundle readBundleFromStorage(@NonNull Application app, @NonNull String name) {
        File inputFile = new File(app.getExternalFilesDir(null), name);
        FileInputStream inputStream = null;
        try {
            //noinspection IOResourceOpenedButNotSafelyClosed
            inputStream = new FileInputStream(inputFile);
            Parcel parcel = Parcel.obtain();
            byte[] data = new byte[(int) inputStream.getChannel().size()];

            //noinspection ResultOfMethodCallIgnored
            inputStream.read(data, 0, data.length);
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            Bundle out = parcel.readBundle(ClassLoader.getSystemClassLoader());
            out.putAll(out);
            parcel.recycle();
            return out;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to read bundle from storage");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //noinspection ResultOfMethodCallIgnored
            inputFile.delete();
            Utils.close(inputStream);
        }
        return null;
    }

}
