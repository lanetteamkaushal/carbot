/*
 * Copyright (C) 2009 The Android Open Source Project
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

package net.cardroid.io;

import java.io.IOException;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connection with RFCOMM device. It has a thread that searches for a device
 * and a reader thread that listens for incoming data.
 *
 * Clients should add a {@link ConnectionListener} to check connection
 * status and receive data from the reader thread.
 */
public interface DeviceConnectionService {
    boolean isConnected();

    void addListener(ConnectionListener listener);

    void removeListener(ConnectionListener listener);

    void setDeviceConnector(DeviceConnector deviceConnector);

    void stop();

    void disableConnectionAttempts();

    void enableConnectionAttempts();

    void write(byte[] out);

    DeviceConnector getDeviceConnector();
}
