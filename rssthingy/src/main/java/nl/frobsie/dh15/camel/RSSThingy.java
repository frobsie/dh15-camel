package nl.frobsie.dh15.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import nl.frobsie.dh15.camel.PostgresBean;
import nl.frobsie.dh15.camel.FeedConvertBean;

public class RSSThingy {

    public static void main(String args[]) throws Exception {

        // Init
        SimpleRegistry registry = new SimpleRegistry() ;
        PostgresBean postgresBean = new PostgresBean();
        FeedConvertBean feedConvertBean = new FeedConvertBean();

        // Voeg de custom beans to aan de registry
        registry.put("postgres", postgresBean.getDataSource());
        registry.put("feedConvertBean", feedConvertBean);

        // Bouw de camelContext op met de registry
        CamelContext context = new DefaultCamelContext(registry);

        // Voeg de route toe aan de camelContext
        context.addRoutes(new RouteBuilder() {
            public void configure() {

                String rssUrl = "http://phys.org/rss-feed/space-news/astronomy/";

                String sqlInsert = "insert into public.feedentry (title, link, description, pubdate) values (:#title, :#link, :#description, :#pubdate)";

                /**
                 * Route 1
                 * Haalt de feed per aanwezig item op van opgegeven url en zet deze om
                 * naar records in de database.
                 */
                from("rss:" + rssUrl + "?splitEntries=true&consumer.delay=100").
                    to("bean:feedConvertBean?method=convert").
                    to("sql:" + sqlInsert + "?dataSource=postgres");
            }
        });

        context.start();
        Thread.sleep(5000);
        context.stop();
    }
}
