/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.cnsn.serializer;

import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.ChoiceNodeBaseSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;

import com.google.common.base.Preconditions;

public class ChoiceNodeCnSnSerializer
        extends
        ChoiceNodeBaseSerializer<Node<?>> {

    private final NodeSerializerDispatcher<Node<?>> dispatcher;

    ChoiceNodeCnSnSerializer(final NodeSerializerDispatcher<Node<?>> dispatcher) {
        this.dispatcher = Preconditions.checkNotNull(dispatcher);
    }

    @Override
    protected NodeSerializerDispatcher<Node<?>> getNodeDispatcher() {
        return dispatcher;
    }
}
