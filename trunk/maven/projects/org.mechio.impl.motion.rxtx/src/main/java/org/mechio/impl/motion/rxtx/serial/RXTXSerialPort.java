/*
 * Copyright 2014 the MechIO Project.
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

package org.mechio.impl.motion.rxtx.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import org.jflux.api.common.rk.property.PropertyChangeNotifier;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.mechio.api.motion.servos.utils.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A SerialPort using the RXTX library.
 * This class is not thread safe.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RXTXSerialPort extends PropertyChangeNotifier {
	private static final Logger theLogger = LoggerFactory.getLogger(RXTXSerialPort.class);

	/**
	 * Property string for RXTXSerialPort Errors.
	 */
	public final static String PROP_ERRORS = "Errors";

	/**
	 * Timeout error string.
	 */
	public final static String TIMEOUT_ERROR = "Operation Timed Out";
	/**
	 * Port Not Found error string.
	 */
	public final static String PORT_NOT_FOUND_ERROR = "Port Not Found";
	/**
	 * Port In Use error string.
	 */
	public final static String PORT_IN_USE_ERROR = "Port in Use";
	/**
	 * Invalid Port error string.
	 */
	public final static String INVALID_PORT_ERROR = "Not a Valid Serial Port";
	/**
	 * Port Read Error error string.
	 */
	public final static String READ_ERROR = "Port Read Error";
	/**
	 * Port Write Error error string.
	 */
	public final static String WRITE_ERROR = "Port Write Error";
	/**
	 * Device Error error string.
	 */
	public final static String DEVICE_ERROR = "Device Error";

	private SerialPort myPort;
	private OutputStream myPortWriter;
	private InputStream myPortReader;
	private String myPortName;
	private ConnectionStatus myConnectionStatus;
	private ConnectionStatus myPreviousStatus;
	private List<String> myErrors;
	private int myTimeoutLength;
	private int myBaudRate;
	private int myDataBtis;
	private int myStopBits;
	private int myParity;

	/**
	 * Creates a new RXTXSerialPort using the given port.
	 *
	 * @param portName port identifier
	 */
	public RXTXSerialPort(String portName) {
		myPortName = portName;
		myConnectionStatus = ConnectionStatus.DISCONNECTED;
		myPreviousStatus = ConnectionStatus.DISCONNECTED;
		myTimeoutLength = 100;
		myErrors = new ArrayList();
	}

	/**
	 * Sets the timeout length.
	 *
	 * @param len timeout length in milliseconds
	 */
	public synchronized void setTimeoutLength(int len) {
		myTimeoutLength = len;
		if (ConnectionStatus.DISCONNECTED == myConnectionStatus) {
			return;
		}
		try {
			myPort.enableReceiveTimeout(myTimeoutLength);
		} catch (UnsupportedCommOperationException ex) {
			theLogger.warn("Unable to set port timeout length.", ex);
		}
	}

	/**
	 * Returns port timeout length.
	 *
	 * @return port timeout length
	 */
	public int getTimeoutLength() {
		return myTimeoutLength;
	}

	/**
	 * Returns underlying SerialPort.
	 *
	 * @return underlying SerialPort
	 */
	public SerialPort getPort() {
		return myPort;
	}

	/**
	 * Returns underlying OutputStream.
	 *
	 * @return underlying OutputStream
	 */
	public OutputStream getWriter() {
		return myPortWriter;
	}

	/**
	 * Returns underlying InputStream.
	 *
	 * @return underlying InputStream
	 */
	public InputStream getReader() {
		return myPortReader;
	}

	/**
	 * Connects to the serial port using the given parameters.
	 *
	 * @param baudRate port baud rate
	 * @param dataBtis number of data bits
	 * @param stopBits number of stop bits
	 * @param parity   parity
	 * @return true is successful
	 */
	public synchronized boolean connect(int baudRate, int dataBtis, int stopBits, int parity) {
		if (ConnectionStatus.DISCONNECTED != myConnectionStatus) {
			theLogger.error("Port must be disconnected before connecting.");
			return false;
		}

		CommPortIdentifier portIdentifier;
		CommPort commPort = null;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(myPortName);
			commPort = portIdentifier.open(this.getClass().getName(), 1000);
			if (!(commPort instanceof SerialPort)) {
				addError(INVALID_PORT_ERROR);
				return false;
			}
			myPort = (SerialPort) commPort;
			myPort.setSerialPortParams(baudRate, dataBtis, stopBits, parity);
			myPortReader = myPort.getInputStream();
			myPortWriter = myPort.getOutputStream();
			myConnectionStatus = ConnectionStatus.CONNECTED;
			myPort.enableReceiveTimeout(myTimeoutLength);
			return true;
		} catch (NoSuchPortException ex) {
			addError(PORT_NOT_FOUND_ERROR, ex);
		} catch (PortInUseException ex) {
			addError(PORT_IN_USE_ERROR, ex);
		} catch (UnsupportedCommOperationException ex) {
			addError(DEVICE_ERROR, ex);
		} catch (IOException ex) {
			addError(DEVICE_ERROR, ex);
		} catch (Throwable t) {
			addError(DEVICE_ERROR, t);
		}
		cleanup();
		return false;
	}

	private void cleanup() {
		try {
			if (myPortReader != null) {
				myPortReader.close();
			}
		} catch (Throwable t) {
			//theLogger.log(Level.WARNING, "Error in Cleanup.", t);
		}
		try {
			if (myPortWriter != null) {
				myPortWriter.close();
			}
		} catch (Throwable t) {
			//theLogger.log(Level.WARNING, "Error in Cleanup.", t);
		}
		try {
			if (myPort != null) {
				myPort.close();
			}
		} catch (Throwable t) {
			//theLogger.log(Level.WARNING, "Error in Cleanup.", t);
		}
		myPort = null;
		myPortReader = null;
		myPortWriter = null;
	}

	/**
	 * Disconnect the serial port.
	 *
	 * @return true if successful
	 */
	public synchronized boolean disconnect() {
		if (ConnectionStatus.DISCONNECTED == myConnectionStatus) {
			return true;
		}
		try {
			myPortReader.close();
		} catch (IOException ex) {
			addError(DEVICE_ERROR, "Unable to close Serial Port", ex);
		}
		try {
			myPortWriter.close();
		} catch (IOException ex) {
			addError(DEVICE_ERROR, "Unable to close Serial Port", ex);
		}
		try {
			myPort.close();
		} catch (Throwable ex) {
			addError(DEVICE_ERROR, "Unable to close Serial Port", ex);
		}
		myConnectionStatus = ConnectionStatus.DISCONNECTED;
		cleanup();
		return true;
	}

	public boolean reconnect() {
		clearErrors();
		disconnect();
		return connect(myBaudRate, myDataBtis, myStopBits, myParity);
	}

	/**
	 * Writes the data to the serial port.
	 *
	 * @param data   bytes to write
	 * @param offset data array offset
	 * @param len    data write length
	 * @return true if successful
	 */
	public synchronized boolean write(byte data[], int offset, int len) {
		if (ConnectionStatus.DISCONNECTED == myConnectionStatus) {
			return false;
		}
		try {
			myPortWriter.write(data, offset, len);
			return true;
		} catch (IOException ex) {
			addError(WRITE_ERROR, ex);
		} catch (Throwable t) {
			addError(WRITE_ERROR, t);
		}
		return false;
	}

	/**
	 * Writes the data to the serial port.
	 *
	 * @param data bytes to write
	 * @return true if successful
	 */
	public boolean write(byte... data) {
		return write(data, 0, data.length);
	}

	/**
	 * Flushes the serial port's OutputStream.
	 *
	 * @return true if flush is successful
	 */
	public synchronized boolean flushWriter() {
		if (ConnectionStatus.DISCONNECTED == myConnectionStatus) {
			return false;
		}
		try {
			myPortWriter.flush();
			return true;
		} catch (IOException ex) {
			addError(WRITE_ERROR, ex);
		} catch (Throwable t) {
			addError(WRITE_ERROR, t);
		}
		return false;
	}

	/**
	 * Reads a number of bytes from the serial port.
	 *
	 * @param len number of bytes to read
	 * @return array of bytes read from the serial port
	 */
	public byte[] read(int len) {
		return read(len, myTimeoutLength);
	}

	public byte[] read(int len, int timeout) {
		byte[] data = new byte[len];
		if (read(data, 0, len, timeout) == -1) {
			return null;
		}
		return data;
	}

	/**
	 * Reads a number of bytes from the serial port.
	 *
	 * @param data   array to fill
	 * @param offset data array offset
	 * @param len    number of bytes to read
	 * @return number of bytes read from the serial port
	 */
	public int read(byte data[], int offset, int len) {
		return read(data, offset, len, myTimeoutLength);
	}

	public synchronized int read(byte data[], int offset, int len, int timeout) {
		if (ConnectionStatus.DISCONNECTED == myConnectionStatus) {
			return -1;
		}
		try {
			long start = TimeUtils.now();
			long elapsed = 0;
			int total = 0;
			do {
				int count = myPortReader.read(data, offset, len);
				len -= count;
				total += count;
				offset += count;
				elapsed = TimeUtils.now() - start;
			} while (len > 0 && elapsed < myTimeoutLength);
			if (len > 0) {
				//addError(TIMEOUT_ERROR);
			}
			return total;
		} catch (IOException ex) {
			addError(READ_ERROR, ex);
		} catch (Throwable t) {
			addError(READ_ERROR, t);
		}
		return -1;
	}

	/**
	 * Reads a single byte.
	 *
	 * @return a single unsigned byte.  Returns -1 if no data or an error is encountered
	 */
	public int read() {
		if (ConnectionStatus.DISCONNECTED == myConnectionStatus) {
			return -1;
		}
		try {
			long start = TimeUtils.now();
			long elapsed = 0;
			int b = -1;
			do {
				b = myPortReader.read();
				elapsed = TimeUtils.now() - start;
			} while (b == -1 && elapsed < myTimeoutLength);
			return b;
		} catch (IOException ex) {
			addError(READ_ERROR, ex);
		} catch (Throwable t) {
			addError(READ_ERROR, t);
		}
		return -1;
	}

	/**
	 * Clears any available data from the port reader.
	 */
	public synchronized void clearReader() {
		if (ConnectionStatus.DISCONNECTED == myConnectionStatus) {
			return;
		}
		try {
			int n = myPortReader.available();
			myPortReader.skip(n);
		} catch (IOException ex) {
			addError(READ_ERROR, ex);
		} catch (Throwable t) {
			addError(READ_ERROR, t);
		}
	}

	/**
	 * Returns the port's ConnectionStatus.
	 *
	 * @return port's ConnectionStatus
	 */
	public ConnectionStatus getConnectionStatus() {
		return myConnectionStatus;
	}

	/**
	 * Adds an error to the RXTXSerialPort.
	 *
	 * @param error error string to add
	 */
	protected void addError(String error) {
		addError(error, null, null);
	}

	/**
	 * Adds an error to the RXTXSerialPort.
	 *
	 * @param error   error string to add
	 * @param message error message
	 */
	protected void addError(String error, String message) {
		addError(error, message, null);
	}

	/**
	 * Adds an error to the RXTXSerialPort.
	 *
	 * @param error error string to add
	 * @param t     error Throwable
	 */
	protected void addError(String error, Throwable t) {
		addError(error, null, t);
	}

	/**
	 * Adds an error to the RXTXSerialPort.
	 *
	 * @param error   error string to add
	 * @param message error message
	 * @param t       error Throwable
	 */
	public void addError(String error, String message, Throwable t) {
		myErrors.add(error);
		if (ConnectionStatus.CONNECTION_ERROR != myConnectionStatus) {
			myPreviousStatus = myConnectionStatus;
		}
		myConnectionStatus = ConnectionStatus.CONNECTION_ERROR;
		if (t == null && message == null) {
			theLogger.error("Serial Port Error on port: {}.  Error: {}.", myPortName, error);
		} else if (message == null) {
			theLogger.error("Serial Port Error on port: {}.  Error: {}.", myPortName, error, t);
		} else if (t == null) {
			theLogger.error("Serial Port Error on port: {}.  Error: {} ({}).",
					myPortName, error, message);
		} else {
			theLogger.error("Serial Port Error on port: {}.  Error: {} ({}).",
					myPortName, error, message, t);
		}
		firePropertyChange(PROP_ERRORS, null, myErrors);
	}

	/**
	 * Returns a List of the port's errors.
	 *
	 * @return List of the port's errors
	 */
	public List<String> getErrors() {
		return myErrors;
	}

	/**
	 * Clears all port errors.
	 */
	public void clearErrors() {
		myErrors.clear();
		if (ConnectionStatus.CONNECTION_ERROR == myConnectionStatus) {
			myConnectionStatus = myPreviousStatus;
		}
		firePropertyChange(PROP_ERRORS, null, myErrors);
	}

	/**
	 * Returns true if the port has errors.
	 *
	 * @return true if the port has errors
	 */
	public boolean hasErrors() {
		return !myErrors.isEmpty();
	}
}
