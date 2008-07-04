/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2008 Joshua Shinavier
 */


package net.fortytwo.ripple.libs.graph;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.libs.etc.EtcLibrary;
import net.fortytwo.ripple.flow.Sink;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.StackContext;
import net.fortytwo.ripple.model.RdfPredicateMapping;
import net.fortytwo.ripple.model.RdfValue;
import net.fortytwo.ripple.model.StackMappingWrapper;
import org.openrdf.model.Resource;


/**
 * A primitive which follows consumes an object and predicate, producing
 * all subjects such that there is a backlink from the object to the subject
 * via the predicate.  Note: the backward traversal of links is much more
 * dependent on the history of query evaluation than forward traversal, which is
 * built into Ripple's query model.
 */
public class InContext extends PrimitiveStackMapping
{
	private static final int ARITY = 2;

    private static final String[] IDENTIFIERS = {
            // FIXME: this is a kludge for programs created by Ripple 0.5-dev.  Remove this alias when it is no longer needed
            GraphLibrary.NS_2007_08 + "inContext",

            GraphLibrary.NS_2008_06 + "inContext"};

    public String[] getIdentifiers()
    {
        return IDENTIFIERS;
    }

	public InContext() throws RippleException
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
		final ModelConnection mc = arg.getModelConnection();
		RippleList stack = arg.getStack();
        RdfValue context = stack.getFirst().toRDF( mc );
        stack = stack.getRest();
        RdfValue pred = stack.getFirst().toRDF( mc );
        stack = stack.getRest();

        // FIXME: bit of a hack
        if ( !( pred.sesameValue() instanceof Resource ) )
        {
            return;
        }

        boolean includeInferred = false;
        RdfPredicateMapping map = new RdfPredicateMapping( pred, includeInferred );
        map.setContext( context );
//System.out.println("created RDF predicate mapping with predicate " + pred + " and context " + context);

        RippleValue result = new StackMappingWrapper( map, mc );
        
        solutions.put( arg.with( stack.push( result ) ) );
	}
}
