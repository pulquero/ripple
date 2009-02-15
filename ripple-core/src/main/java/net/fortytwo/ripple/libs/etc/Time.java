/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2009 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.etc;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.flow.Sink;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.StackContext;
import net.fortytwo.ripple.model.RippleList;

/**
 * A primitive which produces the current time, in seconds since midnight UTC of
 * January 1, 1970.
 */
public class Time extends PrimitiveStackMapping
{
	private static final int ARITY = 0;

    private static final String[] IDENTIFIERS = {
            EtcLibrary.NS_2008_08 + "time",
            EtcLibrary.NS_2007_08 + "time",
            EtcLibrary.NS_2007_05 + "time"};

    public String[] getIdentifiers()
    {
        return IDENTIFIERS;
    }

	public Time() throws RippleException
	{
		super();
	}

	public int arity()
	{
		return ARITY;
	}

	public void apply( final StackContext arg,
						 final Sink<StackContext, RippleException> solutions	)
		    throws RippleException
	{
		final ModelConnection mc = arg.getModelConnection();
		RippleList stack = arg.getStack();

		solutions.put( arg.with( stack.push(
			mc.value( System.currentTimeMillis() ) ) ) );
	}
}
