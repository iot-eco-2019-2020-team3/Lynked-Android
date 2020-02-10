package com.iotteam3.ConnectedStudents;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ArrayAdapter;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * @author Gabriel Goldschmitt
 * @author David Le Duy
 */
public class ConnectedStudents extends Application implements BootstrapNotifier {
    private static final String TAG = "BeaconReferenceApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private String cumulativeLog = "";
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter arrayAdapter;
    private ArrayList<Beacon> detectedBeacons = new ArrayList<>();

    public void onCreate() {
        super.onCreate();




        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
        // layout expression for other beacon types, do a web search for "setBeaconLayout"
        // including the quotes.
        //
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setDebug(true);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);

        // Uncomment the code below to use a foreground service to scan for beacons. This unlocks
        // the ability to continually scan for long periods of time in the background on Andorid 8+
        // in exchange for showing an icon at the top of the screen and a always-on notification to
        // communicate to users that your app is using resources in the background.
        //

        generateDemo();
        //beaconManager.enableForegroundServiceScanning(builder.build(), 456);

        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        //
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(2200);


        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        // Ein sicherheitsfeature deshalb uuid damit das nie gleich ist  wenn die app man neu gestartet wird
        Region region = new Region("com.iotteam3.ConnectedStudents.bootstrapRegion" + UUID.randomUUID(),
               null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        //backgroundPowerSaver = new BackgroundPowerSaver(this);

        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
        // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
        // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
    }

    // Mock für den fall der Fälle
    private void generateDemo(){
        Room room = new Room();
        room.roomNr = 311;
        room.buildingNr = 1;

        // Startet das abholen vom Server
        new HttpTask(this).execute(room);
    }

    static boolean isStarted = false;

    @Override
    public void didEnterRegion(Region arg0) {
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        Log.d(TAG, "did enter region." + arg0.getId1() + "   " + arg0.getId2());

        if (!isStarted) {
            RangeNotifier rangeNotifier = new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    if (beacons.size() > 0) {
                        Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  " + beacons.size());
                        for (Beacon bc : beacons) {
                            addBLE(bc);
                        }

                    }
                }

            };

            beaconManager.addRangeNotifier(rangeNotifier);
            try {
                beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId" + UUID.randomUUID(), null, null, null)); // so reagiert es auf mehr
                isStarted = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void didExitRegion(Region region) {
        // man kann theoretisch da noch die notification schließen
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }


    private void addBLE(Beacon newBeacon) {

        Room room = new Room();
        room.roomNr = newBeacon.getId3().toInt();
        room.buildingNr = newBeacon.getId2().toInt();

        // Startet das abholen vom Server
        new HttpTask(this).execute(room);
    }




    public String getLog() {
        return cumulativeLog;
    }

}
