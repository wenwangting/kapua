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
import org.apache.camel.model.RouteDefinition;

@XmlRootElement(name = "baseRoute")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {
        "id",
        "autoStartup",
        "from",
        "multicast",
        "routeList",
        "onExceptionList"
})
public class BaseRoute implements Route {

    private String id;
    private boolean autoStartup;
    private String from;
    private List<Route> routeList;
    private boolean multicast;
    private List<OnException> onExceptionList;

    public BaseRoute() {
        routeList = new ArrayList<>();
    }

    @XmlAttribute
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute
    public boolean isAutoStartup() {
        return autoStartup;
    }

    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    @XmlAttribute
    public boolean isMulticast() {
        return multicast;
    }

    public void setMulticast(boolean multicast) {
        this.multicast = multicast;
    }

    @XmlAttribute
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @XmlAnyElement(lax = true)
    @XmlElementWrapper(name = "routeList")
    public List<Route> getRouteList() {
        return routeList;
    }

    public void setRouteList(List<Route> routeList) {
        this.routeList = routeList;
    }

    @XmlAnyElement(lax = true)
    @XmlElementWrapper(name = "onExceptionList")
    public List<OnException> getOnExceptionList() {
        return onExceptionList;
    }

    public void setOnExceptionList(List<OnException> onExceptionList) {
        this.onExceptionList = onExceptionList;
    }

    @Override
    public void appendRouteDefinition(RouteDefinition routeDefinition, CamelContext camelContext) {
        routeDefinition.setId(id);
        routeDefinition.setAutoStartup(Boolean.toString(autoStartup));
        if (multicast) {
            routeDefinition.multicast();
        }
        for (Route route : routeList) {
            PipelineDefinition pd = routeDefinition.pipeline();
            route.appendRouteDefinition(routeDefinition, camelContext);
            pd.end();
        }
        for (OnException onException : onExceptionList) {
            onException.appendExceptionDefinition(routeDefinition, camelContext);
        }
    }

    @Override
    public void appendRouteDefinition(ChoiceDefinition choiceDefinition, CamelContext camelContext) {
        if (onExceptionList != null && onExceptionList.size() > 0) {
            throw new UnsupportedOperationException(String.format("Operation not allowed for the %s. The subroute cannot have from and/or exception handling set", this.getClass()));
        }
        for (Route route : routeList) {
            route.appendRouteDefinition(choiceDefinition, camelContext);
        }
    }

    @Override
    public void toLog(StringBuffer buffer, String prefix) {
        buffer.append(prefix);  
        buffer.append("Route - id: ");
        buffer.append(id);
        buffer.append(" from: ");
        buffer.append(from);
        buffer.append(" multicast: ");
        buffer.append(multicast);
        buffer.append(" auto startup: ");
        buffer.append(autoStartup);
        buffer.append("\n");
        prefix += "\t";
        for (Route route : routeList) {
            buffer.append(prefix);
            route.toLog(buffer, prefix);
            buffer.append("\n");
        }
        if (onExceptionList != null && onExceptionList.size() > 0) {
            for (OnException onException : onExceptionList) {
                buffer.append(prefix);
                onException.toLog(buffer, prefix);
                buffer.append("\n");
            }
        } else {
            buffer.append(prefix);
            buffer.append("NO configured exception handling");
            buffer.append("\n");
        }
    }

}
