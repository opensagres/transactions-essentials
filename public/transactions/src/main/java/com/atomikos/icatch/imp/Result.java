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

package com.atomikos.icatch.imp;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import com.atomikos.icatch.HeuristicMessage;
import com.atomikos.icatch.Participant;

/**
 * A Result is responsible for collecting the replies of a termination round.
 */

abstract class Result
{

    public static final int ALL_OK = 0 , HEUR_HAZARD = 1 , HEUR_MIXED = 2 ,
            HEUR_ROLLBACK = 3 , HEUR_COMMIT = 4 , ALL_READONLY = 5 ,
            ROLLBACK = 6;

    protected int result_ = -1;
    // should be set by analyze()

    protected int numberOfMissingReplies_ = 0;
    protected Stack<Reply> replies_ = new Stack<Reply>();
    protected Hashtable<Participant,Object> repliedlist_ = new Hashtable<Participant,Object>();
    protected Vector<HeuristicMessage> heuristicMessagesOfNormalParticipants_ = new Vector<HeuristicMessage>();
    protected Vector<HeuristicMessage> heuristicMessagesOfHeuristicParticipants_ = new Vector<HeuristicMessage>();

    public Result ( int numberOfRepliesToWaitFor )
    {
        numberOfMissingReplies_ = numberOfRepliesToWaitFor;
    }

    /**
     * Get the overall result for this communication round.
     *
     * @return int One of the static codes.
     *
     * @exception IllegalStateException
     *                If active msgs exist.
     * @exception InterruptedException
     *                If interrupted during wait.
     */

    public int getResult() throws IllegalStateException, InterruptedException
    {
        calculateResultFromAllReplies();
        return result_;
    }

    /**
     * Add any heuristic messages from this message round.
     *
     * Needed in implementation of analyze().
     */

    protected synchronized void addMessages ( HeuristicMessage[] marr )
    {
        if ( marr == null ) return;
        
        for ( int i = 0; i < marr.length; i++ )
            heuristicMessagesOfNormalParticipants_.addElement ( marr[i] );
    }

    /**
     * Add any heuristic messages from this message round. This method serves
     * for adding the messages of participants that decided ON THEIR OWN to
     * terminate heuristically.
     *
     * Needed in implementation of analyze().
     */

    protected synchronized void addErrorMessages ( HeuristicMessage[] marr )
    {
        if ( marr == null ) return;
        
        for ( int i = 0; i < marr.length; i++ )
            heuristicMessagesOfHeuristicParticipants_.addElement ( marr[i] );
    }

    /**
     * Abstract method: analyze the results for this message round.
     *
     * @exception IllegalStateException
     *                If not done yet.
     * @exception InterruptedException
     *                If interruption during result wait.
     */

    protected abstract void calculateResultFromAllReplies() throws IllegalStateException,
            InterruptedException;

    /**
     * Get the heuristic info for the message round. This returns all heuristic
     * messages for participants that did NOT heuristically terminate on their
     * own.
     *
     * @return HeuristicMessages[] The heuristic messages, or null if none.
     * @exception IllegalStateException
     *                If not done yet.
     * @exception InterruptedException
     *                If interrupted during wait for results.
     */

    public HeuristicMessage[] getMessages () throws IllegalStateException,
            InterruptedException
    {
        calculateResultFromAllReplies();

        if ( heuristicMessagesOfNormalParticipants_.isEmpty () ) return null;

        Object[] oarr = heuristicMessagesOfNormalParticipants_.toArray ();
        HeuristicMessage[] ret = new HeuristicMessage[oarr.length];
        for ( int i = 0; i < ret.length; i++ )
            ret[i] = (HeuristicMessage) oarr[i];

        return ret;
    }

    /**
     * Get the heuristic error info for the message round. These are for
     * participants that actually did terminate heuristically on their own.
     *
     * @return HeuristicMessages[] The heuristic messages, or null if none.
     * @exception IllegalStateException
     *                If not done yet.
     * @exception InterruptedException
     *                If interrupted during wait.
     */

    public HeuristicMessage[] getErrorMessages () throws IllegalStateException,
            InterruptedException
    {
        calculateResultFromAllReplies();

        if ( heuristicMessagesOfHeuristicParticipants_.isEmpty () )
            return null;

        Object[] oarr = heuristicMessagesOfHeuristicParticipants_.toArray ();
        HeuristicMessage[] ret = new HeuristicMessage[oarr.length];
        for ( int i = 0; i < ret.length; i++ )
            ret[i] = (HeuristicMessage) oarr[i];

        return ret;
    }
    
    private boolean ignoreReply ( Reply reply ) {
    	// retried messages are not counted in result
        // and duplicate entries per participant neither
        // otherwise duplicates arise if a participant sends replay
    	return reply.isRetried() || repliedlist_.containsKey(reply.getParticipant());
    }

    /**
     * Add a reply to the result.
     *
     * @param reply
     *            The reply to add.
     */

    public synchronized void addReply(Reply reply)
    {
        if ( !ignoreReply(reply) ) {
        	repliedlist_.put(reply.getParticipant(),new Object());
        	replies_.push(reply);
        	numberOfMissingReplies_--;
        	notifyAll();
        }
    }

    /**
     * Get all replies for this result's message round. Block until ready.
     *
     * @return Stack All replies in a stack.
     * @exception IllegalStateException
     *                If not all replies are in yet.
     * @exception InterruptedException
     *                During waiting interrupt.
     */

    public Stack<Reply> getReplies() throws IllegalStateException,
            InterruptedException
    {
        waitForReplies();
        return replies_;
    }

    /**
     * Wait until all replies arrived.
     *
     * @exception InterruptedException
     *                If the wait is interrupted.
     */

    synchronized void waitForReplies() throws InterruptedException
    {
        while ( numberOfMissingReplies_ > 0 ) wait();
    }

}
