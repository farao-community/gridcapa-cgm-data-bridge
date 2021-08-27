/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.gridcapa.cgm_data_bridge;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.zip.splitter.UnZipResultSplitter;
import org.springframework.integration.zip.transformer.UnZipTransformer;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Component
public class CgmTransferFlow {

    @Bean
    public MessageChannel cgmArchivesChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel cgmFilesChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow unzipArchivesIntegrationFlow() {
        return IntegrationFlows.from("cgmArchivesChannel")
                .<File, Boolean>route(this::isZip, m -> m
                        .subFlowMapping(false, flow -> flow
                                .channel("cgmFilesChannel")
                        )
                        .subFlowMapping(true, flow -> flow
                                .transform(new UnZipTransformer())
                                .split(new UnZipResultSplitter())
                                .channel("cgmFilesChannel")
                        )
                )
                .get();
    }

    private boolean isZip(File file) {
        return ZipFileDetector.isZip(file);
    }
}
