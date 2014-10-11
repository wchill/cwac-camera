/***
  Copyright (c) 2013 CommonsWare, LLC
  
  Licensed under the Apache License, Version 2.0 (the "License"); you may
  not use this file except in compliance with the License. You may obtain
  a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.commonsware.cwac.camera.acl.demo;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import java.io.File;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements
    ActionBar.OnNavigationListener, DemoCameraFragment.Contract {
  private static final String STATE_SELECTED_NAVIGATION_ITEM=
      "selected_navigation_item";
  private static final String STATE_LOCK_TO_LANDSCAPE=
      "lock_to_landscape";
  private static final int CONTENT_REQUEST=1337;
  private static final String STATE_SINGLE_SHOT="single_shot";
  private DemoCameraFragment std=null;
  private DemoCameraFragment ffc=null;
  private DemoCameraFragment current=null;
  private boolean hasTwoCameras=(Camera.getNumberOfCameras() > 1);
  private boolean singleShot=false;
  private boolean isLockedToLandscape=false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (hasTwoCameras) {
      final ActionBar actionBar=getSupportActionBar();

      actionBar.setDisplayShowTitleEnabled(false);
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

      ArrayAdapter<CharSequence> adapter=
          ArrayAdapter.createFromResource(actionBar.getThemedContext(),
                                          R.array.nav,
                                          android.R.layout.simple_list_item_1);

      actionBar.setListNavigationCallbacks(adapter, this);
    }
    else {
      current=DemoCameraFragment.newInstance(false);

      getSupportFragmentManager().beginTransaction()
                                 .replace(R.id.container, current)
                                 .commit();
    }
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    if (hasTwoCameras) {
      if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
        getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
      }
    }

    setSingleShotMode(savedInstanceState.getBoolean(STATE_SINGLE_SHOT));
    isLockedToLandscape=
        savedInstanceState.getBoolean(STATE_LOCK_TO_LANDSCAPE);

    if (current != null) {
      current.lockToLandscape(isLockedToLandscape);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (hasTwoCameras) {
      outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                      getSupportActionBar().getSelectedNavigationIndex());
    }

    outState.putBoolean(STATE_SINGLE_SHOT, isSingleShotMode());
    outState.putBoolean(STATE_LOCK_TO_LANDSCAPE, isLockedToLandscape);
  }

  @Override
  public boolean onNavigationItemSelected(int position, long id) {
    if (position == 0) {
      if (std == null) {
        std=DemoCameraFragment.newInstance(false);
      }

      current=std;
    }
    else {
      if (ffc == null) {
        ffc=DemoCameraFragment.newInstance(true);
      }

      current=ffc;
    }

    getSupportFragmentManager().beginTransaction()
                               .replace(R.id.container, current)
                               .commit();

    findViewById(android.R.id.content).post(new Runnable() {
      @Override
      public void run() {
        current.lockToLandscape(isLockedToLandscape);
      }
    });

    return(true);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getSupportMenuInflater().inflate(R.menu.main, menu);

    menu.findItem(R.id.landscape).setChecked(isLockedToLandscape);

    return(super.onCreateOptionsMenu(menu));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.content) {
      Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      File dir=
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
      File output=new File(dir, "CameraContentDemo.jpeg");

      i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));

      startActivityForResult(i, CONTENT_REQUEST);
    }
    else if (item.getItemId() == R.id.landscape) {
      item.setChecked(!item.isChecked());
      current.lockToLandscape(item.isChecked());
      isLockedToLandscape=item.isChecked();
    }

    return(super.onOptionsItemSelected(item));
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode,
                                  Intent data) {
    if (requestCode == CONTENT_REQUEST) {
      if (resultCode == RESULT_OK) {
        // do nothing
      }
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_CAMERA && current != null
        && !current.isSingleShotProcessing()) {
      current.takePicture();

      return(true);
    }

    return(super.onKeyDown(keyCode, event));
  }

  @Override
  public boolean isSingleShotMode() {
    return(singleShot);
  }

  @Override
  public void setSingleShotMode(boolean mode) {
    singleShot=mode;
  }
}
