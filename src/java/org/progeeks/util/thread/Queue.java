/*
 * $Id$
 *
 * Copyright (c) 2001-2003, Paul Speed
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3) Neither the names "Progeeks", "Meta-JB", nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


package org.progeeks.util.thread;

/**
 *  A thread safe FIFO data structure.  Internally this uses an
 *  Object array to store objects.  Objects are enqueued and dequeued
 *  in round-robin fashion.  Once this queue is created then there is
 *  no further allocation done.  Enqueues and dequeues are very efficient
 *  since it only requires two integer modifications and an array referencing.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 *  @since     1.0
 */
public class Queue
{
    public static final int DEFAULT_CAPACITY = 100;

    protected int capacity;
    protected int head;
    protected int tail;
    protected Object[] data;

    /**
     *  Creates a new Queue object with the DEFAULT_CAPACITY.
     */
    public Queue()
    {
        this( DEFAULT_CAPACITY );
    }

    /**
     *  Creates a new Queue object with the specified capacity.
     */
    public Queue( int capacity )
    {
        // We add one so that the queue can actually hold capacity items.
        // We need one extra item to keep the head/tail for empty from looking
        // exactly like the one for full.
        this.capacity = capacity + 1;

        data = new Object[ this.capacity ];

        head = 0;
        tail = 0;
    }

    /**
     *  Adds an object to the end of the queue.  This method will block
     *  if the queue is full.
     */
    public synchronized void enqueue( Object obj )
    {
        // Check for list-full condition
        if( next( tail ) == head )
            {
            do
                {
                try
                    {
                    wait();
                    }
                catch( InterruptedException e )
                    {
                    throw new RuntimeException( "Wait for queue available interrupted.", e );
                    }
                } while( next( tail ) == head );
            }

        data[ tail ] = obj;
        tail = next( tail );

        notifyAll();
    }

    /**
     *  Removes an object from the front of the queue.  This method will
     *  block if the queue is empty.
     */
    public synchronized Object dequeue() throws InterruptedException
    {
        // Check for list-empty condition
        while( tail == head )
            {
            // Queue is empty
            wait();
            }
        Object ret = data[head];
        data[head] = null;
        head = next( head );

        notifyAll();

        return( ret );
    }

    /**
     *  Blocks until the queue is empty.
     */
    public synchronized void waitForEmpty() throws InterruptedException
    {
        while( tail != head )
            {
            // Queue is not empty
            wait();
            }
    }

    /**
     *  Returns the next index based on the passed index, wrapping as
     *  appropriate.  This method is final to give the compiler the best
     *  chance to inline.
     */
    protected final int next( int current )
    {
        return( (current + 1) % capacity );
    }
}
