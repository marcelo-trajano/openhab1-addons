/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.novelanheatpump.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * With the heatpump connector the internal state of a Novelan (Siemens) Heatpump can be read.
 *
 * @author Jan-Philipp Bolle
 * @author John Cocula -- made port configurable
 * @since 1.0.0
 */
public class HeatpumpConnector {

    static final Logger logger = LoggerFactory.getLogger(HeatpumpConnector.class);

    private DataInputStream datain = null;
    private DataOutputStream dataout = null;
    private String serverIp;
    private int serverPort;

    public HeatpumpConnector(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    /**
     * connects to the heatpump via network
     * 
     * @throws UnknownHostException indicate that the IP address of a host could not be determined.
     * @throws IOException indicate that no data can be read from the heatpump
     */
    public void connect() throws UnknownHostException, IOException {
        Socket sock = new Socket(serverIp, serverPort);

        InputStream in = sock.getInputStream();
        OutputStream out = sock.getOutputStream();
        datain = new DataInputStream(in);
        dataout = new DataOutputStream(out);
        logger.debug("Novelan Heatpump connect");
    }

    /**
     * read the parameters of the heatpump
     * 
     * @return
     * @throws IOException
     */
    public int[] getParams() throws IOException {
        int[] heatpump_values = null;
        while (datain.available() > 0) {
            datain.readByte();
        }
        dataout.writeInt(3003);
        dataout.writeInt(0);
        dataout.flush();
        if (datain.readInt() != 3003) {
            return null;
        }
        // int stat = datain.readInt();
        int arraylength = datain.readInt();
        heatpump_values = new int[arraylength];

        for (int i = 0; i < arraylength; i++) {
            heatpump_values[i] = datain.readInt();
        }
        return heatpump_values;
    }

    /**
     * set a parameter of the heatpump
     * 
     * @param param
     * @param value
     * @return
     * @throws IOException
     */
    public boolean setParam(int param, int value) throws IOException {
        while (datain.available() > 0) {
            datain.readByte();
        }
        dataout.writeInt(3002);
        dataout.writeInt(param);
        dataout.writeInt(value);
        dataout.flush();

        int cmd = datain.readInt();
        int resp = datain.readInt();
        if (cmd != 3002) {
            logger.warn("Can't write parameter {} with value {} to heatpump.", param, value);
            return false;
        } else {
            logger.debug("Successful parameter {} with value {} to heatpump written.", param, value);
            return true;
        }

    }

    /**
     * read the internal state of the heatpump
     * 
     * @return a array with all internal data of the heatpump
     * @throws IOException indicate that no data can be read from the heatpump
     */
    public int[] getValues() throws IOException {
        int[] heatpump_values = null;
        while (datain.available() > 0) {
            datain.readByte();
        }
        dataout.writeInt(3004);
        dataout.writeInt(0);
        dataout.flush();
        if (datain.readInt() != 3004) {
            return null;
        }
        int stat = datain.readInt();
        int arraylength = datain.readInt();
        heatpump_values = new int[arraylength];

        for (int i = 0; i < arraylength; i++) {
            heatpump_values[i] = datain.readInt();
        }
        return heatpump_values;
    }

    /**
     * disconnect from heatpump
     */
    public void disconnect() {
        if (datain != null) {
            try {
                datain.close();
            } catch (IOException e) {
                logger.warn("Can't close data input stream", e);
            }
        }

        if (dataout != null) {
            try {
                dataout.close();
            } catch (IOException e) {
                logger.warn("Can't close data output stream", e);
            }
        }
    }
}
