package com.yourpackage;

import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.Extractors;

public class IndexStartupListener implements MapListener {

    private static final ValueExtractor<MyServiceData, String> EXTRACT_ISIN =
            Extractors.chained("getBase.isin");

    private static final ValueExtractor<MyServiceData, String> EXTRACT_RIC =
            Extractors.chained("getBase.ric");

    private volatile boolean initialized = false;

    @Override
    public synchronized void entryInserted(MapEvent event) {
        initialize(event);
    }

    @Override
    public void entryUpdated(MapEvent event) {
        initialize(event);
    }

    @Override
    public void entryDeleted(MapEvent event) {
        initialize(event);
    }

    private void initialize(MapEvent event) {
        if (initialized) {
            return;
        }

        NamedCache cache = (NamedCache) event.getMap();

        cache.addIndex(EXTRACT_ISIN, false, null);
        cache.addIndex(EXTRACT_RIC, false, null);

        initialized = true;

        // Remove listener after execution (run once)
        cache.removeMapListener(this);

        System.out.println("Indexes created at server startup.");
    }
}

<listener>
        <class-name>com.yourpackage.IndexStartupListener</class-name>
      </listener>
