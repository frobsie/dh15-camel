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

                // from("direct:truncate").
                //     to("sql:truncate table feedentry?dataSource=postgres").
                //     to("mock:truncate");

                // from("direct:sequence").
                //     to("sql:alter sequence feedentry_id_seq restart?dataSource=postgres").
                //     to("mock:sequence");

                /**
                 * Route 1
                 * Haalt de feed per aanwezig item op van opgegeven url en zet deze om
                 * naar records in de database.
                 */
                from("rss:" + rssUrl + "?splitEntries=true&consumer.delay=100").
                    // marshal().rss().
                    // to("file:tmp");
                    to("bean:feedConvertBean?method=convert").
                    to("sql:" + sqlInsert + "?dataSource=postgres");

                // from("rss:" + rssUrl + "?splitEntries=false").
                //     to("bean:feedConvertBean?method=convertAll");

                // from("bean:feedConvertBean?method=getFeedItems").
                //     to("bean:feedConvertBean?method=getItem").
                //     to("sql:" + sqlInsert + "?dataSource=postgres");
                //     //to("mock:out");

                //from("bean:testBean?method=generatePersons").
                    //to("sql:insert into dh15.person (firstname, middlename, lastname) values (:#firstname, :#middlename, :#lastname)?dataSource=postgres");

                // from("sql:select * from dh15.person?dataSource=postgres").
                //  to("bean:testBean?method=printData").
                //  to("stream:out");

                // from("bean:testBean?method=printTest").
                //  to("stream:out");

            }
        });

        context.start();
        Thread.sleep(5000);
        context.stop();
    }
}
