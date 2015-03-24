package nl.frobsie.dh15.camel;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndEntry;

import org.apache.log4j.Logger;

public class FeedConvertBean
{
    final static Logger logger = Logger.getLogger(FeedConvertBean.class);

    private List<Map<String, Object>> feedItems = new ArrayList<Map<String,Object>>();

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
}
