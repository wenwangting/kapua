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

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountFactory;
import org.eclipse.kapua.service.account.AccountListResult;
import org.eclipse.kapua.service.account.AccountQuery;
import org.eclipse.kapua.service.account.AccountService;

@XmlRootElement(name = "iterator")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {
        "id",
        "type",
        "routeList"
})
public class BrickHandlerIterator implements BrickHandler {

    enum ITERATION_TYPE {
        ACCOUNT
    }

    private AccountFactory accountFactory = KapuaLocator.getInstance().getFactory(AccountFactory.class);
    private AccountService accountService = KapuaLocator.getInstance().getService(AccountService.class);

    private List<Brick> routeList;

    private String id;
    private ITERATION_TYPE type;

    @Override
    @XmlAttribute
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute
    public ITERATION_TYPE getType() {
        return type;
    }

    public void setType(ITERATION_TYPE type) {
        this.type = type;
    }

    @XmlAnyElement(lax = true)
    @XmlElementWrapper(name = "routeList")
    public List<Brick> getRouteList() {
        return routeList;
    }

    public void setRouteList(List<Brick> routeList) {
        this.routeList = routeList;
    }

    @Override
    public void appendBrickDefinition(ProcessorDefinition<?> processorDefinition, CamelContext camelContext, Map<String, Object> ac) throws KapuaException, UnsupportedOperationException {
        switch (type) {
        case ACCOUNT:
            AccountQuery query = accountFactory.newQuery(null);
            AccountListResult accountList = accountService.query(query);
            for (Account account : accountList.getItems()) {
                // override account property
                ac.put(ITERATION_TYPE.ACCOUNT.name(), account.getName());
                for (Brick route : routeList) {
                    route.appendBrickDefinition(processorDefinition, camelContext, ac);
                }
            }
            break;
        default:
            throw new UnsupportedOperationException(String.format("Unsupported type: %s", type));
        }
    }

    @Override
    public void toLog(StringBuffer buffer, String prefix) {

    }

}
