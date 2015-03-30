package nl.frobsie.dh15.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import nl.frobsie.dh15.camel.PostgresBean;
import nl.frobsie.dh15.camel.FeedBean;
import nl.frobsie.dh15.camel.FeedEntry;

public class RSSThingy {

    public static String URL_RSS = "http://phys.org/rss-feed/space-news/astronomy/";
    public static String FILE_RSS = "src/main/resources/feed.xml";

    public static String SQL_INSERT = "insert into public.feedentryqueue (title, link, description, pubdate) values (:#title, :#link, :#description, :#pubdate)";
    //public static String SQL_GET = "select * from feedentryqueue where processed=false";
    public static String SQL_GET_PROCESSED = "select id, title, link, description, pubdate, processed, externalid from feedentryqueue where processed=true";
    public static String SQL_GET_MAIL = "select * from email";
    public static String SQL_PROCESS = "update feedentryqueue set processed = true, externalid = :#externalid where id = :#id";

    public static String MSG_MAIL = "Email versturen?";

    public static void main(String args[]) throws Exception {

        // Init
        SimpleRegistry registry = new SimpleRegistry() ;
        PostgresBean postgresBean = new PostgresBean();
        FeedBean feedBean = new FeedBean();

        // Voeg de custom beans to aan de registry
        registry.put("postgres", postgresBean.getDataSource());
        registry.put("feedBean", feedBean);

        // Bouw de camelContext op met de registry
        CamelContext context = new DefaultCamelContext(registry);

        // Voeg de route toe aan de camelContext
        context.addRoutes(new RouteBuilder() {
            public void configure() {

                /*
                 * EIP's :
                 * - Pipes and Filters
                 * - Multicast
                 *
                 * TODO
                 * recipient list
                 */

                /**
                 * Route 1
                 * Haalt de feed per aanwezig item op van opgegeven url en zet deze om
                 * naar records in de database. Let op dat hier niet marshal().rss()
                 * uitgevoerd wordt, omdat we de daadwerkelijke ROME SyncFeed objecten
                 * willen hebben en niet de XML als string.
                 */
                from("rss:file:" + FILE_RSS + "?splitEntries=true&consumer.delay=100"). // LOKAAL VANWEGE GARE VERBINDING
                //from("rss:" + URL_RSS + "?splitEntries=true&consumer.delay=250").
                    to("bean:feedBean?method=convert").
                    multicast().
                    to("direct:insert", "direct:unprocessed");

                /**
                 * Route 2
                 * Insert de zojuist geconverteerde items
                 * in de database.
                 */
                from("direct:insert").
                    to("sql:" + SQL_INSERT + "?dataSource=postgres");

                /**
                 * Route 3
                 * Verwerkt elk record in de feedentryqueue tabel 
                 * zodat het externalid (a.d.h.v. de link) gevuld wordt.
                 */
                from("direct:unprocessed").
                    to("bean:feedBean?method=processEntry").
                    to("sql:" + SQL_PROCESS + "?dataSource=postgres");

                /**
                 * Route 4
                 * Converteer de processed results in de database
                 * naar een XML bestand. Leg daar een XSL bestand overheen voor
                 * styling en verstuur het naar alle emailadresssen die
                 * in de database bekend zijn.
                 */
                // from("sql:" + SQL_GET_PROCESSED + "?dataSource=postgres&outputClass=nl.frobsie.dh15.camel.FeedEntry").
                //     //convertBodyTo(String.class).
                //     to("xslt:file:src/main/resources/template.xsl");
                    //to("stream:out");
                    //.to("smtps://<google user name>@smtp.gmail.com?password=<passwd>&to=<email address>&from=camellover@gmail.com");

            }
        });

        context.start();
        Thread.sleep(10000);
        context.stop();
    }
}
