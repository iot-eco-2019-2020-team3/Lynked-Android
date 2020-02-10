package com.iotteam3.ConnectedStudents;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.iotteam3.ConnectedStudents.ui.home.AvailableSlackRoom;

import org.altbeacon.beacon.Beacon;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


// Background Task
class HttpTask extends AsyncTask<Room, Void, Void> {
    // static list zum halten aller beacons
    static ArrayList<AvailableSlackRoom> rooms = new ArrayList<>();
    private Context mContext;
    public HttpTask (Context context){
        mContext = context;
    }



    @Override

    // Holt Daten vom Server und öffnet Notification
    protected Void doInBackground(Room...  inputrooms) {
        for (Room inputroom : inputrooms) {
            int roomNr = inputroom.roomNr;
            int buildingNr = inputroom.roomNr;

            String uri = "<url/IpAdress>:3000/api/client/getChannel?roomNr=" + roomNr + "&buildingNr=" + buildingNr;
            Log.i("LookUp", uri);
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
                        Log.i("LookUp", topic + " | " + slackUrl);
                    } catch (Exception e) {
                        Log.e("LookUp", "Json errr", e);
                    }

                    // Wird wieder geöffnet falls der Raum in der Nähe ist
                    for (AvailableSlackRoom room : rooms) {
                        // bei gleichem topic kein neues mehr öffnen
                        if (room.getChannelName().compareTo(topic) == 0) {
                            room.SendNotification();
                            return null;
                        }

                    }

                    // neues popup
                    AvailableSlackRoom popup = new AvailableSlackRoom(mContext, topic, slackUrl);
                    popup.SendNotification();

                    rooms.add(popup);
                    Log.i("LookUp", result);
                }
            } catch (IOException e) {

            } finally {
                // Close Stream and disconnect HTTPS connection.
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {

                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return  null;
    }

            /**
             * Converts the contents of an InputStream to a String.
             */
            private String readStream (InputStream stream,int maxLength) throws IOException {
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



}
