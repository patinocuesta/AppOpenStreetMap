package com.example.appopenstreetmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.mapquest.mapping.MapQuest;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    //étape 4: definition objet type MApWiew pour referencier le composant dans le XML
    private MapView map = null;
    private ItemizedIconOverlay<OverlayItem> locationOverlay;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private GeoPoint startPoint = null;
    private GeoPoint endPoint = null;
    private ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();

        MapQuest.start(getApplicationContext());

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        map = findViewById(R.id.mapView);
        ArrayList<OverlayItem> items = new ArrayList<>();
        items.add(new OverlayItem("Lille", "Ville de Lille", new GeoPoint(50.6329700, 3.0585800)));
        items.add(new OverlayItem("Paris", "Ville de Paris", new GeoPoint(48.8534, 2.3488)));
        items.add(new OverlayItem("Amiens", "Ville d'Amiens", new GeoPoint(49.9000, 2.3000)));
        items.add(new OverlayItem("Chantilly", "Ville de Chantilly", new GeoPoint(49.19461, 2.47124)));


        //ajouter des Listeners aux Marqueurs, quand on va clicker sur un Marqueur
        //il nous affiche le Title et la description
        //Items: liste des Marqueurs
        //new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>()(...):
        //c'est un listener qui va permettre d'associer un traitement des qu'on tape
        //au doigt sur le marqueur
        this.locationOverlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {

                if (startPoint == null) {
                    startPoint = new GeoPoint(item.getPoint());
                    Marker startMarker = new Marker(map);
                    startMarker.setPosition(startPoint);
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(startMarker);
                    //roadManager = new OSRMRoadManager(MainActivity.this);
                    //roadManager = new MapQuestRoadManager("MAdFIx4RxtiX4uG1NRrfnfxoCGjnqnLB");
                    waypoints = new ArrayList<GeoPoint>();
                    waypoints.add(startPoint);
                    Toast.makeText(MainActivity.this, "Ville de depart: " + item.getTitle(), Toast.LENGTH_LONG).show();

                } else {
                    endPoint = new GeoPoint(item.getPoint());
                    waypoints.add(endPoint);
                    Toast.makeText(MainActivity.this, "Ville de destination: " + item.getTitle(), Toast.LENGTH_LONG).show();
                    RoadManager roadManager = new MapQuestRoadManager(BuildConfig.API_KEY);
                    roadManager.addRequestOption("routeType=fastest");
                    Road road = roadManager.getRoad(waypoints);
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road,  0x800000FF, 5.0f);
                    map.getOverlays().add(roadOverlay);
                    map.invalidate();
                }
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                if (startPoint == null) {
                    startPoint = new GeoPoint(item.getPoint());
                    Marker startMarker = new Marker(map);
                    startMarker.setPosition(startPoint);
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(startMarker);
                    waypoints = new ArrayList<GeoPoint>();
                    waypoints.add(startPoint);
                    Toast.makeText(MainActivity.this, "Ville de depart: " + item.getTitle(), Toast.LENGTH_LONG).show();

                } else {
                    endPoint = new GeoPoint(item.getPoint());
                    waypoints.add(endPoint);
                    Toast.makeText(MainActivity.this, "Ville de destination: " + item.getTitle(), Toast.LENGTH_LONG).show();
                    RoadManager roadManager = new MapQuestRoadManager(BuildConfig.API_KEY);
                    roadManager.addRequestOption("routeType=fastest");
                    Road road = roadManager.getRoad(waypoints);
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road,  0x800000FF, 5.0f);
                    map.getOverlays().add(roadOverlay);
                    map.invalidate();
                }
                return true;
            }
        }, getApplicationContext());
        // On affect les Marqueurs à la carte:
        this.map.getOverlayManager().add(this.locationOverlay);

        //on utilise un interface qui va permettre de gerer un MapView
        IMapController mapController = map.getController();
        GeoPoint startPoint = new GeoPoint(48.8534, 2.3488);
        //facteur zoom: depend du fournisseur de carte
        mapController.setZoom(9.5);
        mapController.setCenter(startPoint);
        map.setTileSource(TileSourceFactory.MAPNIK);

        requestPermissionsIfNecessary(new String[] {
                // if you need to show the current location, uncomment the line below
                // Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    }

