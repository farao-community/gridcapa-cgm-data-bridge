/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.gridcapa.cgm_data_bridge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@SpringBootTest
class CgmMetadataProviderTest {
    @Autowired
    private CgmMetadataProvider cgmMetadataProvider;

    @Test
    void checkMetadataSetCorrectlyWhenUcteFileIsCorrect() {
        Message<?> ucteFileMessage = MessageBuilder
                .withPayload("")
                .setHeader("gridcapa_file_name", "20210101_1430_2D5_CSE1.uct")
                .build();
        Map<String, String> metadataMap = new HashMap<>();
        cgmMetadataProvider.populateMetadata(ucteFileMessage, metadataMap);

        assertEquals("cse-d2cc", metadataMap.get(CgmMetadataProvider.GRIDCAPA_PROCESS_METADATA_KEY));
        assertEquals("CGM", metadataMap.get(CgmMetadataProvider.GRIDCAPA_FILE_TYPE_METADATA_KEY));
        assertEquals("2021-01-01T14:30", metadataMap.get(CgmMetadataProvider.GRIDCAPA_TIMESTAMP_METADATA_KEY));
        assertEquals("UCTE", metadataMap.get(CgmMetadataProvider.GRIDCAPA_FILE_FORMAT_METADATA_KEY));
    }

    @Test
    void checkMetadataSetEmptyWhenIncorrectFile() {
        Message<?> ucteFileMessage = MessageBuilder
                .withPayload("")
                .setHeader("gridcapa_file_name", "test.uct")
                .build();
        Map<String, String> metadataMap = new HashMap<>();
        cgmMetadataProvider.populateMetadata(ucteFileMessage, metadataMap);

        assertEquals("cse-d2cc", metadataMap.get(CgmMetadataProvider.GRIDCAPA_PROCESS_METADATA_KEY));
        assertEquals("CGM", metadataMap.get(CgmMetadataProvider.GRIDCAPA_FILE_TYPE_METADATA_KEY));
        assertEquals("", metadataMap.get(CgmMetadataProvider.GRIDCAPA_TIMESTAMP_METADATA_KEY));
        assertEquals("", metadataMap.get(CgmMetadataProvider.GRIDCAPA_FILE_FORMAT_METADATA_KEY));
    }
}
