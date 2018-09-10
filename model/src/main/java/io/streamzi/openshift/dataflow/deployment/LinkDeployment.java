/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamzi.openshift.dataflow.deployment;

import io.streamzi.openshift.dataflow.model.ProcessorLink;

/**
 * This class contains a link to be deployed
 * @author hhiden
 */
public class LinkDeployment {
    public enum LinkType {
        INTERNAL_LINK,
        FROM_REMOTE_CLOUD,
        TO_REMOTE_CLOUD,
        PROXY_TO_REMOTE_CLOUD,
        PROXY_FROM_REMOTE_CLOUD
    }
    
    private ProcessorLink link;
    private LinkType type;
    private String sourceCloudId;
    private String targetCloudId;
    
    public LinkDeployment(ProcessorLink link, LinkType type, String sourceCloudId, String targetCloudId) {
        this.link = link;
        this.type = type;
        this.sourceCloudId = sourceCloudId;
        this.targetCloudId = targetCloudId;
    }

    public ProcessorLink getLink() {
        return link;
    }

    public LinkType getType() {
        return type;
    }

    public String getSourceCloudId() {
        return sourceCloudId;
    }

    public String getTargetCloudId() {
        return targetCloudId;
    }

    
    @Override
    public String toString() {
        switch(type){
            case INTERNAL_LINK:
                return "Internal link from: " + link.getSource().getParent().getDisplayName() + " to: " + link.getTarget().getParent().getDisplayName() + "{" + getTopicName() + "}";
                
            case FROM_REMOTE_CLOUD:
                return "Link from remote cloud: [" + sourceCloudId + "]" + link.getSource().getParent().getDisplayName() + " to: [" + targetCloudId + "] " + link.getTarget().getParent().getDisplayName() + "{" + getTopicName() + "}";
                        
            case TO_REMOTE_CLOUD:
                return "Link from: [" + sourceCloudId + "] " + link.getSource().getParent().getDisplayName() + " to: [" + targetCloudId + "]" + link.getTarget().getParent().getDisplayName() + "{" + getTopicName() + "}";
                        
            case PROXY_TO_REMOTE_CLOUD:
                return "Outbound proxy for: " + link.getSource().getParent().getDisplayName() + " output: " + link.getTarget().getName() + "{" + getTopicName() + "}";
                
            case PROXY_FROM_REMOTE_CLOUD:
                return "Inbound proxy for: " + link.getTarget().getParent().getDisplayName() + " input: " + link.getTarget().getName() + "{" + getTopicName() + "}";
                
            default:
                return "UNDEFINED LINK";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof LinkDeployment){
            LinkDeployment other = (LinkDeployment)obj;
            
            if(other.getType()!=type){
                // Different type
                return false;
            } else {
                // Same type
                if(other.getSourceCloudId().equals(sourceCloudId) && other.getTargetCloudId().equals(targetCloudId)){
                    // Same source / target cloud
                    return true;
                    
                } else {
                    return false;
                }
            }
                    
                    
        } else {
            return false;
        }
    }

    public String getTopicName(){
        String flowName = link.getSource().getParent().getParent().getName();
        return flowName + "-" + link.getSource().getParent().getUuid() + "-" + link.getSource().getName();
    }
}