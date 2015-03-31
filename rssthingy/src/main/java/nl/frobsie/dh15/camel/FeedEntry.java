package nl.frobsie.dh15.camel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElementWrapper;

// TODO
// kijken of het met JAXB lukt ipv de rss component

//@XmlElementWrapper(name = "feedentries")
@XmlRootElement(name = "feedentry")
@XmlAccessorType(XmlAccessType.FIELD)
public class FeedEntry
{
    @XmlElement
    private Integer id;

    @XmlElement
    private String title;

    @XmlElement
    private String link;

    @XmlElement
    private String description;

    @XmlElement
    private String pubDate;

    @XmlElement
    private String externalId;

    // @Override
    // public String toString() {
    //     return "Feedentry: " + id + " - " + title;
    // }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
