/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.broker.core.route;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.PipelineDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.eclipse.persistence.oxm.annotations.XmlPath;

@XmlRootElement(name = "choiceRoot")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {
        "id",
        "choiceList",
        "otherwise"
})
public class ChoiceRoot implements Route {

    private String id;
    private List<Choice> choiceList;
    private Route otherwise;

    public ChoiceRoot() {
        choiceList = new ArrayList<>();
    }

    @XmlAttribute
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElementWrapper(name = "choiceList")
    @XmlAnyElement(lax = true)
    public List<Choice> getChoiceList() {
        return choiceList;
    }

    public void setChoiceList(List<Choice> choiceList) {
        this.choiceList = choiceList;
    }

    @XmlPath("otherwise")
    @XmlAnyElement(lax = true)
    public Route getOtherwise() {
        return otherwise;
    }

    public void setOtherwise(Route otherwise) {
        this.otherwise = otherwise;
    }

    @Override
    public void appendRouteDefinition(ProcessorDefinition<?> pd, CamelContext camelContext) {
        ChoiceDefinition cd = null;
        if (pd instanceof RouteDefinition) {
            cd = ((RouteDefinition) pd).choice();
        } else if (pd instanceof ChoiceDefinition) {
            cd = ((ChoiceDefinition) pd);
        } else if (pd instanceof PipelineDefinition) {
            cd = ((PipelineDefinition) pd).choice();
        }
        else {
            throw new UnsupportedOperationException(String.format("Unsupported ProcessDefinition [%s]... Only ChoiceDefinition, PipelineDefinition and RouteDefinition are allowed", this.getClass()));
        }
        appendRouteDefinitionInternal(cd, camelContext);
    }

    private void appendRouteDefinitionInternal(ChoiceDefinition cd, CamelContext camelContext) {
        for (Choice choiceWhen : choiceList) {
            choiceWhen.appendRouteDefinition(cd, camelContext);
        }
        cd.endChoice();
        if (otherwise != null) {
            cd.otherwise();
            otherwise.appendRouteDefinition(cd, camelContext);
        }
        cd.end();
    }

    @Override
    public String getFrom() {
        throw new UnsupportedOperationException(String.format("Operation not allowed for the %s", this.getClass()));
    }

    @Override
    public void toLog(StringBuffer buffer, String prefix) {
        buffer.append(prefix);
        buffer.append("StepChoice - id: ");
        buffer.append(id);
        buffer.append("\n");
        prefix += "\t";
        for (Choice choiceWhen : choiceList) {
            buffer.append(prefix);
            choiceWhen.toLog(buffer, prefix);
            buffer.append("\n");
        }
        if (otherwise != null) {
            buffer.append(prefix);
            buffer.append("Otherwise");
            buffer.append("\n");
            otherwise.toLog(buffer, prefix + " \t");
        }
    }

}
