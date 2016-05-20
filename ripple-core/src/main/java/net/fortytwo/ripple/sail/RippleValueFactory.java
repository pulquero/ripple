package net.fortytwo.ripple.sail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.sail.SailException;

import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.StringUtils;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RippleValueFactory implements ValueFactory {
    public static final String STRING_NAMESPACE = "urn:string:";

    private final ValueFactory base;

    public RippleValueFactory(final ValueFactory base) {
        this.base = base;
    }

    public Value nativize(final Value other) throws SailException {
        if (other instanceof IRI) {
            String s = other.stringValue();
            if (s.startsWith(STRING_NAMESPACE)) {
                try {
                    return createLiteral(StringUtils.percentDecode(s.substring(STRING_NAMESPACE.length())));
                } catch (RippleException e) {
                    throw new SailException(e);
                }
            } else {
                return createIRI(other.stringValue());
            }
        } else if (other instanceof Literal) {
            Literal l = (Literal) other;
            if (null != l.getDatatype()) {
                return createLiteral(l.getLabel(), l.getDatatype());
            } else if (null != l.getLanguage()) {
                return createLiteral(l.getLabel(), l.getLanguage());
            } else {
                return createLiteral(l.getLabel());
            }
        } else if (other instanceof BNode) {
            return createBNode(((BNode) other).getID());
        } else {
            throw new IllegalStateException("value of unexpected class: " + other);
        }
    }

    @Override
    public IRI createIRI(String s) {
        return new RippleIRI(s);
    }

    @Override
    public IRI createIRI(String s, String s1) {
        return new RippleIRI(s + s1);
    }

    @Override
    public BNode createBNode() {
        return new RippleBNode();
    }

    @Override
    public BNode createBNode(String s) {
        return new RippleBNode(s);
    }

    @Override
    public Literal createLiteral(String s) {
        return new RippleLiteral(s);
    }

    @Override
    public Literal createLiteral(String s, String s1) {
        return new RippleLiteral(s, s1);
    }

    @Override
    public Literal createLiteral(String s, IRI uri) {
        return new RippleLiteral(s, uri);
    }

    @Override
    public Literal createLiteral(boolean b) {
        Literal other = base.createLiteral(b);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
    }

    @Override
    public Literal createLiteral(byte b) {
        Literal other = base.createLiteral(b);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
    }

    @Override
    public Literal createLiteral(short i) {
        Literal other = base.createLiteral(i);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
    }

    @Override
    public Literal createLiteral(int i) {
        Literal other = base.createLiteral(i);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
    }

    @Override
    public Literal createLiteral(long l) {
        Literal other = base.createLiteral(l);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
    }

    @Override
    public Literal createLiteral(float v) {
        Literal other = base.createLiteral(v);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
    }

    @Override
    public Literal createLiteral(double v) {
        Literal other = base.createLiteral(v);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
    }

    @Override
    public Literal createLiteral(XMLGregorianCalendar xmlGregorianCalendar) {
        Literal other = base.createLiteral(xmlGregorianCalendar);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
    }

    @Override
    public Literal createLiteral(Date date) {
        Literal other = base.createLiteral(date);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
    }

    @Override
    public Statement createStatement(Resource resource, IRI uri, Value value) {
        // Note: it is assumed that the argument values were also produced by this ValueFactory.
        return base.createStatement(resource, uri, value);
    }

    @Override
    public Statement createStatement(Resource resource, IRI uri, Value value, Resource resource1) {
        return base.createStatement(resource, uri, value, resource1);
    }

	@Override
	public URI createURI(String uri) {
		return createIRI(uri);
	}

	@Override
	public URI createURI(String namespace, String localName) {
		return createIRI(namespace, localName);
	}

	@Override
	public Literal createLiteral(String label, URI datatype) {
		return createLiteral(label, (IRI) datatype);
	}

	@Override
	public Literal createLiteral(BigDecimal bigDecimal) {
        Literal other = base.createLiteral(bigDecimal);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
	}

	@Override
	public Literal createLiteral(BigInteger bigInteger) {
        Literal other = base.createLiteral(bigInteger);
        return new RippleLiteral(other.getLabel(), other.getDatatype());
	}

	@Override
	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return base.createStatement(subject, predicate, object);
	}

	@Override
	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		return base.createStatement(subject, predicate, object);
	}
}
