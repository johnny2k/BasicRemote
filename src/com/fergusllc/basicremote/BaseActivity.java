/*
 * Copyright (C) 2009 Google Inc.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fergusllc.basicremote;

import com.fergusllc.basicremote.ConnectionManager.ConnectionListener;
import com.fergusllc.basicremote.protocol.ICommandSender;
import com.fergusllc.basicremote.protocol.QueuingSender;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

/**
 * Base for most activities in the app.
 * <p>
 * Automatically connects to the background service on startup.
 *
 */
public class BaseActivity extends CoreServiceActivity
    implements ConnectionListener {



  /**
   * Request code used by this activity.
   */
  private static final int CODE_SWITCH_BOX = 1;

  /**
   * Request code used by this activity for pairing requests.
   */
  private static final int CODE_PAIRING = 2;

  private static final long MIN_TOAST_PERIOD = TimeUnit.SECONDS.toMillis(3);

  /**
   * User codes defined in activities extending this one should start above
   * this value.
   */
  public static final int FIRST_USER_CODE = 100;

  private final QueuingSender commands;

  private boolean isConnected;

  private boolean isKeepingConnection;
  
  /**
   * Constructor.
   */
  BaseActivity() {

    commands = new QueuingSender(new MissingSenderToaster());
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onStart() {
    super.onStart();

    setKeepConnected(true);
  }

  @Override
  protected void onStop() {
    setKeepConnected(false);
    super.onStop();
  }

  @Override
  protected void onResume() {
    super.onResume();
    connect();

  }

  @Override
  protected void onPause() {
    disconnect();
    super.onPause();
  }


  @Override
  protected void onActivityResult(final int requestCode, final int resultCode,
      final Intent data)  {
    executeWhenCoreServiceAvailable(new Runnable() {
      public void run() {
        if (requestCode == CODE_SWITCH_BOX) {
          if (resultCode == RESULT_OK && data != null) {
            RemoteDevice remoteDevice =
                data.getParcelableExtra(DeviceFinder.EXTRA_REMOTE_DEVICE);
            if (remoteDevice != null) {
              getConnectionManager().setTarget(remoteDevice);
            }
          }
          getConnectionManager().deviceFinderFinished();
          connectOrFinish();
        }
        else if (requestCode == CODE_PAIRING) {
          getConnectionManager().pairingFinished();
          handlePairingResult(resultCode);
        }
      }
    });
  }

  private void showMessage(int resId) {
    Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
  }

  private void handlePairingResult(int resultCode) {
    switch (resultCode) {
      case PairingActivity.RESULT_OK:
        showMessage(R.string.pairing_succeeded_toast);
        connect();
        break;
      case PairingActivity.RESULT_CANCELED:
        getConnectionManager().requestDeviceFinder();
        break;
      case PairingActivity.RESULT_CONNECTION_FAILED:
      case PairingActivity.RESULT_PAIRING_FAILED:
        showMessage(R.string.pairing_failed_toast);
        getConnectionManager().requestDeviceFinder();
        break;
      default:
        throw new IllegalStateException("Unsupported pairing activity result: "
            + resultCode);
    }
  }

  /**
   * Returns the interface to send commands to the remote box.
   */
  protected final ICommandSender getCommands() {
    return commands;
  }

  private void connect() {
    if (!isConnected) {
      isConnected = true;

      executeWhenCoreServiceAvailable(new Runnable() {
        public void run() {
          getConnectionManager().connect(BaseActivity.this);
        }
      });
    }
  }

  private void disconnect() {
    if (isConnected) {
      commands.setSender(null);
      isConnected = false;
      executeWhenCoreServiceAvailable(new Runnable() {
        public void run() {
          getConnectionManager().disconnect(BaseActivity.this);
        }
      });
    }
  }

  private void setKeepConnected(final boolean keepConnected) {
    if (isKeepingConnection != keepConnected) {
      isKeepingConnection = keepConnected;
      executeWhenCoreServiceAvailable(new Runnable() {
        public void run() {
          logConnectionStatus("Keep Connected: " + keepConnected);
          getConnectionManager().setKeepConnected(keepConnected);
        }
      });
    }
  }

  /**
   * Starts the box selection dialog.
   */
  private final void showSwitchBoxActivity() {
    disconnect();


    startActivityForResult(
        DeviceFinder.createConnectIntent(this, getConnectionManager().getTarget(),
            getConnectionManager().getRecentlyConnected()), CODE_SWITCH_BOX);

  }

  /**
   * If connection failed due to SSL handshake failure, this method will be
   * invoked to start the pairing session with device, and establish secure
   * connection.
   * <p>
   * When pairing finishes, PairingListener's method will be called to
   * differentiate the result.
   */
  private final void showPairingActivity(RemoteDevice target) {
    disconnect();
    if (target != null) {
      startActivityForResult(
          PairingActivity.createIntent(this, new RemoteDevice(
              target.getName(), target.getAddress(), target.getPort() + 1)),
          CODE_PAIRING);
    }
  }

  public void onConnecting() {
    commands.setSender(null);
    logConnectionStatus("Connecting");
  }

  public void onShowDeviceFinder() {
    commands.setSender(null);
    logConnectionStatus("Show device finder");

    showSwitchBoxActivity();
  }

  public void onConnectionSuccessful(ICommandSender sender) {
    logConnectionStatus("Connected");
    commands.setSender(sender);
  }

  public void onNeedsPairing(RemoteDevice remoteDevice) {
    logConnectionStatus("Pairing");
    showPairingActivity(remoteDevice);
  }

  public void onDisconnected() {
    commands.setSender(null);
    logConnectionStatus("Disconnected");
  }

  private class MissingSenderToaster
      implements QueuingSender.MissingSenderListener {
    private long lastToastTime;

    public void onMissingSender() {
      if (System.currentTimeMillis() - lastToastTime > MIN_TOAST_PERIOD) {
        lastToastTime = System.currentTimeMillis();
        showMessage(R.string.sender_missing);
      }
    }
  }

  private void logConnectionStatus(CharSequence sequence) {
	    String message = String.format("%s (%s)", sequence,
	      getClass().getSimpleName());
	    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	  }

  private void connectOrFinish() {
    if (getConnectionManager() != null) {
      if (getConnectionManager().getTarget() != null) {
        connect();
      } else {
        finish();
      }
    }
  }

  @Override
  protected void onServiceAvailable(CoreService coreService) {
  }

  @Override
  protected void onServiceDisconnecting(CoreService coreService) {
    disconnect();
    setKeepConnected(false);
  }

}
