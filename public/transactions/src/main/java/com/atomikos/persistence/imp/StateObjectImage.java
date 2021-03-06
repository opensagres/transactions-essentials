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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.atomikos.icatch.DataSerializable;
import com.atomikos.persistence.ObjectImage;
import com.atomikos.persistence.Recoverable;
import com.atomikos.util.ClassLoadingHelper;

/**
 *
 *
 * An object image for reconstruction of staterecoverables through a state
 * recovery mgr.
 */

public class StateObjectImage implements Recoverable, ObjectImage, DataSerializable
{

    // force set serialUID to allow backward log compatibility.
    static final long serialVersionUID = 4440634956991605946L;

    protected ObjectImage img_;

    public StateObjectImage ()
    {
    }

    public StateObjectImage ( ObjectImage image )
    {
        img_ = image;
    }

    public Object getId ()
    {
        return img_.getId ();
    }

    public ObjectImage getObjectImage ()
    {
        return img_;
    }

    public Recoverable restore ()
    {
        return img_.restore ();
    }

    public void readExternal ( ObjectInput in ) throws IOException,
            ClassNotFoundException
    {
        img_ = (ObjectImage) in.readObject ();
    }

    public void writeExternal ( ObjectOutput out ) throws IOException
    {
        out.writeObject ( img_ );
    }

	public void writeData(DataOutput out) throws IOException {
		out.writeUTF(img_.getClass().getName());
		((DataSerializable)img_).writeData(out);
	}

	public void readData(DataInput in) throws IOException {
		img_= (ObjectImage)ClassLoadingHelper.newInstance(in.readUTF());
		((DataSerializable)img_).readData(in);

	}
}
