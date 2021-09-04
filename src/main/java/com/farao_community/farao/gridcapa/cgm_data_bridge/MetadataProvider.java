/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.gridcapa.cgm_data_bridge;

import org.springframework.messaging.Message;

import java.util.Map;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface MetadataProvider {
    void populateMetadata(Message<?> message, Map<String, String> metadata);
}
