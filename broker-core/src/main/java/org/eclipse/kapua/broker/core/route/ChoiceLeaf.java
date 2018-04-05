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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.eclipse.kapua.broker.core.router.PlaceholderReplacer;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "choiceLeaf")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {
        "condition",
        "choiceList",
        "otherwise"
})
public class ChoiceLeaf implements Choice {

    private final static Logger logger = LoggerFactory.getLogger(ChoiceLeaf.class);

    private String condition;
    private List<Choice> choiceList;
    private Route otherwise;

    public ChoiceLeaf() {
    }

    @XmlAttribute
    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = PlaceholderReplacer.replacePlaceholder(condition);
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
        if (pd instanceof ChoiceDefinition) {
            ProcessorDefinition<ChoiceDefinition> whenChoiceDefinition = ((ChoiceDefinition) pd).when().simple(condition);
            for (Choice choice : choiceList) {
                if (choice instanceof Endpoint) {
                    try {
                        org.apache.camel.Endpoint ep = ((Endpoint) choice).asEndpoint(camelContext);
                        whenChoiceDefinition.to(ep);
                    } catch (UnsupportedOperationException e) {
                        logger.info("Cannot get {} as Endpoint. Try to get it as Uri", ((Endpoint) choice));
                        whenChoiceDefinition.to(((Endpoint) choice).asUriEndpoint(camelContext));
                    }
                } else {
                    choice.appendRouteDefinition(((ChoiceDefinition) pd), camelContext);
                }
            }
            ((ChoiceDefinition) pd).endChoice();
            whenChoiceDefinition.end();
            if (otherwise != null) {
                ((ChoiceDefinition) pd).otherwise();
                otherwise.appendRouteDefinition(((ChoiceDefinition) pd), camelContext);
            }
        }
        else {
            throw new UnsupportedOperationException(String.format("Unsupported ProcessDefinition [%s]... Only ChoiceDefinition is allowed", this.getClass()));
        }
    }

    public void toLog(StringBuffer buffer, String prefix) {
        buffer.append(prefix);
        buffer.append("StepChoiceWhen - condition: ");
        buffer.append(condition);
        buffer.append("\n");
        prefix += "\t";
        for (Choice choice : choiceList) {
            buffer.append(prefix);
            choice.toLog(buffer, prefix);
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
