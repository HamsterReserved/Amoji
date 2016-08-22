package space.hamsters.amoji;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by hamster on 16/8/22.
 * <p/>
 * AsyncTask executing root commands
 */
public class RootCommandAsyncTask extends AsyncTask<String, Void, Integer> {
    private CommandCallback mCallback;

    RootCommandAsyncTask(CommandCallback callback) {
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(String... params) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(process.getOutputStream());

            out.writeBytes(params[0] + "\n");
            out.flush();

            out.writeBytes("exit\n");
            out.flush();

            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (mCallback != null)
            if (integer == 0)
                mCallback.onSuccess();
            else
                mCallback.onFailure(integer);
    }

    public interface CommandCallback {
        void onSuccess();

        void onFailure(int returnCode);
    }
}
