/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.cnsn.serializer;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yangtools.yang.data.api.MutableCompositeNode;
import org.opendaylight.yangtools.yang.data.api.MutableNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.NodeFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.MapEntryNodeBaseSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import com.google.common.base.Preconditions;

public class MapEntryNodeCnSnSerializer extends MapEntryNodeBaseSerializer<Node<?>> {

    private NodeSerializerDispatcher<Node<?>> dispatcher;

    MapEntryNodeCnSnSerializer(final NodeSerializerDispatcher<Node<?>> dispatcher) {
        this.dispatcher = Preconditions.checkNotNull(dispatcher);
    }

    public List<Node<?>> serialize(ListSchemaNode schema, MapEntryNode node) {

        MutableCompositeNode mutCompNode = NodeFactory.createMutableCompositeNode(node.getNodeType(), null, null, null,
                null);

        for (Node<?> element : super.serialize(schema, node)) {
            if (element instanceof MutableNode<?>) {
                ((MutableNode<?>) element).setParent(mutCompNode);
            }
            mutCompNode.getValue().add(element);
        }

        List<Node<?>> lst = new ArrayList<>();
        lst.add(mutCompNode);
        return lst;
    }

    @Override
    protected NodeSerializerDispatcher<Node<?>> getNodeDispatcher() {
        return dispatcher;
    }
}
