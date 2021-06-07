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
import java.util.Objects;

/**
 * Build
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-06-01T01:12:43.660+03:00")
@XmlRootElement(name = "build")
@XmlAccessorType(XmlAccessType.FIELD)
@JacksonXmlRootElement(localName = "build")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Build {
    @JsonProperty("id")
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    @XmlAttribute(name = "id")
    private Long id = null;

    @JsonProperty("taskId")
    @JacksonXmlProperty(isAttribute = true, localName = "taskId")
    @XmlAttribute(name = "taskId")
    private Long taskId = null;

    @JsonProperty("buildTypeId")
    @JacksonXmlProperty(isAttribute = true, localName = "buildTypeId")
    @XmlAttribute(name = "buildTypeId")
    private String buildTypeId = null;

    @JsonProperty("buildTypeInternalId")
    @JacksonXmlProperty(isAttribute = true, localName = "buildTypeInternalId")
    @XmlAttribute(name = "buildTypeInternalId")
    private String buildTypeInternalId = null;

    @JsonProperty("number")
    @JacksonXmlProperty(isAttribute = true, localName = "number")
    @XmlAttribute(name = "number")
    private String number = null;

    @JsonProperty("status")
    @JacksonXmlProperty(isAttribute = true, localName = "status")
    @XmlAttribute(name = "status")
    private String status = null;

    @JsonProperty("state")
    @JacksonXmlProperty(isAttribute = true, localName = "state")
    @XmlAttribute(name = "state")
    private String state = null;

    @JsonProperty("running")
    @JacksonXmlProperty(isAttribute = true, localName = "running")
    @XmlAttribute(name = "running")
    private Boolean running = false;

    @JsonProperty("composite")
    @JacksonXmlProperty(isAttribute = true, localName = "composite")
    @XmlAttribute(name = "composite")
    private Boolean composite = false;

    @JsonProperty("failedToStart")
    @JacksonXmlProperty(isAttribute = true, localName = "failedToStart")
    @XmlAttribute(name = "failedToStart")
    private Boolean failedToStart = false;

    @JsonProperty("personal")
    @JacksonXmlProperty(isAttribute = true, localName = "personal")
    @XmlAttribute(name = "personal")
    private Boolean personal = false;

    @JsonProperty("percentageComplete")
    @JacksonXmlProperty(isAttribute = true, localName = "percentageComplete")
    @XmlAttribute(name = "percentageComplete")
    private Integer percentageComplete = null;

    @JsonProperty("branchName")
    @JacksonXmlProperty(isAttribute = true, localName = "branchName")
    @XmlAttribute(name = "branchName")
    private String branchName = null;

    @JsonProperty("defaultBranch")
    @JacksonXmlProperty(isAttribute = true, localName = "defaultBranch")
    @XmlAttribute(name = "defaultBranch")
    private Boolean defaultBranch = false;

    @JsonProperty("unspecifiedBranch")
    @JacksonXmlProperty(isAttribute = true, localName = "unspecifiedBranch")
    @XmlAttribute(name = "unspecifiedBranch")
    private Boolean unspecifiedBranch = false;

    @JsonProperty("history")
    @JacksonXmlProperty(isAttribute = true, localName = "history")
    @XmlAttribute(name = "history")
    private Boolean history = false;

    @JsonProperty("pinned")
    @JacksonXmlProperty(isAttribute = true, localName = "pinned")
    @XmlAttribute(name = "pinned")
    private Boolean pinned = false;

    @JsonProperty("href")
    @JacksonXmlProperty(isAttribute = true, localName = "href")
    @XmlAttribute(name = "href")
    private String href = null;

    @JsonProperty("webUrl")
    @JacksonXmlProperty(isAttribute = true, localName = "webUrl")
    @XmlAttribute(name = "webUrl")
    private String webUrl = null;

    @JsonProperty("queuePosition")
    @JacksonXmlProperty(isAttribute = true, localName = "queuePosition")
    @XmlAttribute(name = "queuePosition")
    private Integer queuePosition = null;

    @JsonProperty("limitedChangesCount")
    @JacksonXmlProperty(isAttribute = true, localName = "limitedChangesCount")
    @XmlAttribute(name = "limitedChangesCount")
    private Integer limitedChangesCount = null;

    @JsonProperty("artifactsDirectory")
    @JacksonXmlProperty(isAttribute = true, localName = "artifactsDirectory")
    @XmlAttribute(name = "artifactsDirectory")
    private String artifactsDirectory = null;

    @JsonProperty("statusText")
    @JacksonXmlProperty(localName = "statusText")
    @XmlElement(name = "statusText")
    private String statusText = null;

    @JsonProperty("comment")
    @JacksonXmlProperty(localName = "comment")
    @XmlElement(name = "comment")
    private Comment comment = null;

    @JsonProperty("tags")
    @JacksonXmlProperty(localName = "tags")
    @XmlElement(name = "tags")
    private Tags tags = null;

    @JsonProperty("pinInfo")
    @JacksonXmlProperty(localName = "pinInfo")
    @XmlElement(name = "pinInfo")
    private Comment pinInfo = null;

    @JsonProperty("startEstimate")
    @JacksonXmlProperty(localName = "startEstimate")
    @XmlElement(name = "startEstimate")
    private String startEstimate = null;

    @JsonProperty("waitReason")
    @JacksonXmlProperty(localName = "waitReason")
    @XmlElement(name = "waitReason")
    private String waitReason = null;

    @JsonProperty("canceledInfo")
    @JacksonXmlProperty(localName = "canceledInfo")
    @XmlElement(name = "canceledInfo")
    private Comment canceledInfo = null;

    @JsonProperty("queuedDate")
    @JacksonXmlProperty(localName = "queuedDate")
    @XmlElement(name = "queuedDate")
    private String queuedDate = null;

    @JsonProperty("startDate")
    @JacksonXmlProperty(localName = "startDate")
    @XmlElement(name = "startDate")
    private String startDate = null;

    @JsonProperty("finishDate")
    @JacksonXmlProperty(localName = "finishDate")
    @XmlElement(name = "finishDate")
    private String finishDate = null;

    @JsonProperty("agent")
    @JacksonXmlProperty(localName = "agent")
    @XmlElement(name = "agent")
    private Agent agent = null;

    @JsonProperty("compatibleAgents")
    @JacksonXmlProperty(localName = "compatibleAgents")
    @XmlElement(name = "compatibleAgents")
    private Agents compatibleAgents = null;

    @JsonProperty("properties")
    @JacksonXmlProperty(localName = "properties")
    @XmlElement(name = "properties")
    private Properties properties = null;

    @JsonProperty("resultingProperties")
    @JacksonXmlProperty(localName = "resultingProperties")
    @XmlElement(name = "resultingProperties")
    private Properties resultingProperties = null;

    @JsonProperty("statistics")
    @JacksonXmlProperty(localName = "statistics")
    @XmlElement(name = "statistics")
    private Properties statistics = null;

    @JsonProperty("settingsHash")
    @JacksonXmlProperty(localName = "settingsHash")
    @XmlElement(name = "settingsHash")
    private String settingsHash = null;

    @JsonProperty("currentSettingsHash")
    @JacksonXmlProperty(localName = "currentSettingsHash")
    @XmlElement(name = "currentSettingsHash")
    private String currentSettingsHash = null;

    @JsonProperty("modificationId")
    @JacksonXmlProperty(localName = "modificationId")
    @XmlElement(name = "modificationId")
    private String modificationId = null;

    @JsonProperty("chainModificationId")
    @JacksonXmlProperty(localName = "chainModificationId")
    @XmlElement(name = "chainModificationId")
    private String chainModificationId = null;

    @JsonProperty("usedByOtherBuilds")
    @JacksonXmlProperty(isAttribute = true, localName = "usedByOtherBuilds")
    @XmlAttribute(name = "usedByOtherBuilds")
    private Boolean usedByOtherBuilds = false;

    @JsonProperty("statusChangeComment")
    @JacksonXmlProperty(localName = "statusChangeComment")
    @XmlElement(name = "statusChangeComment")
    private Comment statusChangeComment = null;

    @JsonProperty("locator")
    @JacksonXmlProperty(isAttribute = true, localName = "locator")
    @XmlAttribute(name = "locator")
    private String locator = null;

    public Build id(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     **/
    @ApiModelProperty(value = "")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Build taskId(Long taskId) {
        this.taskId = taskId;
        return this;
    }

    /**
     * Get taskId
     *
     * @return taskId
     **/
    @ApiModelProperty(value = "")
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Build buildTypeId(String buildTypeId) {
        this.buildTypeId = buildTypeId;
        return this;
    }

    /**
     * Get buildTypeId
     *
     * @return buildTypeId
     **/
    @ApiModelProperty(value = "")
    public String getBuildTypeId() {
        return buildTypeId;
    }

    public void setBuildTypeId(String buildTypeId) {
        this.buildTypeId = buildTypeId;
    }

    public Build buildTypeInternalId(String buildTypeInternalId) {
        this.buildTypeInternalId = buildTypeInternalId;
        return this;
    }

    /**
     * Get buildTypeInternalId
     *
     * @return buildTypeInternalId
     **/
    @ApiModelProperty(value = "")
    public String getBuildTypeInternalId() {
        return buildTypeInternalId;
    }

    public void setBuildTypeInternalId(String buildTypeInternalId) {
        this.buildTypeInternalId = buildTypeInternalId;
    }

    public Build number(String number) {
        this.number = number;
        return this;
    }

    /**
     * Get number
     *
     * @return number
     **/
    @ApiModelProperty(value = "")
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Build status(String status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @ApiModelProperty(value = "")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Build state(String state) {
        this.state = state;
        return this;
    }

    /**
     * Get state
     *
     * @return state
     **/
    @ApiModelProperty(value = "")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Build running(Boolean running) {
        this.running = running;
        return this;
    }

    /**
     * Get running
     *
     * @return running
     **/
    @ApiModelProperty(value = "")
    public Boolean isRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public Build composite(Boolean composite) {
        this.composite = composite;
        return this;
    }

    /**
     * Get composite
     *
     * @return composite
     **/
    @ApiModelProperty(value = "")
    public Boolean isComposite() {
        return composite;
    }

    public void setComposite(Boolean composite) {
        this.composite = composite;
    }

    public Build failedToStart(Boolean failedToStart) {
        this.failedToStart = failedToStart;
        return this;
    }

    /**
     * Get failedToStart
     *
     * @return failedToStart
     **/
    @ApiModelProperty(value = "")
    public Boolean isFailedToStart() {
        return failedToStart;
    }

    public void setFailedToStart(Boolean failedToStart) {
        this.failedToStart = failedToStart;
    }

    public Build personal(Boolean personal) {
        this.personal = personal;
        return this;
    }

    /**
     * Get personal
     *
     * @return personal
     **/
    @ApiModelProperty(value = "")
    public Boolean isPersonal() {
        return personal;
    }

    public void setPersonal(Boolean personal) {
        this.personal = personal;
    }

    public Build percentageComplete(Integer percentageComplete) {
        this.percentageComplete = percentageComplete;
        return this;
    }

    /**
     * Get percentageComplete
     *
     * @return percentageComplete
     **/
    @ApiModelProperty(value = "")
    public Integer getPercentageComplete() {
        return percentageComplete;
    }

    public void setPercentageComplete(Integer percentageComplete) {
        this.percentageComplete = percentageComplete;
    }

    public Build branchName(String branchName) {
        this.branchName = branchName;
        return this;
    }

    /**
     * Get branchName
     *
     * @return branchName
     **/
    @ApiModelProperty(value = "")
    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Build defaultBranch(Boolean defaultBranch) {
        this.defaultBranch = defaultBranch;
        return this;
    }

    /**
     * Get defaultBranch
     *
     * @return defaultBranch
     **/
    @ApiModelProperty(value = "")
    public Boolean isDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(Boolean defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public Build unspecifiedBranch(Boolean unspecifiedBranch) {
        this.unspecifiedBranch = unspecifiedBranch;
        return this;
    }

    /**
     * Get unspecifiedBranch
     *
     * @return unspecifiedBranch
     **/
    @ApiModelProperty(value = "")
    public Boolean isUnspecifiedBranch() {
        return unspecifiedBranch;
    }

    public void setUnspecifiedBranch(Boolean unspecifiedBranch) {
        this.unspecifiedBranch = unspecifiedBranch;
    }

    public Build history(Boolean history) {
        this.history = history;
        return this;
    }

    /**
     * Get history
     *
     * @return history
     **/
    @ApiModelProperty(value = "")
    public Boolean isHistory() {
        return history;
    }

    public void setHistory(Boolean history) {
        this.history = history;
    }

    public Build pinned(Boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    /**
     * Get pinned
     *
     * @return pinned
     **/
    @ApiModelProperty(value = "")
    public Boolean isPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public Build href(String href) {
        this.href = href;
        return this;
    }

    /**
     * Get href
     *
     * @return href
     **/
    @ApiModelProperty(value = "")
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Build webUrl(String webUrl) {
        this.webUrl = webUrl;
        return this;
    }

    /**
     * Get webUrl
     *
     * @return webUrl
     **/
    @ApiModelProperty(value = "")
    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public Build queuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
        return this;
    }

    /**
     * Get queuePosition
     *
     * @return queuePosition
     **/
    @ApiModelProperty(value = "")
    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }

    public Build limitedChangesCount(Integer limitedChangesCount) {
        this.limitedChangesCount = limitedChangesCount;
        return this;
    }

    /**
     * Get limitedChangesCount
     *
     * @return limitedChangesCount
     **/
    @ApiModelProperty(value = "")
    public Integer getLimitedChangesCount() {
        return limitedChangesCount;
    }

    public void setLimitedChangesCount(Integer limitedChangesCount) {
        this.limitedChangesCount = limitedChangesCount;
    }

    public Build artifactsDirectory(String artifactsDirectory) {
        this.artifactsDirectory = artifactsDirectory;
        return this;
    }

    /**
     * Get artifactsDirectory
     *
     * @return artifactsDirectory
     **/
    @ApiModelProperty(value = "")
    public String getArtifactsDirectory() {
        return artifactsDirectory;
    }

    public void setArtifactsDirectory(String artifactsDirectory) {
        this.artifactsDirectory = artifactsDirectory;
    }

    public Build statusText(String statusText) {
        this.statusText = statusText;
        return this;
    }

    /**
     * Get statusText
     *
     * @return statusText
     **/
    @ApiModelProperty(value = "")
    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Build comment(Comment comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Get comment
     *
     * @return comment
     **/
    @ApiModelProperty(value = "")
    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public Build tags(Tags tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get tags
     *
     * @return tags
     **/
    @ApiModelProperty(value = "")
    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public Build pinInfo(Comment pinInfo) {
        this.pinInfo = pinInfo;
        return this;
    }

    /**
     * Get pinInfo
     *
     * @return pinInfo
     **/
    @ApiModelProperty(value = "")
    public Comment getPinInfo() {
        return pinInfo;
    }

    public void setPinInfo(Comment pinInfo) {
        this.pinInfo = pinInfo;
    }

    public Build startEstimate(String startEstimate) {
        this.startEstimate = startEstimate;
        return this;
    }

    /**
     * Get startEstimate
     *
     * @return startEstimate
     **/
    @ApiModelProperty(value = "")
    public String getStartEstimate() {
        return startEstimate;
    }

    public void setStartEstimate(String startEstimate) {
        this.startEstimate = startEstimate;
    }

    public Build waitReason(String waitReason) {
        this.waitReason = waitReason;
        return this;
    }

    /**
     * Get waitReason
     *
     * @return waitReason
     **/
    @ApiModelProperty(value = "")
    public String getWaitReason() {
        return waitReason;
    }

    public void setWaitReason(String waitReason) {
        this.waitReason = waitReason;
    }

    public Build canceledInfo(Comment canceledInfo) {
        this.canceledInfo = canceledInfo;
        return this;
    }

    /**
     * Get canceledInfo
     *
     * @return canceledInfo
     **/
    @ApiModelProperty(value = "")
    public Comment getCanceledInfo() {
        return canceledInfo;
    }

    public void setCanceledInfo(Comment canceledInfo) {
        this.canceledInfo = canceledInfo;
    }

    public Build queuedDate(String queuedDate) {
        this.queuedDate = queuedDate;
        return this;
    }

    /**
     * Get queuedDate
     *
     * @return queuedDate
     **/
    @ApiModelProperty(value = "")
    public String getQueuedDate() {
        return queuedDate;
    }

    public void setQueuedDate(String queuedDate) {
        this.queuedDate = queuedDate;
    }

    public Build startDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * Get startDate
     *
     * @return startDate
     **/
    @ApiModelProperty(value = "")
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Build finishDate(String finishDate) {
        this.finishDate = finishDate;
        return this;
    }

    /**
     * Get finishDate
     *
     * @return finishDate
     **/
    @ApiModelProperty(value = "")
    public String getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(String finishDate) {
        this.finishDate = finishDate;
    }

    public Build agent(Agent agent) {
        this.agent = agent;
        return this;
    }

    /**
     * Get agent
     *
     * @return agent
     **/
    @ApiModelProperty(value = "")
    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Build compatibleAgents(Agents compatibleAgents) {
        this.compatibleAgents = compatibleAgents;
        return this;
    }

    /**
     * Get compatibleAgents
     *
     * @return compatibleAgents
     **/
    @ApiModelProperty(value = "")
    public Agents getCompatibleAgents() {
        return compatibleAgents;
    }

    public void setCompatibleAgents(Agents compatibleAgents) {
        this.compatibleAgents = compatibleAgents;
    }

    public Build properties(Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get properties
     *
     * @return properties
     **/
    @ApiModelProperty(value = "")
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Build resultingProperties(Properties resultingProperties) {
        this.resultingProperties = resultingProperties;
        return this;
    }

    /**
     * Get resultingProperties
     *
     * @return resultingProperties
     **/
    @ApiModelProperty(value = "")
    public Properties getResultingProperties() {
        return resultingProperties;
    }

    public void setResultingProperties(Properties resultingProperties) {
        this.resultingProperties = resultingProperties;
    }

    public Build statistics(Properties statistics) {
        this.statistics = statistics;
        return this;
    }

    /**
     * Get statistics
     *
     * @return statistics
     **/
    @ApiModelProperty(value = "")
    public Properties getStatistics() {
        return statistics;
    }

    public void setStatistics(Properties statistics) {
        this.statistics = statistics;
    }

    public Build settingsHash(String settingsHash) {
        this.settingsHash = settingsHash;
        return this;
    }

    /**
     * Get settingsHash
     *
     * @return settingsHash
     **/
    @ApiModelProperty(value = "")
    public String getSettingsHash() {
        return settingsHash;
    }

    public void setSettingsHash(String settingsHash) {
        this.settingsHash = settingsHash;
    }

    public Build currentSettingsHash(String currentSettingsHash) {
        this.currentSettingsHash = currentSettingsHash;
        return this;
    }

    /**
     * Get currentSettingsHash
     *
     * @return currentSettingsHash
     **/
    @ApiModelProperty(value = "")
    public String getCurrentSettingsHash() {
        return currentSettingsHash;
    }

    public void setCurrentSettingsHash(String currentSettingsHash) {
        this.currentSettingsHash = currentSettingsHash;
    }

    public Build modificationId(String modificationId) {
        this.modificationId = modificationId;
        return this;
    }

    /**
     * Get modificationId
     *
     * @return modificationId
     **/
    @ApiModelProperty(value = "")
    public String getModificationId() {
        return modificationId;
    }

    public void setModificationId(String modificationId) {
        this.modificationId = modificationId;
    }

    public Build chainModificationId(String chainModificationId) {
        this.chainModificationId = chainModificationId;
        return this;
    }

    /**
     * Get chainModificationId
     *
     * @return chainModificationId
     **/
    @ApiModelProperty(value = "")
    public String getChainModificationId() {
        return chainModificationId;
    }

    public void setChainModificationId(String chainModificationId) {
        this.chainModificationId = chainModificationId;
    }

    public Build usedByOtherBuilds(Boolean usedByOtherBuilds) {
        this.usedByOtherBuilds = usedByOtherBuilds;
        return this;
    }

    /**
     * Get usedByOtherBuilds
     *
     * @return usedByOtherBuilds
     **/
    @ApiModelProperty(value = "")
    public Boolean isUsedByOtherBuilds() {
        return usedByOtherBuilds;
    }

    public void setUsedByOtherBuilds(Boolean usedByOtherBuilds) {
        this.usedByOtherBuilds = usedByOtherBuilds;
    }

    public Build statusChangeComment(Comment statusChangeComment) {
        this.statusChangeComment = statusChangeComment;
        return this;
    }

    /**
     * Get statusChangeComment
     *
     * @return statusChangeComment
     **/
    @ApiModelProperty(value = "")
    public Comment getStatusChangeComment() {
        return statusChangeComment;
    }

    public void setStatusChangeComment(Comment statusChangeComment) {
        this.statusChangeComment = statusChangeComment;
    }

    public Build locator(String locator) {
        this.locator = locator;
        return this;
    }

    /**
     * Get locator
     *
     * @return locator
     **/
    @ApiModelProperty(value = "")
    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Build build = (Build) o;
        return Objects.equals(this.id, build.id) &&
                Objects.equals(this.taskId, build.taskId) &&
                Objects.equals(this.buildTypeId, build.buildTypeId) &&
                Objects.equals(this.buildTypeInternalId, build.buildTypeInternalId) &&
                Objects.equals(this.number, build.number) &&
                Objects.equals(this.status, build.status) &&
                Objects.equals(this.state, build.state) &&
                Objects.equals(this.running, build.running) &&
                Objects.equals(this.composite, build.composite) &&
                Objects.equals(this.failedToStart, build.failedToStart) &&
                Objects.equals(this.personal, build.personal) &&
                Objects.equals(this.percentageComplete, build.percentageComplete) &&
                Objects.equals(this.branchName, build.branchName) &&
                Objects.equals(this.defaultBranch, build.defaultBranch) &&
                Objects.equals(this.unspecifiedBranch, build.unspecifiedBranch) &&
                Objects.equals(this.history, build.history) &&
                Objects.equals(this.pinned, build.pinned) &&
                Objects.equals(this.href, build.href) &&
                Objects.equals(this.webUrl, build.webUrl) &&
                Objects.equals(this.queuePosition, build.queuePosition) &&
                Objects.equals(this.limitedChangesCount, build.limitedChangesCount) &&
                Objects.equals(this.artifactsDirectory, build.artifactsDirectory) &&
                Objects.equals(this.statusText, build.statusText) &&
                Objects.equals(this.comment, build.comment) &&
                Objects.equals(this.tags, build.tags) &&
                Objects.equals(this.pinInfo, build.pinInfo) &&
                Objects.equals(this.startEstimate, build.startEstimate) &&
                Objects.equals(this.waitReason, build.waitReason) &&
                Objects.equals(this.canceledInfo, build.canceledInfo) &&
                Objects.equals(this.queuedDate, build.queuedDate) &&
                Objects.equals(this.startDate, build.startDate) &&
                Objects.equals(this.finishDate, build.finishDate) &&
                Objects.equals(this.agent, build.agent) &&
                Objects.equals(this.compatibleAgents, build.compatibleAgents) &&
                Objects.equals(this.properties, build.properties) &&
                Objects.equals(this.resultingProperties, build.resultingProperties) &&
                Objects.equals(this.statistics, build.statistics) &&
                Objects.equals(this.settingsHash, build.settingsHash) &&
                Objects.equals(this.currentSettingsHash, build.currentSettingsHash) &&
                Objects.equals(this.modificationId, build.modificationId) &&
                Objects.equals(this.chainModificationId, build.chainModificationId) &&
                Objects.equals(this.usedByOtherBuilds, build.usedByOtherBuilds) &&
                Objects.equals(this.statusChangeComment, build.statusChangeComment) &&
                Objects.equals(this.locator, build.locator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, taskId, buildTypeId, buildTypeInternalId, number, status, state, running, composite,
                failedToStart, personal, percentageComplete, branchName, defaultBranch, unspecifiedBranch, history,
                pinned, href, webUrl, queuePosition, limitedChangesCount, artifactsDirectory, statusText, comment, tags,
                pinInfo, startEstimate, waitReason, canceledInfo, queuedDate, startDate, finishDate, agent,
                compatibleAgents, properties, resultingProperties, statistics, settingsHash, currentSettingsHash,
                modificationId, chainModificationId, usedByOtherBuilds, statusChangeComment, locator);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Build {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    taskId: ").append(toIndentedString(taskId)).append("\n");
        sb.append("    buildTypeId: ").append(toIndentedString(buildTypeId)).append("\n");
        sb.append("    buildTypeInternalId: ").append(toIndentedString(buildTypeInternalId)).append("\n");
        sb.append("    number: ").append(toIndentedString(number)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    running: ").append(toIndentedString(running)).append("\n");
        sb.append("    composite: ").append(toIndentedString(composite)).append("\n");
        sb.append("    failedToStart: ").append(toIndentedString(failedToStart)).append("\n");
        sb.append("    personal: ").append(toIndentedString(personal)).append("\n");
        sb.append("    percentageComplete: ").append(toIndentedString(percentageComplete)).append("\n");
        sb.append("    branchName: ").append(toIndentedString(branchName)).append("\n");
        sb.append("    defaultBranch: ").append(toIndentedString(defaultBranch)).append("\n");
        sb.append("    unspecifiedBranch: ").append(toIndentedString(unspecifiedBranch)).append("\n");
        sb.append("    history: ").append(toIndentedString(history)).append("\n");
        sb.append("    pinned: ").append(toIndentedString(pinned)).append("\n");
        sb.append("    href: ").append(toIndentedString(href)).append("\n");
        sb.append("    webUrl: ").append(toIndentedString(webUrl)).append("\n");
        sb.append("    queuePosition: ").append(toIndentedString(queuePosition)).append("\n");
        sb.append("    limitedChangesCount: ").append(toIndentedString(limitedChangesCount)).append("\n");
        sb.append("    artifactsDirectory: ").append(toIndentedString(artifactsDirectory)).append("\n");
        sb.append("    statusText: ").append(toIndentedString(statusText)).append("\n");
        sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
        sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
        sb.append("    pinInfo: ").append(toIndentedString(pinInfo)).append("\n");
        sb.append("    startEstimate: ").append(toIndentedString(startEstimate)).append("\n");
        sb.append("    waitReason: ").append(toIndentedString(waitReason)).append("\n");
        sb.append("    canceledInfo: ").append(toIndentedString(canceledInfo)).append("\n");
        sb.append("    queuedDate: ").append(toIndentedString(queuedDate)).append("\n");
        sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
        sb.append("    finishDate: ").append(toIndentedString(finishDate)).append("\n");
        sb.append("    agent: ").append(toIndentedString(agent)).append("\n");
        sb.append("    compatibleAgents: ").append(toIndentedString(compatibleAgents)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("    resultingProperties: ").append(toIndentedString(resultingProperties)).append("\n");
        sb.append("    statistics: ").append(toIndentedString(statistics)).append("\n");
        sb.append("    settingsHash: ").append(toIndentedString(settingsHash)).append("\n");
        sb.append("    currentSettingsHash: ").append(toIndentedString(currentSettingsHash)).append("\n");
        sb.append("    modificationId: ").append(toIndentedString(modificationId)).append("\n");
        sb.append("    chainModificationId: ").append(toIndentedString(chainModificationId)).append("\n");
        sb.append("    usedByOtherBuilds: ").append(toIndentedString(usedByOtherBuilds)).append("\n");
        sb.append("    statusChangeComment: ").append(toIndentedString(statusChangeComment)).append("\n");
        sb.append("    locator: ").append(toIndentedString(locator)).append("\n");
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
