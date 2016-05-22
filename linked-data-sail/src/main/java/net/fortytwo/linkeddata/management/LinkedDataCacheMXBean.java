package net.fortytwo.linkeddata.management;

import net.fortytwo.ripple.RippleException;

public interface LinkedDataCacheMXBean {
    boolean isDereferenceSubjectsEnabled();
    void setDereferenceSubjectsEnabled(boolean flag);
    boolean isDereferencePredicatesEnabled();
    void setDereferencePredicatesEnabled(boolean flag);
    boolean isDereferenceObjectsEnabled();
    void setDereferenceObjectsEnabled(boolean flag);
    boolean isDereferenceContextsEnabled();
    void setDereferenceContextsEnabled(boolean flag);

    void clear() throws RippleException;
}
