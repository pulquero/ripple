package net.fortytwo.linkeddata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fortytwo.flow.rdf.RDFBuffer;
import net.fortytwo.flow.rdf.RDFSink;
import net.fortytwo.flow.rdf.SailInserter;
import net.fortytwo.flow.rdf.SesameInputAdapter;
import net.fortytwo.flow.rdf.SesameOutputAdapter;
import net.fortytwo.flow.rdf.SingleContextPipe;
import net.fortytwo.linkeddata.dereferencers.FileURIDereferencer;
import net.fortytwo.linkeddata.dereferencers.HTTPURIDereferencer;
import net.fortytwo.linkeddata.dereferencers.JarURIDereferencer;
import net.fortytwo.linkeddata.rdfizers.ImageRdfizer;
import net.fortytwo.linkeddata.rdfizers.VerbatimRdfizer;
import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.StringUtils;
import net.fortytwo.ripple.URIMap;

/**
 * A manager for a dynamic set of RDF graphs collected from the Web.
 * The cache uses configurable URI dereferencers and RDFizers to fetch and translate documents,
 * and connects to an RDF triple store which provides a unified view of the Web of Data.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LinkedDataCache {
    private static final Logger logger = LoggerFactory.getLogger(LinkedDataCache.class);

    public static final String
            CACHE_NS = "http://fortytwo.net/2012/02/linkeddata#";
    public static final IRI
            CACHE_MEMO = SimpleValueFactory.getInstance().createIRI(CACHE_NS + "memo"),
            CACHE_REDIRECTSTO = SimpleValueFactory.getInstance().createIRI(CACHE_NS + "redirectsTo"),
            CACHE_GRAPH = SimpleValueFactory.getInstance().createIRI("http://fortytwo.net/2012/02/linkeddata/cache");  // the default context is used for caching metadata

    private static final String[] NON_RDF_EXTENSIONS = {
            "123", "3dm", "3dmf", "3gp", "8bi", "aac", "ai", "aif", "app", "asf",
            "asp", "asx", "avi", "bat", "bin", "bmp", "c", "cab", "cfg", "cgi",
            "com", "cpl", "cpp", "css", "csv", "dat", "db", "dll", "dmg", "dmp",
            "doc", "drv", "drw", "dxf", "eps", "exe", "fnt", "fon", "gif", "gz",
            "h", "hqx", /*"htm", "html",*/ "iff", "indd", "ini", "iso", "java", /*"jpeg",*/
            /*"jpg",*/ "js", "jsp", "key", "log", "m3u", "mdb", "mid", "midi", "mim",
            "mng", "mov", "mp3", "mp4", "mpa", "mpg", "msg", "msi", "otf", "pct",
            "pdf", "php", "pif", "pkg", "pl", "plugin", "png", "pps", "ppt", "ps",
            "psd", "psp", "qt", "qxd", "qxp", "ra", "ram", "rar", "reg", "rm",
            "rtf", "sea", "sit", "sitx", "sql", "svg", "swf", "sys", "tar", "tif",
            "ttf", "uue", "vb", "vcd", "wav", "wks", "wma", "wmv", "wpd", "wps",
            "ws", /*"xhtml",*/ "xll", "xls", "yps", "zip"};

    private final int MINIMUM_CAPACITY = 100;

    private CachingMetadata metadata;
    private ValueFactory valueFactory;
    private boolean useBlankNodes;

    private URIMap uriMap;
    private boolean autoCommit = true;

    private boolean derefSubjects = true;
    private boolean derefPredicates = false;
    private boolean derefObjects = true;
    private boolean derefContexts = false;

    private String acceptHeader = null;

    private CacheExpirationPolicy expirationPolicy;

    // Maps media types to Rdfizers
    private final Map<MediaType, MediaTypeInfo> rdfizers
            = new HashMap<MediaType, MediaTypeInfo>();

    // Maps URI schemes to Dereferencers
    private final Map<String, Dereferencer> dereferencers = new HashMap<String, Dereferencer>();

    private DataStore dataStore;

    // single connection shared among all accessing threads
    private SailConnection sailConnection;

    /**
     * Constructs a cache with the default settings, dereferencers, and rdfizers.
     *
     * @param sail the underlying triple store for the cache
     * @return the default cache
     * @throws RippleException if construction fails for any reason
     */
    public static LinkedDataCache createDefault(final Sail sail) throws RippleException {
    	LinkedDataCache cache = new LinkedDataCache(sail);
    	configure(cache);
    	return cache;
    }

    public static void configure(LinkedDataCache cache) throws RippleException {
        RedirectManager redirectManager = new RedirectManager(cache.getSailConnection(), cache.getValueFactory());

        // Add URI dereferencers.
        HTTPURIDereferencer hdref = new HTTPURIDereferencer(cache, redirectManager);
        for (String x : NON_RDF_EXTENSIONS) {
            hdref.blackListExtension(x);
        }
        cache.addDereferencer("http", hdref);

        cache.addDereferencer("file", new FileURIDereferencer());
        cache.addDereferencer("jar", new JarURIDereferencer());

        String p = Ripple.getConfiguration().getString(LinkedDataSail.DATATYPE_HANDLING_POLICY);
        ParserConfig parserConfig = new ParserConfig();
        parserConfig.set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, !p.equals("ignore"));
        parserConfig.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, p.equals("verify"));
        parserConfig.set(BasicParserSettings.NORMALIZE_DATATYPE_VALUES, p.equals("normalize"));

        // Rdfizers for registered RDF formats
        for (RDFFormat f : RDFParserRegistry.getInstance().getKeys()) {
            Rdfizer r = new VerbatimRdfizer(f, parserConfig);
            for (String type : f.getMIMETypes()) {
                double qualityFactor = type.equals("application/rdf+xml") ? 1.0 : 0.5;
                cache.addRdfizer(new MediaType(type), r, qualityFactor);
            }
        }

        // Additional rdfizers
        Rdfizer imageRdfizer = new ImageRdfizer();
        // Mainstream EXIF-compatible image types: JPEG, TIFF
        cache.addRdfizer(MediaType.IMAGE_JPEG, imageRdfizer, 0.4);
        cache.addRdfizer(new MediaType("image/tiff"), imageRdfizer, 0.4);
        cache.addRdfizer(new MediaType("image/tiff-fx"), imageRdfizer, 0.4);
        // TODO: add an EXIF-based Rdfizer for RIFF WAV audio files
    }

    public LinkedDataCache() {}

    /**
     * @param sail underlying triple store for the cache
     * @throws RippleException if there is a configuration error
     */
    public LinkedDataCache(final Sail sail) throws RippleException {
    	initialize(sail);
    }

    public void initialize(Sail sail) throws RippleException {
        try {
            sailConnection = sail.getConnection();
            sailConnection.begin();
        } catch (SailException e) {
            throw new RippleException(e);
        }

        int capacity = Ripple.getConfiguration().getInt(LinkedDataSail.MEMORY_CACHE_CAPACITY);
        if (capacity < MINIMUM_CAPACITY) {
            logger.warn("LinkedDataSail.MEMORY_CACHE_CAPACITY is suspiciously low. Using "
                    + MINIMUM_CAPACITY);
        }

        this.metadata = new CachingMetadata(capacity, sail.getValueFactory());

        this.valueFactory = sail.getValueFactory();
        useBlankNodes = Ripple.getConfiguration().getBoolean(Ripple.USE_BLANK_NODES);

        this.expirationPolicy = new DefaultCacheExpirationPolicy();

        dataStore = new DataStore() {
            public RDFSink createInputSink(final SailConnection sc) {
                return new SesameOutputAdapter(new SailInserter(sc));
            }
        };
    }

    public synchronized void clear() throws RippleException {
        metadata.clear();

        SailConnection sc = getSailConnection();
        try {
            sc.clear();
            sc.commit();
            sc.begin();
        } catch (SailException e) {
            throw new RippleException(e);
        }
    }

    // note: only closes in one thread
    public synchronized void close() throws RippleException {
        try {
            SailConnection sc = getSailConnection();
            if (null != sc) {
                sc.close();
            }
        } catch (SailException e) {
            throw new RippleException(e);
        }
    }

    public ValueFactory getValueFactory() {
    	return valueFactory;
    }

    public SailConnection getSailConnection() throws RippleException {
        return sailConnection;
    }

    public void setDataStore(final DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * @return an application-specific mapping for URIs dereferenced by the cache, or null no mapping is used
     */
    public URIMap getURIMap() {
        return uriMap;
    }

    /**
     * Defines an application-specific mapping for URIs dereferenced by the cache, in the manner of a Web proxy.
     *
     * @param map the mapping
     */
    public void setURIMap(final URIMap map) {
        this.uriMap = map;
    }

    /**
     * @return an HTTP "Accept" header which matches the cache's collection of rdfizers
     */
    public String getAcceptHeader() {
        if (null == acceptHeader) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;

            // Order from highest quality to lowest.
            Comparator<MediaTypeInfo> comparator
                    = new Comparator<MediaTypeInfo>() {
                public int compare(final MediaTypeInfo first,
                                   final MediaTypeInfo second) {
                    return first.quality < second.quality ? 1 : first.quality > second.quality ? -1 : 0;
                }
            };

            MediaTypeInfo[] array = new MediaTypeInfo[rdfizers.size()];
            rdfizers.values().toArray(array);
            Arrays.sort(array, comparator);

            for (MediaTypeInfo m : array) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }

                sb.append(m.mediaType.getName());
                double quality = m.quality;
                if (1.0 != quality) {
                    sb.append(";q=").append(quality);
                }
            }

            acceptHeader = sb.toString();
        }

        return acceptHeader;
    }

    /**
     * Associates an rdfizer with a given media type.
     *
     * @param mediaType     a MIME type, e.g. "application/rdf+xml", "image/tiff"
     * @param rdfizer       the associated rdfizer
     * @param qualityFactor a quality value ranging from 0 to 1 which expresses
     *                      the client's preference for the given media type.
     *                      This value is used for HTTP content negotiation.
     */
    public void addRdfizer(final MediaType mediaType,
                           final Rdfizer rdfizer,
                           final double qualityFactor) {
        logger.info("adding RDFizer for media type " + mediaType + ": " + rdfizer);

        if (qualityFactor <= 0 || qualityFactor > 1) {
            throw new IllegalArgumentException("quality factor must be between 0 and 1");
        }

        if (null != rdfizers.get(mediaType)) {
            logger.warn("overriding already-registered RDFizer for media type " + mediaType);
        }

        MediaTypeInfo rq = new MediaTypeInfo();
        rq.mediaType = mediaType;
        rq.quality = qualityFactor;
        rq.rdfizer = rdfizer;
        rdfizers.put(mediaType, rq);

        acceptHeader = null;
    }

    /**
     * Associates a dereferencer with a given URI scheme.
     *
     * @param scheme the name of the URI scheme (e.g. "http", "ftp", "file", "jar")
     * @param dref   the associated dereferencer
     */
    public void addDereferencer(final String scheme, final Dereferencer dref) {
        logger.info("adding dereferencer for for URI scheme " + scheme + ": " + dref);

        dereferencers.put(scheme, dref);
    }

    /**
     * Retrieves caching metadata for a URI if it exists, but does not dereference the URI or modify the cache.
     *
     * @param uri the URI to look up
     * @param sc  a connection to a Sail
     * @return the current status of the URI, or null if it does not exist in the cache
     */
    public CacheEntry.Status peek(final IRI uri,
                                  final SailConnection sc) throws RippleException {
        return peekOrRetrieve(uri, sc, false);
    }

    /**
     * Retrieves caching metadata for a URI, possibly dereferencing a document from the Web first.
     *
     * @param uri the URI to look up and possibly dereference
     * @param sc  a connection to a Sail
     * @return the result of the dereferencing operation
     */
    public CacheEntry.Status retrieve(final IRI uri,
                                      final SailConnection sc) throws RippleException {
        return peekOrRetrieve(uri, sc, true);
    }

    // Look up and create the memo for a URI in one atomic operation, avoiding races between threads
    // The status of a URI in the cache is Undetermined until the retrieval operation is completed.
    private synchronized CacheEntry getSetMemo(final IRI uri,
                                              final String graphUri,
                                              final SailConnection sc,
                                              final boolean doRetrieve) throws RippleException {
        CacheEntry memo = metadata.getMemo(graphUri, sc);

        // If there is already a (non-expired) entry for this URI, just return its status.
        if (null != memo && !expirationPolicy.isExpired(uri.toString(), memo)) {
            return memo;
        }

        if (!doRetrieve) {
            return null;
        }

        memo = new CacheEntry(CacheEntry.Status.Undetermined);
        metadata.setMemo(graphUri, memo, null);
        memo.setStatus(CacheEntry.Status.CacheLookup);
        return memo;
    }

    private CacheEntry.Status peekOrRetrieve(final IRI uri,
                                             final SailConnection sc,
                                             final boolean doRetrieve) throws RippleException {
        // Find the named graph which stores all information associated with this URI
        String graphUri = RDFUtils.findGraphUri(uri.toString());

        CacheEntry memo = getSetMemo(uri, graphUri, sc, doRetrieve);
        CacheEntry.Status status = null == memo ? null : memo.getStatus();
        if (null == status || status != CacheEntry.Status.CacheLookup) {
            return status;
        }
        memo.setStatus(CacheEntry.Status.Undetermined);

        // This URI should be treated as a "black box" once created;
        // it need not resemble the URI it was created from.
        String retrievalUri;

        String mapped = null == uriMap ? uri.toString() : uriMap.get(uri.toString());
        retrievalUri = RDFUtils.removeFragmentIdentifier(mapped);

        Dereferencer dref;

        try {
            dref = chooseDereferencer(retrievalUri);
        } catch (URISyntaxException e) {
            return CacheEntry.Status.InvalidUri;
        }

        if (null == dref) {
            return CacheEntry.Status.BadUriScheme;
        }

        logger.info("dereferencing <"
                + StringUtils.escapeURIString(uri.toString()) + ">");

        memo.setDereferencer(dref.getClass().getName());

        // Note: from this point on, we are committed to actually dereferencing the URI,
        // and failures are explicitly stored as caching metadata.
        boolean wasTxnActive;
        try {
			wasTxnActive = sc.isActive();
	        if(autoCommit && !wasTxnActive) {
	        	sc.begin();
	        }
		} catch (SailException e) {
			throw new RippleException(e);
		}
        try {
            Representation rep;

            memo.setStatus(CacheEntry.Status.DereferencerError);
            rep = dref.dereference(retrievalUri);

            // a null representation indicates that dereferencing the URI would be redundant; exit early
            if (null == rep) {
                memo.setStatus(CacheEntry.Status.RedirectsToCached);
                return memo.getStatus();
            }

            // We have the representation, now try to rdfize it.

            memo.setMediaType(rep.getMediaType());

            Rdfizer rfiz = chooseRdfizer(memo.getMediaType());
            if (null == rfiz) {
                memo.setStatus(CacheEntry.Status.BadMediaType);
                return logStatus(uri, memo);
            }

            memo.setRdfizer(rfiz.getClass().getName());

            RDFSink adder = dataStore.createInputSink(sc);
            RDFBuffer buffer = new RDFBuffer(adder);

            // Note: any context information in the source document is discarded.
            RDFSink pipe = new SingleContextPipe(buffer, valueFactory.createIRI(graphUri), valueFactory);

            RDFHandler handler = new SesameInputAdapter(useBlankNodes
                    ? pipe
                    : new BNodeToURIFilter(pipe, valueFactory));

            InputStream is;
            try {
                is = rep.getStream();
            } catch (IOException e) {
                throw new RippleException(e);
            }

            // Use the namespace portion of the original URI as the base URI for the retrieved RDF document.
            String baseUri = uri.getNamespace();

            memo.setStatus(rfiz.rdfize(is, handler, baseUri));

            // Only update the graph in the triple store if the operation was successful.
            if (CacheEntry.Status.Success == memo.getStatus()) {
                try {
                    sc.removeStatements(null, null, null, valueFactory.createIRI(graphUri));
                } catch (SailException e) {
                    throw new RippleException(e);
                }

                buffer.flush();
            }
        } finally {
            metadata.setMemo(graphUri, memo, sc);

            // an autocommit happens independently of a call to LinkedDataSail#commit
            if (autoCommit) {
                try {
                    sc.commit();
                    if(wasTxnActive) {
                    	sc.begin();
                    }
                } catch (SailException e) {
                    throw new RippleException(e);
                }
            }

            logStatus(uri, memo);
        }

        return memo.getStatus();
    }

    /**
     * @return whether the cache commits to the triple store after each Web request
     * (true by default)
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * @param autoCommit whether the cache should commit to the triple store after each Web request
     *                   (true by default)
     */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /**
     * @param expirationPolicy the rule by which the cache determines whether a cache entry has expired.
     *                         If an entry has expired, the cache will issue a new request in order to refresh it.
     */
    public void setExpirationPolicy(CacheExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }

    public boolean getDereferenceSubjects() {
        return derefSubjects;
    }

    public void setDereferenceSubjects(final boolean flag) {
        this.derefSubjects = flag;
    }

    public boolean getDereferencePredicates() {
        return derefPredicates;
    }

    public void setDereferencePredicates(final boolean flag) {
        this.derefPredicates = flag;
    }

    public boolean getDereferenceObjects() {
        return derefObjects;
    }

    public void setDereferenceObjects(final boolean flag) {
        this.derefObjects = flag;
    }

    public boolean getDereferenceContexts() {
        return derefContexts;
    }

    public void setDereferenceContexts(final boolean flag) {
        this.derefContexts = flag;
    }

    private CacheEntry.Status logStatus(final IRI uri,
                                        final CacheEntry memo) {
        CacheEntry.Status status = memo.getStatus();

        if (CacheEntry.Status.Success != status && CacheEntry.Status.RedirectsToCached != status) {
            StringBuilder msg = new StringBuilder("Failed to dereference URI <"
                    + StringUtils.escapeURIString(uri.toString()) + "> (");

            msg.append("dereferencer: ").append(memo.getDereferencer());
            msg.append(", media type: ").append(memo.getMediaType());
            msg.append(", rdfizer: ").append(memo.getRdfizer());
            msg.append("): ").append(status);

            logger.info(msg.toString());
        }

        return status;
    }

    private Dereferencer chooseDereferencer(final String uri) throws URISyntaxException {
        String scheme = new java.net.URI(uri).getScheme();

        return dereferencers.get(scheme);
    }

    private Rdfizer chooseRdfizer(final MediaType mediaType) throws RippleException {
        MediaTypeInfo rq = rdfizers.get(mediaType);
        return (null == rq) ? null : rq.rdfizer;
    }


    private class DefaultCacheExpirationPolicy implements CacheExpirationPolicy {
        private long cacheLifetime;

        public DefaultCacheExpirationPolicy() throws RippleException {
            cacheLifetime = Ripple.getConfiguration().getLong(LinkedDataSail.CACHE_LIFETIME) * 1000;
        }

        public boolean isExpired(final String uri,
                                 final CacheEntry entry) {
            Date last = entry.getTimestamp();
            return null != last
                    && System.currentTimeMillis() - last.getTime() >= cacheLifetime;
        }
    }

    private class MediaTypeInfo {
        MediaType mediaType;
        public double quality;
        public Rdfizer rdfizer;
    }

    public interface DataStore {
        RDFSink createInputSink(SailConnection sc);
    }
}
