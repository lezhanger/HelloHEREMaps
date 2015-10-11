package com.mapstutorial.helloheremaps;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.common.PositioningManager.*;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.PositioningManager;
import java.lang.ref.WeakReference;

public class MainActivity extends Activity {
    // map embedded in the map fragment
    private Map map = null;
    // map fragment embedded in this activity
    private MapFragment mapFragment = null;


    private PositioningManager positioningManager; // = new PositioningManager();;
    private GeoPosition currLocation;
    private GeoCoordinate currCoordinate;
    private boolean paused = false;

    // Resume positioning listener on wake up
    public void onResume() {
        super.onResume();
        System.out.println("1");
        paused = false;
        PositioningManager.getInstance().start(PositioningManager.LocationMethod.GPS_NETWORK);
    }

    // To pause positioning listener
    public void onPause() {
        PositioningManager.getInstance().stop();
        super.onPause();
        System.out.println("2");
        paused = true;
    }

    // To remove the positioning listener
    public void onDestroy() {
        // Cleanup
        PositioningManager.getInstance().removeListener(positionListener);
        map = null;
        System.out.println("3");
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapfragment);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    //positioningManager.addListener(positionListener);
                    System.out.println("4");
                    currLocation = PositioningManager.getInstance().getLastKnownPosition();
                    currCoordinate = currLocation.getCoordinate();
                    System.out.println("5");
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    // Register positioning listener
                    PositioningManager.getInstance().addListener(new WeakReference<OnPositionChangedListener>(positionListener));
                    System.out.println("6");
                    // Set the map center to the Vancouver region (no animation)
                    map.setCenter(currCoordinate,Map.Animation.NONE);
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);
                    map.getPositionIndicator().setVisible(true);
                }
                else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Define positioning listener
    private OnPositionChangedListener positionListener = new OnPositionChangedListener() {
        public void onPositionUpdated(LocationMethod method, GeoPosition position, boolean isMapMatched) {
            // set the center only when the app is in the foreground
            // to reduce CPU consumption
            if (!paused) {
                map.setCenter(position.getCoordinate(), Map.Animation.NONE);
                currLocation = position;
            }
        }
        public void onPositionFixChanged(LocationMethod method, LocationStatus status) {
        }
    };
}
