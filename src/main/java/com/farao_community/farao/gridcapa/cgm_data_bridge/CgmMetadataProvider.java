/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.gridcapa.cgm_data_bridge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Component
public class CgmMetadataProvider implements MetadataProvider {
    private static final String UCTE_FILE_NAME_REGEX = "(?<datetime>[0-9]{8}_[0-9]{4})_.*.(uct|UCT)";
    private static final String GRIDCAPA_PROCESS_METADATA_KEY = "gridcapa_process";
    private static final String GRIDCAPA_FILE_TYPE_METADATA_KEY = "gridcapa_file_type";
    private static final String GRIDCAPA_TIMESTAMP_METADATA_KEY = "gridcapa_timestamp";
    private static final String GRIDCAPA_FILE_FORMAT_METADATA_KEY = "gridcapa_file_format";

    @Value("${cgm-data-bridge.target-process}")
    private String targetProcess;

    @Override
    public void populateMetadata(Message<?> message, Map<String, String> metadata) {
        String filename = message.getHeaders().get("gridcapa_file_name", String.class);
        metadata.put(GRIDCAPA_PROCESS_METADATA_KEY, targetProcess);
        metadata.put(GRIDCAPA_FILE_TYPE_METADATA_KEY, "CGM");
        metadata.put(GRIDCAPA_TIMESTAMP_METADATA_KEY, getTimestampFromFileName(filename));
        metadata.put(GRIDCAPA_FILE_FORMAT_METADATA_KEY, getFileFormatFromFileName(filename));
    }

    private String getTimestampFromFileName(String filename) {
        Pattern pattern = Pattern.compile(UCTE_FILE_NAME_REGEX);
        Matcher matcher = pattern.matcher(filename);
        if (matcher.matches()) {
            String datetimeString = matcher.group("datetime");
            return LocalDateTime.parse(datetimeString, DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")).toString();
        } else {
            return "";
        }
    }

    private String getFileFormatFromFileName(String filename) {
        Pattern pattern = Pattern.compile(UCTE_FILE_NAME_REGEX);
        Matcher matcher = pattern.matcher(filename);
        if (matcher.matches()) {
            return "UCTE";
        } else {
            return "";
        }
    }
}
