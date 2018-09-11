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


import io.streamzi.openshift.dataflow.model.ProcessorConstants;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.ProcessorLink;
import io.streamzi.openshift.dataflow.model.ProcessorNode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Set of things to deploy to a cloud
 * @author hhiden
 */
public class FlowDeployment {
    private static final Logger logger = Logger.getLogger(FlowDeployment.class.getName());
    
    private String cloud;
    private List<NodeDeployment> nodeDeployments = new ArrayList<>();
    private List<LinkDeployment> linkDeployments = new ArrayList<>();
    
    public FlowDeployment(String cloud, ProcessorFlow flow) {
        this.cloud = cloud;
        buildFromFlow(flow);
    }

    public final void buildFromFlow(ProcessorFlow flow){
        // Add the nodes that are deployed here
        flow.getNodes().stream()
            .filter(node->node.getProcessorType()==ProcessorConstants.ProcessorType.DEPLOYABLE_IMAGE)
            .filter(node->node.getTargetClouds().containsKey(cloud))
            .filter(node->node.getTargetClouds().get(cloud)>0)
            .forEach(node->nodeDeployments.add(new NodeDeployment(node, node.getTargetClouds().get(cloud))));
        

        // Add the links that are not proxied
        flow.getLinks().stream()
            .filter(link->containsNode(link.getSource().getParent()))
            .filter(link->containsNode(link.getTarget().getParent()))
            .forEach(link->linkDeployments.add(new LinkDeployment(link, LinkDeployment.LinkType.INTERNAL_LINK, cloud, cloud)));
                
        // Add Proxies for the links that cannot be left as they are
        flow.getLinks().stream()
            .filter(link->!containsNode(link.getSource().getParent()) || !containsNode(link.getTarget().getParent()))
            .forEach(link->addProxiedLink(link));
                

    }


    
    private void addProxiedLink(ProcessorLink link){
        // Work out which end of the link refers to this cloud
        int sourceDeploymentsHere = getDeploymentsForThisCloud(link.getSource().getParent());
        int targetDeploymentsHere = getDeploymentsForThisCloud(link.getTarget().getParent());
        
        if(sourceDeploymentsHere>0 && targetDeploymentsHere==0){
            // Link starts here and goes to the remote cloud
            linkDeployments.add(new LinkDeployment(link, LinkDeployment.LinkType.REMOTE_LINK, cloud, getFirstNonZeroCloudId(link.getTarget().getParent())));
            
        } else if(sourceDeploymentsHere==0 && targetDeploymentsHere>0){
            // Link comes from outside
            linkDeployments.add(new LinkDeployment(link, LinkDeployment.LinkType.REMOTE_LINK, getFirstNonZeroCloudId(link.getSource().getParent()), cloud));
            
        } else {
            logger.log(Level.SEVERE, "Invalid deployment for link");

        
            
        }
    }
    
    public String getCloud() {
        return cloud;
    }
    
    public String getFirstNonZeroCloudId(ProcessorNode node){
        for(String id : node.getTargetClouds().keySet()){
            if(node.getTargetClouds().get(id)>0){
                return id;
            }
        }
        return "UNKNOWN";
    }
    
    public boolean isDeployedElseWhere(ProcessorNode node){
        for(String id : node.getTargetClouds().keySet()){
            if(!id.equals(cloud) && node.getTargetClouds().get(id)>0){
                return true;
            }
        }
        return false;
    }
    
    public int getDeploymentsForThisCloud(ProcessorNode node){
        if(node.getTargetClouds().containsKey(cloud)){
            return node.getTargetClouds().get(cloud);
        } else {
            return 0;
        }
    }
    
    public boolean containsNode(ProcessorNode node){
        return nodeDeployments.stream().filter(d->d.getNode().equals(node)).count()>0;
    }
    
    public boolean containsLink(LinkDeployment link){
        for(LinkDeployment d : linkDeployments){
            if(d.equals(link)){
                return true;
            }
        } 
        return false;
    }

    public void print(){
        System.out.println("Cloud: " + cloud);
        for(NodeDeployment node : nodeDeployments){
            System.out.println(node);
        }
        for(LinkDeployment link : linkDeployments){
            System.out.println(link);
        }
    }
    
    
}
