/**
 * Copyright (C) 2000-2012 Atomikos <info@atomikos.com>
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

package com.atomikos.persistence;

import java.util.Stack;

/**
 * Exception signaling (fatal) errors during logging of recoverable state.
 */

@SuppressWarnings("serial")
public class LogException extends Exception
{

    protected Stack<Exception> errors_ = null;

    public LogException ()
    {
        super ();
    }

    public LogException ( String s )
    {
        super ( s );
    }

 
    public LogException(String msg, Exception e) {
		super(msg,e);
		errors_ = new Stack<Exception>();
		errors_.push(e);
    }
    
    public LogException(Exception e) {
    	super(e);
    }


}
