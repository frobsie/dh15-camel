package nl.frobsie.dh15.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import nl.frobsie.dh15.camel.PostgresBean;
import nl.frobsie.dh15.camel.FeedBean;

public class RSSThingy {

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

                String rssUrl = "http://phys.org/rss-feed/space-news/astronomy/";

                String sqlInsertQueue = "insert into public.feedentryqueue (title, link, description, pubdate) values (:#title, :#link, :#description, :#pubdate)";

                String sqlGet = "select * from feedentryqueue where processed=false";

                String sqlProcess = "update feedentryqueue set processed = true, externalid = :#externalid where id = :#id";

                /**
                 * Route 1
                 * Haalt de feed per aanwezig item op van opgegeven url en zet deze om
                 * naar records in de database. Let op dat hier niet marshal().rss()
                 * uitgevoerd wordt, omdat we de daadwerkelijke ROME SyncFeed objecten
                 * willen hebben en niet de XML als string.
                 */
                from("rss:" + rssUrl + "?splitEntries=true&consumer.delay=100").
                    to("bean:feedBean?method=convert").
                    to("sql:" + sqlInsertQueue + "?dataSource=postgres");

                /**
                 * Route 2
                 * Verwerkt elk record in de feedentryqueue tabel 
                 * zodat het externalid (a.d.h.v. de link) gevuld wordt.
                 */
                from("sql:" + sqlGet + "?dataSource=postgres").
                    to("bean:feedBean?method=processEntry").
                    to("sql:" + sqlProcess + "?dataSource=postgres");
            }
        });

        context.start();
        Thread.sleep(10000);
        context.stop();
    }
}
