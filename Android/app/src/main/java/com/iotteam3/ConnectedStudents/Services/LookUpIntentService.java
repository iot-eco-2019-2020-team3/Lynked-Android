package com.iotteam3.ConnectedStudents.Services;


import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.iotteam3.ConnectedStudents.ui.home.AvailableSlackRoom;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 *
 * NOT USED !!!!!
 */
public class LookUpIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.android.networkconnect.action.FOO";
    private static final String ACTION_BAZ = "com.example.android.networkconnect.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.android.networkconnect.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.android.networkconnect.extra.PARAM2";

    public LookUpIntentService() {
        super("LookUpIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, LookUpIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, LookUpIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    static ArrayList<AvailableSlackRoom> rooms = new ArrayList<>();

    /**
     * Ausgeführt on startService
     *
     * @see IntentService
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        int roomNr = intent.getIntExtra("roomNr",0);
        int buildingNr = intent.getIntExtra("buildingNr",0);
        String uri = "url:3000/api/client/getChannel?roomNr="+roomNr+"&buildingNr="+buildingNr;
        Log.i("LookUp",uri);
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        String topic = null;
        String slackUrl = null;
        try {
            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            // Open communications link (network traffic occurs here).
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            if (stream != null) {
                // Converts Stream to String with max length of 500.
                result = readStream(stream, 500);
                try {
                    JSONObject json = new JSONObject(result);
                    topic = json.getJSONArray("channels").getJSONObject(0).getString("topic");
                    slackUrl = json.getJSONArray("channels").getJSONObject(0).getString("url");
                    Log.i("LookUp",topic+" | "+slackUrl);
                } catch (Exception e){
                    Log.e("LookUp","Json errr",e);
                }
                for (AvailableSlackRoom room:rooms  ) {
                    if (room.getChannelName().compareTo(topic) == 0){
                        room.SendNotification();
                        return;
                    }

                }
                
                AvailableSlackRoom popup = new AvailableSlackRoom(this, topic,slackUrl);
                popup.SendNotification();

                rooms.add(popup);
                Log.i("LookUp",result);
            }
        } catch (IOException e){

        }
        finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e){

                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    /**
     * Converts the contents of an InputStream to a String.
     */
    private String readStream(InputStream stream, int maxLength) throws IOException {
        String result = null;
        // Read InputStream using the UTF-8 charset.
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        // Create temporary buffer to hold Stream data with specified max length.
        char[] buffer = new char[maxLength];
        // Populate temporary buffer with Stream data.
        int numChars = 0;
        int readSize = 0;
        while (numChars < maxLength && readSize != -1) {
            numChars += readSize;
            int pct = (100 * numChars) / maxLength;
            readSize = reader.read(buffer, numChars, buffer.length - numChars);
        }
        if (numChars != -1) {
            // The stream was not empty.
            // Create String that is actual length of response body if actual length was less than
            // max length.
            numChars = Math.min(numChars, maxLength);
            result = new String(buffer, 0, numChars);
        }
        return result;
    }
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
