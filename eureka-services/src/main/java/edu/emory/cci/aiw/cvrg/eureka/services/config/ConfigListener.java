package edu.emory.cci.aiw.cvrg.eureka.services.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;

import edu.emory.cci.aiw.cvrg.eureka.services.thread.JobUpdateTask;

/**
 * Set up the Guice dependency injection engine. Uses two modules:
 * {@link ServletModule} for web related configuration, and {@link AppModule}
 * for non-web related configuration.
 *
 * @author hrathod
 *
 */
public class ConfigListener extends GuiceServletContextListener {

	/**
	 * A timer scheduler to run the job update task.
	 */
	private final ScheduledExecutorService executorService = Executors
			.newSingleThreadScheduledExecutor();
	/**
	 * Make sure we always use the same injector
	 */
	private final Injector injector = Guice.createInjector(new ServletModule(),
			new AppModule(), new JpaPersistModule("services-jpa-unit"));
	/**
	 * The persistence service for the application.
	 */
	private PersistService persistService = null;

	@Override
	protected Injector getInjector() {
		return this.injector;
	}

	/**
	 * The class level logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConfigListener.class);

	/*
	 * @see
	 * com.google.inject.servlet.GuiceServletContextListener#contextInitialized
	 * (javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent inServletContextEvent) {
		super.contextInitialized(inServletContextEvent);
		this.persistService = this.getInjector().getInstance(
				PersistService.class);
		this.persistService.start();

		try {
			ServiceProperties applicationProperties = this.getInjector()
					.getInstance(ServiceProperties.class);
			JobUpdateTask jobUpdateTask = new JobUpdateTask(
					applicationProperties.getEtlJobUpdateUrl());
			this.executorService.scheduleWithFixedDelay(jobUpdateTask, 0, 10,
					TimeUnit.SECONDS);
		} catch (KeyManagementException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/*
	 * @see
	 * com.google.inject.servlet.GuiceServletContextListener#contextDestroyed
	 * (javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent inServletContextEvent) {
		super.contextDestroyed(inServletContextEvent);
		if (this.persistService != null) {
			this.persistService.stop();
		}
		this.executorService.shutdown();
		try {
			this.executorService.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}
