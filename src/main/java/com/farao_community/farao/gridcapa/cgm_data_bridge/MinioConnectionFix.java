/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 */
package com.farao_community.farao.gridcapa.cgm_data_bridge;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.integration.aws.support.S3Session;
import org.springframework.integration.aws.support.S3SessionFactory;
import org.springframework.util.Assert;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
final class MinioConnectionFix {
    private MinioConnectionFix() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    public static S3SessionFactory getSessionFactory(AmazonS3 amazonS3) {
        return new MinioFixedS3SessionFactory(amazonS3);
    }
}

class MinioFixedS3SessionFactory extends S3SessionFactory {

    private final MinioFixedS3Session s3Session;

    @Override
    public S3Session getSession() {
        return s3Session;
    }

    public MinioFixedS3SessionFactory(AmazonS3 amazonS3) {
        super(amazonS3);
        Assert.notNull(amazonS3, "'amazonS3' must not be null.");
        this.s3Session = new MinioFixedS3Session(amazonS3);
    }
}

class MinioFixedS3Session extends S3Session {

    public MinioFixedS3Session(AmazonS3 amazonS3) {
        super(amazonS3);
    }

    @Override
    public String getHostPort() {
        return "";
    }
}
