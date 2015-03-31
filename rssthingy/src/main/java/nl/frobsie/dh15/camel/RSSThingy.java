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
    public static String FILE_MAIL = "mail.eml";
    public static String FILE_RSS = "feed.xml";
    public static String FILE_RSS_PATH = "tmp";
    public static String FILE_XSL = "src/main/resources/template.xsl";

    /** Queries */
    public static String SQL_INSERT = "insert into public.feedentryqueue (title, link, description, pubdate) values (:#title, :#link, :#description, :#pubdate)";
    public static String SQL_GET_UNPROCESSED = "select * from feedentryqueue where processed=false";
    public static String SQL_GET_PROCESSED = "select id, title, link, description, pubdate, processed, externalid from feedentryqueue where processed=true";
    public static String SQL_GET_MAIL = "select * from email";
    public static String SQL_UPDATE_PROCESSED = "update feedentryqueue set processed = true where id = :#id";
    public static String SQL_UPDATE_EXTERNALID = "update feedentryqueue set externalid = :#externalid where id = :#id";

    /** Registry keys */
    public static String REGISTRY_KEY_DATASOURCE = "jdbc/postgres";
    public static String REGISTRY_KEY_FEEDBEAN = "feedBean";

    /**
     * Gebruikte EIP's :
     * - Pipes and Filters
     * - Message Translator (beans -> geen afhankelijkheid camel)
     * - Multicast
     *
     * Leest een RSS feed uit, zet de items in een PostgreSQL 
     * database, voert daar een (simpele) bewerking op uit,
     * transformeert het resultaat middels XSLT naar HTML
     * en verstuurt deze per mail richting eindgebruiker.
     *
     * De gegevens die middels de SQL Component opgeslagen
     * worden in de database zijn puur voor testdoeleinden
     * ingebouwd. Middels een simpel php script kunnen deze
     * gegevens uitgelezen worden op een website.
     * 
     * @param  args[]
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {

        // Init
        SimpleRegistry registry = new SimpleRegistry() ;
        PostgresBean postgresBean = new PostgresBean();
        FeedBean feedBean = new FeedBean();

        // Voeg de custom beans to aan de registry
        registry.put(REGISTRY_KEY_DATASOURCE, postgresBean.getDataSource());
        registry.put(REGISTRY_KEY_FEEDBEAN, feedBean);

        // Bouw de camelContext op met de registry
        CamelContext context = new DefaultCamelContext(registry);

        // Voeg de route toe aan de camelContext
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                
                // Controleer of gmail gegevens aanwezig zijn
                RSSThingy.validateGmail();
                
                /**
                 * Route 1
                 * Sla de feed in zijn geheel op in de tmp folder.
                 * Let op : Deze wordt steeds overschreven.
                 */
                from("rss:" + URL_RSS + "?splitEntries=false").
                    marshal().rss().
                    to("file:" + FILE_RSS_PATH + "?fileName=" + FILE_RSS);

                /**
                 * Route 2
                 * Haalt de feed per aanwezig item op van opgegeven url. 
                 * Let op dat hier niet marshal().rss() uitgevoerd wordt, 
                 * omdat we de daadwerkelijke ROME SyncFeed objecten
                 * willen hebben en niet de XML als string.
                 */
                from("rss:file:" + FILE_RSS_PATH + "/" + FILE_RSS + "?splitEntries=true&consumer.delay=100").
                    to("direct:rssResults");

                /**
                 * Route 3
                 * Converteren van de rss results naar een format
                 * wat de SQL component snapt en deze inserten.
                 */
                from("direct:rssResults").
                    to("bean:feedBean?method=convert").
                    to("sql:" + SQL_INSERT + "?dataSource=" + REGISTRY_KEY_DATASOURCE);

                /**
                 * Route 4
                 * Verwerkt elk record in de feedentryqueue tabel 
                 * zodat het externalid (a.d.h.v. de link) gevuld wordt.
                 */
                from("sql:" + SQL_GET_UNPROCESSED + "?dataSource=" + REGISTRY_KEY_DATASOURCE).
                    to("bean:feedBean?method=processEntry").
                    multicast().
                        to("direct:updateProcessed", "direct:updateExternalId");
                    
                /**
                 * Route 5
                 * Update processed boolean in database.
                 */
                from("direct:updateProcessed").
                    to("sql:" + SQL_UPDATE_PROCESSED + "?dataSource=" + REGISTRY_KEY_DATASOURCE);

                /**
                 * Route 6
                 * Update externalid in database.
                 */
                from("direct:updateExternalId").
                    to("sql:" + SQL_UPDATE_EXTERNALID + "?dataSource=" + REGISTRY_KEY_DATASOURCE);

                /**
                 * Route 7
                 * Laadt de feed opnieuw in, maar dit maal in zijn geheel.
                 * Op deze manier kan er een XSL stylesheet overheen gehaald 
                 * worden ter voorbereiding op het mailen. Ook wordt
                 * de html voor het mailtje klaargezet in een bestand (ter controle).
                 */
                from("rss:file:" + FILE_RSS_PATH + "/" + FILE_RSS + "?splitEntries=false").
                    marshal().rss().
                    to("xslt:file:" + FILE_XSL).
                    to("log:nl.frobsie.dh15.camel.RSSThingy").
                    to("file:" + FILE_RSS_PATH + "?fileName=" + FILE_MAIL).
                    to("direct:xsltResult");
                    

                /**
                 * Route 8
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

    /**
     *  Controleer of GMail gegevens ingevuld zijn
     *  
     * @throws RuntimeException
     */
    public static void validateGmail() throws RuntimeException {
        if (GMAIL_FROM.equals("") || GMAIL_TO.equals("") || GMAIL_USER.equals("") || GMAIL_PASSWORD.equals("")) {
            throw new RuntimeException("Gmail gegevens niet (volledig) ingevuld!");
        }
    }
}
