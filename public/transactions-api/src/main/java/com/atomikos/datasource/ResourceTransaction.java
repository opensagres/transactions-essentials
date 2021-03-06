/**
 * Copyright (C) 2000-2011 Atomikos <info@atomikos.com>
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

package com.atomikos.datasource;

import com.atomikos.icatch.HeuristicMessage;

/**
 * The notion of a local transaction executed on a resource.
 * Serves as a handle towards the transaction management module.
 */
 
public interface ResourceTransaction 
{

  
    /**
     * Adds heuristic resolution information.
     * @param mesg The heuristic message.
     * @exception IllegalStateException If no longer active.
     */

    public void addHeuristicMessage(HeuristicMessage mesg)
        throws IllegalStateException;
    
   
    /**
     *
     * @return HeuristicMessage[] An array of messages, or null if none.
     */

    public HeuristicMessage[] getHeuristicMessages();


    /**
     * Suspends the work, so that underlying resources can
     * be used for a next (sibling) invocation.
     *
     */

    public void suspend() throws IllegalStateException,ResourceException;

    /**
     * Resumes a previously suspended tx.
     *
     */

    public void resume() throws IllegalStateException,ResourceException;
       
}
