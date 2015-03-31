package nl.frobsie.dh15.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import nl.frobsie.dh15.camel.PostgresBean;
import nl.frobsie.dh15.camel.FeedBean;

public class RSSThingy {

    /** GMAIL */
    private static String GMAIL_USER = "";
    private static String GMAIL_PASSWORD = "";
    private static String GMAIL_FROM = "";
    private static String GMAIL_TO = "";

    /** RSS */
    public static String URL_RSS = "http://phys.org/rss-feed/space-news/astronomy/";
    public static String FILE_RSS = "src/main/resources/feed.xml";
    public static String FILE_XSL = "src/main/resources/template.xsl";

    /** Queries */
    public static String SQL_INSERT = "insert into public.feedentryqueue (title, link, description, pubdate) values (:#title, :#link, :#description, :#pubdate)";
    public static String SQL_GET_UNPROCESSED = "select * from feedentryqueue where processed=false";
    public static String SQL_GET_PROCESSED = "select id, title, link, description, pubdate, processed, externalid from feedentryqueue where processed=true";
    public static String SQL_GET_MAIL = "select * from email";
    public static String SQL_UPDATE_PROCESSED = "update feedentryqueue set processed = true where id = :#id";
    public static String SQL_UPDATE_EXTERNALID = "update feedentryqueue set externalid = :#externalid where id = :#id";

    public static void main(String args[]) throws Exception {

        // Init
        SimpleRegistry registry = new SimpleRegistry() ;
        PostgresBean postgresBean = new PostgresBean();
        FeedBean feedBean = new FeedBean();

        // Voeg de custom beans to aan de registry
        registry.put("jdbc/postgres", postgresBean.getDataSource());
        registry.put("feedBean", feedBean);

        // Bouw de camelContext op met de registry
        CamelContext context = new DefaultCamelContext(registry);

        // Voeg de route toe aan de camelContext
        context.addRoutes(new RouteBuilder() {
            public void configure() {

                /*
                 * Gebruikte EIP's :
                 * - Pipes and Filters
                 * - Message Translator (beans -> geen afhankelijkheid camel)
                 * - Multicast
                 */

                /**
                 * Route 1
                 * Haalt de feed per aanwezig item op van opgegeven url. 
                 * Let op dat hier niet marshal().rss() uitgevoerd wordt, 
                 * omdat we de daadwerkelijke ROME SyncFeed objecten
                 * willen hebben en niet de XML als string.
                 */
                from("rss:file:" + FILE_RSS + "?splitEntries=true&consumer.delay=100"). // LOKAAL VANWEGE GARE VERBINDING
                //from("rss:" + URL_RSS + "?splitEntries=true&consumer.delay=250").
                    to("direct:rssResults");

                /**
                 * Route 2
                 * Converteren van de rss results naar een format
                 * wat de SQL component snapt en deze inserten.
                 */
                from("direct:rssResults").
                    to("bean:feedBean?method=convert").
                    to("sql:" + SQL_INSERT + "?dataSource=jdbc/postgres");

                /**
                 * Route 3
                 * Verwerkt elk record in de feedentryqueue tabel 
                 * zodat het externalid (a.d.h.v. de link) gevuld wordt.
                 */
                from("sql:" + SQL_GET_UNPROCESSED + "?dataSource=jdbc/postgres").
                    to("bean:feedBean?method=processEntry").
                    multicast().
                        to("direct:updateProcessed", "direct:updateExternalId");
                    
                /**
                 * Route 4
                 * Update processed boolean in database.
                 */
                from("direct:updateProcessed").
                    to("sql:" + SQL_UPDATE_PROCESSED + "?dataSource=jdbc/postgres");

                /**
                 * Route 5
                 * Update externalid in database.
                 */
                from("direct:updateExternalId").
                    to("sql:" + SQL_UPDATE_EXTERNALID + "?dataSource=jdbc/postgres");

                // TODO 
                // kopieerslag van queue tabel naar iets anders
                // zodat het niet compleet nutteloos is ;-)

                /**
                 * Route 5
                 * Converteer de processed results in de database
                 * naar een XML bestand. Leg daar een XSL bestand overheen voor
                 * styling en verstuur het naar alle emailadresssen die
                 * in de database bekend zijn.
                 */
                from("rss:file:" + FILE_RSS + "?splitEntries=false").
                    marshal().rss().
                    to("xslt:file:" + FILE_XSL).
                    to("log:nl.frobsie.dh15.camel.RSSThingy").
                    to("direct:xsltResult");

                /**
                 * Route 6
                 * Mail de geconverteerde rssresults
                 */
                from("direct:xsltResult").
                    setHeader("subject", simple("RSS Thingy")).
                    to("smtps://" + GMAIL_USER + "@smtp.gmail.com?password=" + GMAIL_PASSWORD + "&to=" + GMAIL_TO + "&from=" + GMAIL_FROM);
            }
        });

        context.start();
        Thread.sleep(10000);
        context.stop();
    }
}
