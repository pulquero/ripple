/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2008 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.string;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.StackContext;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.flow.Sink;
import net.fortytwo.ripple.StringUtils;
import net.fortytwo.ripple.libs.etc.EtcLibrary;

/**
 * A primitive which consumes a string and produces its SHA-1 sum.
 */
public class Sha1 extends PrimitiveStackMapping
{
	private static final int ARITY = 1;

    private static final String[] IDENTIFIERS = {
            StringLibrary.NS_2008_06 + "sha1",
            StringLibrary.NS_2007_08 + "sha1",
            EtcLibrary.NS_2007_05 + "sha1"};

    public String[] getIdentifiers()
    {
        return IDENTIFIERS;
    }

	public Sha1()
		throws RippleException
	{
		super();
	}

	public int arity()
	{
		return ARITY;
	}

	public void applyTo( final StackContext arg,
						 final Sink<StackContext, RippleException> solutions )
		throws RippleException
	{
		RippleList stack = arg.getStack();
		final ModelConnection mc = arg.getModelConnection();

		String a;

		a = mc.toString( stack.getFirst() );
		stack = stack.getRest();

		solutions.put( arg.with(
				stack.push(
			mc.value( StringUtils.sha1SumOf( a ) ) ) ) );
	}
}

