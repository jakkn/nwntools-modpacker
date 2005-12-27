/*
 * $Id$
 *
 * Copyright (c) 2005, Paul Speed
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
 * 3) Neither the names "Progeeks", "NWN Tools", nor the names of its contributors
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

package org.progeeks.nwn.status;

import java.text.*;
import java.util.*;

import org.progeeks.parser.regex.*;

/**
 *  Generates a Date object for a set of parsed date components.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class DateProduction extends NameValueProduction
{
    private static int year = Calendar.getInstance().get( Calendar.YEAR );
    private SimpleDateFormat format = new SimpleDateFormat( "EEE MMM d HH:mm:ss yyyy" );
    private SimpleDateFormat dayFormat = new SimpleDateFormat( "EEE" );

    public Object createProduction( CompositeMatcher m, String name, Object value )
    {
        if( value != null )
            {
            try
                {
                // Add our own year and then check the value to see if the
                // day is right.  If not then subtract one and try again.  A
                // kludge but the logs don't include a year... just a day of
                // the week.
                String time = (String)value;
                String day = time.substring( 0, 3 );
                String test = "";
                int y = year;
                Date result = null;
                while( !test.equals(day) )
                    {
                    result = format.parse( time + " " + y );

                    // Check the format against our original
                    test = dayFormat.format( result );

                    y = y - 1;
                    }
                return( result );
                }
            catch( ParseException e )
                {
                e.printStackTrace();
                }
            }
        return( super.createProduction( m, name, value ) );
    }
}
