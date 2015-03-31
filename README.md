Gmail STMP statics wijzigen in RSSThhingy.java

Gegevens van database aanpassen in PostgresBean :
```sh
dataSource.setDriverClassName("org.postgresql.Driver");
dataSource.setUsername("postgres");
dataSource.setPassword("postgres");
dataSource.setUrl("jdbc:postgresql://192.168.3.60:5432/dh15");
```

CREATE script draaien :
```sh
postgres.sql
```

Uitvoeren met :
```sh
mvn compile exec:java -Dexec.mainClass=nl.frobsie.dh15.camel.RSSThingy
```