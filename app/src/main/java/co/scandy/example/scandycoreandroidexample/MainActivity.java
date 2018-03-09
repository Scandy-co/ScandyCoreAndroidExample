/****************************************************************************\
 * Copyright (C) 2017 Scandy
 *
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 \****************************************************************************/

package co.scandy.example.scandycoreandroidexample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import co.scandy.scandycore.ScanResolution;
import co.scandy.scandycore.ScandyCore;
import co.scandy.scandycore.ScandyCoreFileUtilities;
import co.scandy.scandycore.ScandyCoreListener;
import co.scandy.scandycore.TrackedMetadata;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  private static final int REQUEST_EXTERNAL_STORAGE = 0;
  private static final String TAG = "MainActivity";

  // How big of steps to take when adjusting the scan size
  private final float SIZE_STEP_SCALE = 0.1f;

  // Menu that has various options based on the state of Scandy Core
  private Menu mMenu;

  // The LinearLayout that contains all the scan controls
  private LinearLayout mScanControlsView;

  // mScanToggle and mPreviewToggle control the preview and scan state of Scandy Core
  private ToggleButton mScanToggle;
  private ToggleButton mPreviewToggle;
  
  // mListener binds all the callback events that Scandy Core emits
  private final ScandyCoreListener mListener = new ScandyCoreListener() {
    @Override
    public void onVisualizerReady(boolean visualizer_ready) {

    }

    @Override
    public void onConnectedUSBScanner(boolean did_connect_usb_sensor) {
    }

    @Override
    public void onDisconnectedUSBScanner(boolean did_disconnect_usb_sensor) {
    }

    @Override
    public void onLoadMeshProgress(int bytes_written, int bytes_total){
    }

    @Override
    public void onFinishedLoadingMesh(boolean success) {

    }

    @Override
    public void onFinishedGeneratingMesh(boolean success) {

    }

    @Override
    public void onFinishedSavingMesh(boolean success) {

    }

    @Override
    public void onFinishedStoppingScanning(boolean success) {
      mScanToggle.setChecked(false);
      mScanToggle.setEnabled(false);
      mPreviewToggle.setChecked(false);
      mPreviewToggle.setEnabled(true);

      if( success ) {
        findViewById(R.id.volume_actions).setVisibility(success ? View.VISIBLE : View.GONE);
      }

    }

    @Override
    public void onFinishedStartingPreview(boolean did_start) {
      // Set the mScanToggle to be the opposite of did_start preview
      mScanToggle.setEnabled(did_start);
    }

    @Override
    public void onFinishedStartingScanning(boolean did_start) {
      // Set the mPreviewToggle to be the opposite of did_start
      mPreviewToggle.setEnabled(!did_start);
    }

    @Override
    public void onScannerReady(boolean is_ready) {

      // Make the scan controls view visibility be based on whether the scanner is ready
      if( mScanControlsView != null ){
        mScanControlsView.setVisibility(is_ready ? View.VISIBLE : View.GONE);
      }

      // Make the mPreviewToggle be based on whether the scanner is ready
      if (mPreviewToggle != null) {
        mPreviewToggle.setEnabled(is_ready);
      }
      // Make the mScanToggle be NOT enabled. Since it based on previewing state
      if (mScanToggle != null) {
        mScanToggle.setEnabled(false);
      }

      // The volume actions should be hidden since we have to have just stopped scanning to use them
      View view = findViewById(R.id.volume_actions);
      if (view != null) {
        view.setVisibility(View.GONE);
      }

      // Get the available ScanResolutions is successful
      if( is_ready ) {
        // Hide the resolutions bar until we've received the right resolutions from Scandy Core
        SeekBar resolutions = (SeekBar)findViewById(R.id.scan_resolution);
        // Make sure don't have a null reference
        if( resolutions != null ) {
          resolutions.setVisibility(View.GONE);
        }
        ScandyCore.getAvailableScanResolutions();
      }
    }

    @Override
    public void onFinishedGettingScanSize(float[] size_meters_xyz) {

    }

    @Override
    public void onFinishedSettingScanSize(boolean success) {

    }

    @Override
    public void onResolutionsAvailable(ScanResolution[] scanResolutions) {
      // Update the available resolutions based on callback
      SeekBar resolutions = (SeekBar)findViewById(R.id.scan_resolution);
      // Make sure don't have a null reference
      if( resolutions != null ) {
        resolutions.setVisibility(View.VISIBLE);
        resolutions.setMax(scanResolutions.length - 1);
      }
    }

    @Override
    public void onFinishedGettingScanResolution(boolean success) {

    }

    @Override
    public void onFinishedSettingScanResolution(boolean success) {

    }

    @Override
    public void onFinishedQuit(boolean successfully_quit) {

    }
  
    @Override
    public void onTrackingDidUpdate(TrackedMetadata trackedMetadata) {
    
    }
  
    @Override
    public void onFinishedSavingScreenShot(boolean b) {
    
    }
  };

  // onCreate for MainActivity
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Setup all the Android UI stuff
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Create a reference to this so that it may be used in a callback
    final MainActivity weak_self = this;
    // Set the onClick for the FAB to request permission
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.storage_permission_fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ActivityCompat.requestPermissions( weak_self, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            REQUEST_EXTERNAL_STORAGE);
        return;
      }
    });

    // Setup the Drawer. The left side slide out menu
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.setDrawerListener(toggle);
    toggle.syncState();

    // Setup the navigation menu. The ... hamburger
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    mMenu = navigationView.getMenu();

    // Add our ScandyCore listener to ScandyCore so we can get updates
    // NOTE: by including co.scandy.scandycore.ScandyCoreVisualizer, we have a ScandyCore instance
    // already made for us. ScandyCore has all static methods
    ScandyCore.addListener(mListener);
  }

  @Override
  public void onResume(){
    super.onResume();

    // Check to see if we can enable scanning
    enableScanMode( ScandyCoreFileUtilities.hasStoragePermission() );
    
    ScandyCore.resume();
  }
  
  @Override
  public void onPause(){
    super.onPause();
    
    ScandyCore.pause();
  }


  /**
   * Helper function to read a string from a file in the assets directory
   * @param inFile The name of the file in the assets directory
   * @return       The contents of the file as a String
   */
  public String readStringFromAssetsFile(String inFile) {
    String tContents = "";

    try {
      InputStream stream = getAssets().open(inFile);

      int size = stream.available();
      byte[] buffer = new byte[size];
      stream.read(buffer);
      stream.close();
      tContents = new String(buffer);
    } catch (IOException e) {
      // Handle exceptions here
      Log.e(TAG, e.getMessage());
    }

    return tContents;

  }

  private void enableScanMode(boolean enable_scanning) {
    if( enable_scanning ) {
      ((FloatingActionButton) findViewById(R.id.storage_permission_fab)).setVisibility(View.GONE);
      /**
       * Now that we file permissions let's read the license file from App Cache and send it to Scandy Core
       */
      String license = readStringFromAssetsFile("scandycore_license.json");
      ScandyCore.setLicense(license);
    } else {
      ((FloatingActionButton) findViewById(R.id.storage_permission_fab)).setVisibility(View.VISIBLE);
    }

    mMenu.findItem(R.id.initialize_button).setVisible( enable_scanning );
    mMenu.findItem(R.id.loadmesh_button).setVisible( enable_scanning );
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         String permissions[], int[] grantResults) {
    switch (requestCode) {
      case REQUEST_EXTERNAL_STORAGE: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          // permission was granted, yay!
          enableScanMode(true);
        } else {
          enableScanMode(false);
        }
        break;
      }
    }
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);

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

  void bindScanControls(){
    // Listen for changes to size bar
    ((SeekBar) findViewById(R.id.scan_size)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // Update Scandy Core scan size based on the value of the seekbar
        float x = (float) (((SeekBar) findViewById(R.id.scan_size)).getProgress() * SIZE_STEP_SCALE);
        x += 0.25;
        ScandyCore.setScanSize(x,x,x);

        // Update the TextView with the new scan size
        TextView textView = (TextView) findViewById(R.id.scan_size_text);
        textView.setText(textView.getContentDescription() + " " + x + "m");
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    // Listen for changes to resolution bar
    ((SeekBar)findViewById(R.id.scan_resolution)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // The ScanResolution id is the index of the seekbar + 1 to offset the 0 index.
        ScanResolution resolution = new ScanResolution(progress + 1, "");
        // Tell Scandy Core that we want a new resolution
        ScandyCore.setResolution(resolution);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    // Listen for clicking on the Mesh button
    ((Button) findViewById(R.id.mesh)).setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Tell Scandy Core to generate a mesh based on the last scan
        ScandyCore.generateMesh();
      }
    });

    // Listen for clicking on the Save button
    ((Button) findViewById(R.id.save)).setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Save the mesh that Scandy Core just generated through scanning
        // NOTE: you could ask a user where to save this scan
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        ScandyCore.saveMesh(dir.getAbsolutePath() + "ScandyCoreAndroidExample-"+ System.currentTimeMillis() + ".ply");
      }
    });

    // First initialize all our class Toggle Buttons
    mPreviewToggle = (ToggleButton) findViewById(R.id.preview_toggle);
    mScanToggle = (ToggleButton) findViewById(R.id.scan_toggle);

    // Configure Preview Toggle
    mPreviewToggle.setEnabled(false);
    mPreviewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Start / stop the preview based on this button
        if (isChecked) {
          ScandyCore.startPreview();
        } else {
          ScandyCore.stopScanning();
        }
      }
    });

    // Configure Scan Toggle
    mScanToggle.setEnabled(false);
    mScanToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Start / stop the scan based on this button
        if (isChecked) {
          ScandyCore.startScanning();
        } else {
          ScandyCore.stopScanning();
        }
      }
    });

  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.initialize_button) {
      // Setup the scan_controls
      if( mScanControlsView == null ) {
        // Find the controls_container
        ViewStub stub = (ViewStub) findViewById(R.id.controls_container);
        // Set the resource to be the scan_controls
        stub.setLayoutResource(R.layout.scan_controls);
        // Store the view inflated as the mScanControlsView
        mScanControlsView = (LinearLayout) stub.inflate();
        // Bind the newly created controls to their callbacks
        bindScanControls();
      } else {
        // Or just make them visible again
        mScanControlsView.setVisibility(View.VISIBLE);
      }

      // Initialize the scanner with the first file we find in the Download directory
      File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
      String rrf_path = "";
      // NOTE: you could present your users with a list of files to chose from if you felt like it!
      for ( File file : dir.listFiles()) {
        if( file.getName().contains(".rrf") ){
          rrf_path = file.getAbsolutePath();
          break;
        }
      }

      // check to see if we have a USB sensor attached
      if( ScandyCore.hasValidSensor() ) {
        ScandyCore.initializeScanner();
      }
      else if( rrf_path != "" ) {
        ScandyCore.initializeScanner(rrf_path);
      } else {
        // Show an Alert that we didn't find anything to initialize a scanner with
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("No scanner found");
        alertDialog.setMessage("Didn't find a pico flexx or a pre-recorded file.");
        alertDialog.show();
      }

    } else if (id == R.id.loadmesh_button) {
      // Make sure to uninitialize the scanner. Scandy Core gracefully handles various states.
      ScandyCore.uninitializeScanner();

      try {
        // Lets load a test Obj
        ScandyCore.loadMeshFromURL(new URL("https://s3.amazonaws.com/scandycore-test-assets/scandy-obj.zip"));
      } catch (MalformedURLException e) {
        Log.e(TAG, e.getMessage());
      }
    } else if (id == R.id.nav_manage) {

    } else if (id == R.id.nav_share) {

    } else if (id == R.id.nav_send) {

    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }
}
