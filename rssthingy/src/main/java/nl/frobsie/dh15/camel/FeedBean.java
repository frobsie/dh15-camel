package nl.frobsie.dh15.camel;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.io.*;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndEntry;

import org.apache.log4j.Logger;

public class FeedBean
{
    final static Logger logger = Logger.getLogger(FeedBean.class);
    
    /**
     * Verwerkt het SyndFeed object wat teruggegegven wordt
     * uit de camel-rss module tot een object wat de datasource
     * begrijpt. Omdat het pollen van de feed op feed-item basis
     * gebeurt kunnen we hier altijd 1 item in de entries lijst
     * verwachten.
     * 
     * @param  SyndFeed feed
     * @return Map<String, Object>
     */
    public Map<String, Object> convert(SyndFeed feed) {
        List<SyndEntry> entries = feed.getEntries();

        if (!entries.isEmpty()) {
            return convertSyndEntryToMap(entries.get(0));
        }

        return null;
    }

    /**
     * Converteert een enkel SyndEntry object.
     * Zie http://www.docjar.com/docs/api/com/sun/syndication/feed/synd/SyndEntry.html
     * 
     * @param SyndEntry entry
     * @return Map<String, Object>
     */
    protected Map<String, Object> convertSyndEntryToMap(SyndEntry entry) {
        Map<String, Object> feedEntry = new HashMap<String, Object>();
        feedEntry.put("title", entry.getTitle());
        feedEntry.put("link", entry.getLink());
        feedEntry.put("description", entry.getDescription().getValue());
        feedEntry.put("pubdate", entry.getPublishedDate());
        return feedEntry;
    }

    /**
     * Verwerkt het link veld wat in de feedEntry record
     * zit tot een externalid zodat hier mee gematched kan worden.
     * 
     * @param  Map<String, Object> feedEntry
     * @return Map<String, Object>
     */
    public Map<String, Object> processEntry(Map<String, Object> feedEntry) {
        Map<String, Object> updateEntry = new HashMap<String, Object>();

        Boolean processed = (Boolean) feedEntry.get("processed");
        String link = (String) feedEntry.get("link");
        String externalId = "";

        // Strip beide kanten van de link zodat
        // we een identifier overhouden
        // Dit omdat de ROME API geen ondersteuning heeft 
        // voor het guid element in de XML.
        if (!link.equals("")) {
            externalId = link.replace("http://phys.org/news", "");
            externalId = externalId.replace(".html", "");
        }

        updateEntry.put("id", feedEntry.get("id"));
        updateEntry.put("processed", processed);
        updateEntry.put("externalid", externalId);
        return updateEntry;
    }
}
