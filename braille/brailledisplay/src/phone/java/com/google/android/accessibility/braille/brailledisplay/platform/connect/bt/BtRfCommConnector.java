/*
 * Copyright 2021 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.accessibility.braille.brailledisplay.platform.connect.bt;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import com.google.android.accessibility.braille.brailledisplay.BrailleDisplayLog;
import com.google.android.accessibility.braille.brailledisplay.platform.connect.Connector;
import com.google.android.accessibility.braille.brailledisplay.platform.connect.device.ConnectableBluetoothDevice;
import com.google.android.accessibility.braille.brailledisplay.platform.connect.device.ConnectableDevice;
import com.google.android.accessibility.braille.brailledisplay.platform.lib.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Sets up a Bluetooth rfcomm connection to a remote device that is advertising itself or already
 * bonded.
 */
public class BtRfCommConnector extends Connector {
  private static final String TAG = "BtRfCommConnector";
  private static final int MSG_CONNECT_SUCCESS = 1;
  private static final int MSG_CONNECT_FAILURE = 2;
  private final Handler mainHandler;
  private Connector.Callback callback;
  private BluetoothSocket connectingSocket;
  private boolean hasFailed;

  /** Constructor. Must be called on the main thread. */
  public BtRfCommConnector(ConnectableDevice device, Connector.Callback callback) {
    super(device);
    Utils.assertMainThread();
    this.callback = callback;
    mainHandler = new MainHandler();
  }

  /**
   * Connect to the device. If a failure occurs, {@link Callback#onConnectFailure(ConnectableDevice,
   * boolean, Exception)} will be invoked.
   */
  @Override
  public void connect() {
    BrailleDisplayLog.d(TAG, "createRfcommSocket");
    try {
      connectingSocket =
          BluetoothDevices.createInsecureRfcommSocketToServiceRecord(
              ((ConnectableBluetoothDevice) getDevice()).bluetoothDevice());
    } catch (IOException ioe) {
      BrailleDisplayLog.e(TAG, "createRfcommSocket failed");
      callback.onConnectFailure(getDevice(), ioe);
      return;
    }
    new ConnectThread().start();
  }

  /**
   * Cancel establishing the connection. This will prevent any future callbacks from occurring. Has
   * no effect if this class has already delivered the connection. Shutdown. Stop threads, close
   * sockets.
   */
  @Override
  public void disconnect() {
    mainHandler.removeCallbacksAndMessages(null);
    if (connectingSocket == null) {
      // connectingSocket is null when onConnectFailed.
      return;
    }
    try {
      connectingSocket.close();
    } catch (IOException e) {
      BrailleDisplayLog.e(TAG, "socket closed exception.", e);
    }
  }

  private class MainHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == MSG_CONNECT_SUCCESS) {
        BtConnection connection = (BtConnection) msg.obj;
        if (isShutdown()) {
          // Oops, the client invoked shutdown in the brief interval between the creation
          // of the D2dConnection object and our ability to invoke the onConnectSuccess
          // callback. So we shutdown the connection and do not invoke the callback.
          BrailleDisplayLog.d(TAG, "skip onConnectSuccess due to shutdown");
          connection.shutdown();
        } else {
          BrailleDisplayLog.d(TAG, "invoke onConnectSuccess");
          callback.onConnectSuccess(connection);
        }
      } else if (msg.what == MSG_CONNECT_FAILURE) {
        if (isShutdown()) {
          BrailleDisplayLog.d(TAG, "skip onConnectFailure due to shutdown");
        } else if (hasFailed) {
          BrailleDisplayLog.d(TAG, "skip onConnectFailure due to has failed");
        } else {
          BrailleDisplayLog.d(TAG, "invoke onConnectFailure");
          hasFailed = true;
          callback.onConnectFailure(getDevice(), (Exception) msg.obj);
          callback = null;
        }
      }
    }
  }

  private class ConnectThread extends Thread {
    @Override
    public void run() {
      try {
        BrailleDisplayLog.d(TAG, "Socket.connect()");
        // The following connect() invocation throws an IOException with message
        // "read failed, socket might closed or timeout, read ret: -1" in case one attempts
        // to connect to a device that does not accept rfcomm connection.
        connectingSocket.connect();

        // According to cs, BluetoothSocket.getXxxStream() will never throw, but let's
        // follow the rules and assume it can.
        InputStream inputStream = connectingSocket.getInputStream();
        OutputStream outputStream = connectingSocket.getOutputStream();
        BtConnection btConnection = new BtConnection(getDevice(), inputStream, outputStream);

        // This will usually cause {@link Callback#onConnectSuccess(BtConnection)} to be
        // invoked (on main); however there are edge cases that we must handle carefully:
        //
        // Suppose shutdown() is invoked just before we reach this point but after the
        // connection object has already been created, possibly with object(s) that require
        // closing. Or suppose shutdown() is invoked just after we receive this point but
        // before main has processed the MSG_CONNECT_SUCCESS message.
        //
        // In both of these cases the responsibility to close the connection lies at the
        // point where the main thread processes MSG_CONNECT_SUCCESS. Furthermore we do not
        // invoke onConnectSuccess() in these cases because shutdown has occurred, so we
        // presume that the client is not expecting for onConnectSuccess() to be invoked.
        mainHandler.obtainMessage(MSG_CONNECT_SUCCESS, btConnection).sendToTarget();
      } catch (Exception exception) {
        // This will cause {@link Callback#onConnectFailure(Exception)} to be invoked (on
        // main). Has no effect if shutdown has been invoked, which is a situation we must
        // allow for.
        mainHandler.obtainMessage(MSG_CONNECT_FAILURE, exception).sendToTarget();
      }
    }
  }

  private boolean isShutdown() {
    return callback == null;
  }
}
