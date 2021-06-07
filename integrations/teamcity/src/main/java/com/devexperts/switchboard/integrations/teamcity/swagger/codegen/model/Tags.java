/*
 * TeamCity REST API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 2018.1
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tags
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-06-01T01:12:43.660+03:00")
@XmlRootElement(name = "tags")
@XmlAccessorType(XmlAccessType.FIELD)
@JacksonXmlRootElement(localName = "tags")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tags {
    @JsonProperty("count")
    @JacksonXmlProperty(isAttribute = true, localName = "count")
    @XmlAttribute(name = "count")
    private Integer count = null;

    @JsonProperty("tag")
    // Is a container wrapped=false
    // items.name=tag items.baseName=tag items.xmlName= items.xmlNamespace=
    // items.example= items.type=Tag
    @XmlElement(name = "tag")
    private List<Tag> tag = null;

    public Tags count(Integer count) {
        this.count = count;
        return this;
    }

    /**
     * Get count
     *
     * @return count
     **/
    @ApiModelProperty(value = "")
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Tags tag(List<Tag> tag) {
        this.tag = tag;
        return this;
    }

    public Tags addTagItem(Tag tagItem) {
        if (this.tag == null) {
            this.tag = new ArrayList<Tag>();
        }
        this.tag.add(tagItem);
        return this;
    }

    /**
     * Get tag
     *
     * @return tag
     **/
    @ApiModelProperty(value = "")
    public List<Tag> getTag() {
        return tag;
    }

    public void setTag(List<Tag> tag) {
        this.tag = tag;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tags tags = (Tags) o;
        return Objects.equals(this.count, tags.count) &&
                Objects.equals(this.tag, tags.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, tag);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Tags {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    tag: ").append(toIndentedString(tag)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
