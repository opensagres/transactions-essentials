/**
 * Copyright (C) 2000-2010 Atomikos <info@atomikos.com>
 *
 * This code ("Atomikos TransactionsEssentials"), by itself,
 * is being distributed under the
 * Apache License, Version 2.0 ("License"), a copy of which may be found at
 * http://www.atomikos.com/licenses/apache-license-2.0.txt .
 * You may not use this file except in compliance with the License.
 *
 * While the License grants certain patent license rights,
 * those patent license rights only extend to the use of
 * Atomikos TransactionsEssentials by itself.
 *
 * This code (Atomikos TransactionsEssentials) contains certain interfaces
 * in package (namespace) com.atomikos.icatch
 * (including com.atomikos.icatch.Participant) which, if implemented, may
 * infringe one or more patents held by Atomikos.
 * It should be appreciated that you may NOT implement such interfaces;
 * licensing to implement these interfaces must be obtained separately from Atomikos.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.atomikos.persistence.imp;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;
import java.util.Enumeration;
import java.util.Vector;

import com.atomikos.logging.Logger;
import com.atomikos.logging.LoggerFactory;
import com.atomikos.persistence.LogException;
import com.atomikos.persistence.LogStream;
import com.atomikos.persistence.Recoverable;

/**
 * A file implementation of a LogStream.
 */

public class FileLogStream extends AbstractLogStream implements LogStream {
	private static final Logger LOGGER = LoggerFactory.createLogger(FileLogStream.class);

	// keeps track of the latest output stream returned
	// from writeCheckpoint, so that it can be closed ( invalidated )
	// if necessary.

	private ObjectOutputStream ooutput_;

	private boolean corrupt_;

	// true if error on checkpoint; second call of recover
	// not allowed, otherwise suffix_ will be wrong
	// especially since checkpoint failed.

	public FileLogStream(String baseDir, String baseName) throws IOException {
		super(baseDir, baseName);
	}

	void markAsCorrupt() {
		corrupt_ = true;
	}

	public synchronized Vector<Recoverable> recover() throws LogException {

		if (corrupt_)
			throw new LogException("Instance might be corrupted");

		Vector<Recoverable> ret = new Vector<Recoverable>();
		InputStream in = null;

		try {
			FileInputStream f = file_.openLastValidVersionForReading();

			in = f;

			ObjectInputStream ins = new ObjectInputStream(in);
			int count = 0;
			if (LOGGER.isInfoEnabled()) {
				LOGGER.logInfo("Starting read of logfile " + file_.getCurrentVersionFileName());
			}
			while (in.available() > 0) {
				// if crashed, then unproper closing might cause endless blocking!
				// therefore, we check if avaible first.
				count++;
				Recoverable nxt = (Recoverable) ins.readObject();
				ret.addElement(nxt);
				if (count % 10 == 0) {
					LOGGER.logInfo(".");
				}

			}
			LOGGER.logInfo("Done read of logfile");

		} catch (java.io.EOFException unexpectedEOF) {
			LOGGER.logDebug("Unexpected EOF - logfile not closed properly last time?", unexpectedEOF);
			// merely return what was read so far...
		} catch (StreamCorruptedException unexpectedEOF) {
			LOGGER.logDebug("Unexpected EOF - logfile not closed properly last time?", unexpectedEOF);
			// merely return what was read so far...
		} catch (ObjectStreamException unexpectedEOF) {
			LOGGER.logDebug("Unexpected EOF - logfile not closed properly last time?", unexpectedEOF);
			// merely return what was read so far...
		} catch (FileNotFoundException firstStart) {
			// the file could not be opened for reading;
			// merely return the default empty vector
		} catch (Exception e) {
			String msg = "Error in recover";
			LOGGER.logWarning(msg, e);
			throw new LogException(msg, e);
		} finally {
			try {
				if (in != null)
					in.close();

			} catch (IOException io) {
				throw new LogException("Error in recover", io);
			}
		}

		return ret;
	}

	public synchronized void writeCheckpoint(Enumeration elements) throws LogException {

		// first, make sure that any pending output stream handles
		// in the client are invalidated
		closeOutput();

		try {
			// open the new output file
			// NOTE: after restart, any previous and failed checkpoint files
			// will be overwritten here. That is perfectly OK.
			output_ = file_.openNewVersionForWriting();
			ooutput_ = new ObjectOutputStream(new BufferedOutputStream(output_,4096));
			while (elements != null && elements.hasMoreElements()) {
				Object next = elements.nextElement();
				ooutput_.writeObject(next);
			}
			ooutput_.flush();
			output_.flush();
			output_.getFD().sync();
			// NOTE: we do NOT close the object output, since the client
			// will probably want to write more!
			// Thus, we return the open stream to the client.
			// Any closing will be done later, during cleanup if necessary.

			if (corrupt_) {
				throw new LogException("Instance corrupted");
			}

			try {
				file_.discardBackupVersion();
			} catch (IOException errorOnDelete) {
				markAsCorrupt();
				// should restart
				throw new LogException("Old file could not be deleted");
			}
		} catch (Exception e) {
			throw new LogException("Error during checkpointing", e);
		}

	}

	public synchronized void flushObject(Object o, boolean shouldSync) throws LogException {
		if (ooutput_ == null)
			throw new LogException("Not Initialized or already closed");
		try {
			ooutput_.writeObject(o);
			output_.flush();
			ooutput_.flush();
			if (shouldSync)
				output_.getFD().sync();
		} catch (IOException e) {
			throw new LogException(e.getMessage(), e);
		}
	}

}
