/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import java.util.Objects;
import org.opendaylight.yangtools.yang.data.api.codec.Uint32Codec;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

final class Uint32StringCodec extends AbstractIntegerStringCodec<Long, UnsignedIntegerTypeDefinition> implements
        Uint32Codec<String> {

    Uint32StringCodec(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
        super(typeDef, extractRange(typeDef.orNull()), Long.class);
    }

    @Override
    Long deserialize(final String stringRepresentation, final int base) {
        return Long.valueOf(stringRepresentation, base);
    }

    @Override
    public String serialize(final Long data) {
        return Objects.toString(data, "");
    }

    @Override
    Long convertValue(final Number value) {
        return value.longValue();
    }
}
