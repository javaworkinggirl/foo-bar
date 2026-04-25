package com.example.it;

import com.example.bar.S3ReaderService;
import com.example.carnival.CarnivalService;
import com.example.common.S3Config;
import com.example.common.S3Properties;
import com.example.foo.S3WriterService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/*
 * Minimal Spring context for the cross-module integration test.
 *
 * CarnivalService depends on S3WriterService (from foo), which depends on
 * S3Client and S3Properties (from common). We import the full chain explicitly
 * rather than relying on component scan so the context is predictable and fast.
 *
 * No web stack — WebEnvironment.NONE in the test class keeps startup under 1s.
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
@Import({S3Config.class, S3WriterService.class, CarnivalService.class, S3ReaderService.class})
public class IntegrationTestConfig {}
