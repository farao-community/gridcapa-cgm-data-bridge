/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.gridcapa.cgm_data_bridge;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Configuration
public class FtpSource {
    public static final String SYNCHRONIZE_TEMP_DIRECTORY_PREFIX = "gridcapa-cgm-data-bridge";

    private final ApplicationContext applicationContext;

    @Value("${cgm-data-bridge.sources.ftp.host}")
    private String host;
    @Value("${cgm-data-bridge.sources.ftp.port}")
    private int port;
    @Value("${cgm-data-bridge.sources.ftp.username}")
    private String username;
    @Value("${cgm-data-bridge.sources.ftp.password}")
    private String password;
    @Value("${cgm-data-bridge.sources.ftp.base-directory}")
    private String baseDirectory;

    public FtpSource(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public MessageChannel ftpSourceChannel() {
        return new PublishSubscribeChannel();
    }

    private SessionFactory<FTPFile> ftpSessionFactory() {
        DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
        sf.setHost(host);
        sf.setPort(port);
        sf.setUsername(username);
        sf.setPassword(password);
        sf.setClientMode(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE);
        return sf;
    }

    public FtpInboundFileSynchronizer ftpInboundFileSynchronizer() {
        FtpInboundFileSynchronizer fileSynchronizer = new FtpInboundFileSynchronizer(ftpSessionFactory());
        fileSynchronizer.setDeleteRemoteFiles(false);
        fileSynchronizer.setBeanFactory(applicationContext);
        fileSynchronizer.setRemoteDirectory(baseDirectory);
        fileSynchronizer.setFilter(Arrays::asList);
        return fileSynchronizer;
    }

    @Bean
    @InboundChannelAdapter(channel = "ftpSourceChannel", poller = @Poller(fixedDelay = "${cgm-data-bridge.sources.ftp.polling-delay-in-ms}"))
    public MessageSource<File> ftpMessageSource() throws IOException {
        FtpInboundFileSynchronizingMessageSource source =
                new FtpInboundFileSynchronizingMessageSource(ftpInboundFileSynchronizer());
        source.setLocalDirectory(Files.createTempDirectory(SYNCHRONIZE_TEMP_DIRECTORY_PREFIX).toFile());
        source.setAutoCreateLocalDirectory(true);
        return source;
    }

    @Bean
    public IntegrationFlow ftpCgmPreprocessFlow() {
        return IntegrationFlows.from("ftpSourceChannel")
                .channel("cgmArchivesChannel")
                .get();
    }
}
