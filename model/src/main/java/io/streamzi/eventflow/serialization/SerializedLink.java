package io.streamzi.eventflow.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.streamzi.eventflow.model.ProcessorLink;

/**
 * @author hhiden
 */
public class SerializedLink {
    @JsonIgnore
    private ProcessorLink link;

    private String sourceUuid;
    private String targetUuid;
    private String sourcePortName;
    private String targetPortName;

    public SerializedLink() {
    }

    public SerializedLink(ProcessorLink link) {
        setLink(link);
    }

    public void setLink(ProcessorLink link) {
        this.link = link;
        this.sourcePortName = link.getSource().getName();
        this.sourceUuid = link.getSource().getParent().getUuid();
        this.targetPortName = link.getTarget().getName();
        this.targetUuid = link.getTarget().getParent().getUuid();
    }

    public String getSourcePortName() {
        return sourcePortName;
    }

    public void setSourcePortName(String sourcePortName) {
        this.sourcePortName = sourcePortName;
    }

    public String getTargetPortName() {
        return targetPortName;
    }

    public void setTargetPortName(String targetPortName) {
        this.targetPortName = targetPortName;
    }

    public String getSourceUuid() {
        return sourceUuid;
    }

    public void setSourceUuid(String sourceUuid) {
        this.sourceUuid = sourceUuid;
    }

    public String getTargetUuid() {
        return targetUuid;
    }

    public void setTargetUuid(String targetUuid) {
        this.targetUuid = targetUuid;
    }

    @Override
    public String toString() {
        return "SerializedLink{" +
                "link=" + link +
                ", sourceUuid='" + sourceUuid + '\'' +
                ", targetUuid='" + targetUuid + '\'' +
                ", sourcePortName='" + sourcePortName + '\'' +
                ", targetPortName='" + targetPortName + '\'' +
                '}';
    }
}
