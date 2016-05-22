package net.fortytwo.linkeddata.management;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableWeakReference;

import net.fortytwo.linkeddata.LinkedDataCache;

public class ManagedLinkedDataCache extends StandardMBean implements LinkedDataCacheMXBean {
	private static final FinalizableReferenceQueue REF_QUEUE = new FinalizableReferenceQueue();

	public static Object createMXBean(LinkedDataCache cache) {
		return new ManagedLinkedDataCache(cache);
	}

	private FinalizableWeakReference<LinkedDataCache> cacheRef;
	private MBeanServer mbs;
	private ObjectName objectName;

	private ManagedLinkedDataCache(LinkedDataCache cache) {
		super(LinkedDataCacheMXBean.class, true);
		this.cacheRef = new FinalizableWeakReference<LinkedDataCache>(cache, REF_QUEUE)
		{
			@Override
			public void finalizeReferent() {
				try {
					mbs.unregisterMBean(objectName);
				} catch (JMException e) {
				}
				cacheRef = null;
				mbs = null;
			}
		};
	}

	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		objectName = super.preRegister(server, name);
		this.mbs = server;
		return objectName;
	}

	private void checkCachePresent() {
		if(cacheRef.get() == null) {
			throw new IllegalStateException("LinkedDataCache has already been gc-ed");
		}
	}

	@Override
	public boolean isDereferenceSubjectsEnabled() {
		checkCachePresent();
		return cacheRef.get().getDereferenceSubjects();
	}

	@Override
	public void setDereferenceSubjectsEnabled(boolean flag) {
		checkCachePresent();
		cacheRef.get().setDereferenceSubjects(flag);
	}

	@Override
	public boolean isDereferencePredicatesEnabled() {
		checkCachePresent();
		return cacheRef.get().getDereferencePredicates();
	}

	@Override
	public void setDereferencePredicatesEnabled(boolean flag) {
		checkCachePresent();
		cacheRef.get().setDereferencePredicates(flag);
	}

	@Override
	public boolean isDereferenceObjectsEnabled() {
		checkCachePresent();
		return cacheRef.get().getDereferenceObjects();
	}

	@Override
	public void setDereferenceObjectsEnabled(boolean flag) {
		checkCachePresent();
		cacheRef.get().setDereferenceObjects(flag);
	}

	@Override
	public boolean isDereferenceContextsEnabled() {
		checkCachePresent();
		return cacheRef.get().getDereferenceContexts();
	}

	@Override
	public void setDereferenceContextsEnabled(boolean flag) {
		checkCachePresent();
		cacheRef.get().setDereferenceContexts(flag);
	}

	public void clear() {
		checkCachePresent();
		clear();
	}
}
