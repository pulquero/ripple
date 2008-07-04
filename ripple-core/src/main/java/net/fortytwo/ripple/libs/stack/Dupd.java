/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2008 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.stack;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.flow.Sink;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.StackContext;
import net.fortytwo.ripple.model.RippleList;

/**
 * A primitive which pushes a copy of the second-to-topmost item on the stack to
 * the head of the stack.
 */
public class Dupd extends PrimitiveStackMapping
{
	private static final int ARITY = 2;

    private static final String[] IDENTIFIERS = {
            StackLibrary.NS_2008_06 + "dupd",
            StackLibrary.NS_2007_08 + "dupd",
            StackLibrary.NS_2007_05 + "dupd"};

    public String[] getIdentifiers()
    {
        return IDENTIFIERS;
    }

	public Dupd()
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
		RippleValue y, x;
		RippleList stack = arg.getStack();

		y = stack.getFirst();
		stack = stack.getRest();
		x = stack.getFirst();
		stack = stack.getRest();

		solutions.put( arg.with(
				stack.push( x ).push( x ).push( y ) ) );
	}
}

