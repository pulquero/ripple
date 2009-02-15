/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2009 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.string;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.StackContext;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.flow.Sink;

/**
 * A primitive which consumes a string and a substring and produces the index of
 * the first occurrence of the substring.
 */
public class IndexOf extends PrimitiveStackMapping
{
	private static final int ARITY = 2;

    private static final String[] IDENTIFIERS = {
            StringLibrary.NS_2008_08 + "indexOf",
            StringLibrary.NS_2007_08 + "indexOf"};

    public String[] getIdentifiers()
    {
        return IDENTIFIERS;
    }

	public IndexOf()
		throws RippleException
	{
		super();
	}

	public int arity()
	{
		return ARITY;
	}

	public void apply( final StackContext arg,
						 final Sink<StackContext, RippleException> solutions )
		throws RippleException
	{
		RippleList stack = arg.getStack();
		final ModelConnection mc = arg.getModelConnection();

		String str, substr;
		int result;

		substr = mc.toString( stack.getFirst() );
		stack = stack.getRest();
		str = mc.toString( stack.getFirst() );
		stack = stack.getRest();

		result = str.indexOf( substr );
		solutions.put( arg.with(
				stack.push( mc.value( result ) ) ) );
	}
}

