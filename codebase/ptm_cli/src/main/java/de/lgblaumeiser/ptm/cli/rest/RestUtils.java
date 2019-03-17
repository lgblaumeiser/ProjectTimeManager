/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.rest;

import static de.lgblaumeiser.ptm.cli.Utils.assertState;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.lgblaumeiser.ptm.cli.engine.UserStore.UserInfo;

/**
 * Utils to do rest calls on the rest api
 */
public class RestUtils {
	private static final int TIMEOUT = 5 * 1000; // 5 times 1000 msec

	private CloseableHttpClient clientConnector;
	private String baseUrl;

	private ObjectMapper jsonMapper;

	private Properties applicationProps;

	/**
	 * Post a call to the rest api. Expects that a numerical id is returned as part
	 * of the creation call.
	 * 
	 * @param apiName  Name of the api
	 * @param user     Either the user information for authentication or empty, if
	 *                 information does not need authorization
	 * @param bodyData Body of the post data, this is a flat map that is converted
	 *                 into a flat json
	 * @return The Id of the created or manipulated object
	 */
	public Long post(final String apiName, final Optional<UserInfo> user, final Map<String, String> bodyData) {
		try {
			final HttpPost request = new HttpPost(baseUrl + apiName);
			StringEntity bodyJson = new StringEntity(jsonMapper.writeValueAsString(bodyData), "UTF-8");
			bodyJson.setContentType("application/json");
			bodyJson.setContentEncoding("UTF-8");
			request.setEntity(bodyJson);
			request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
			user.ifPresent(u -> request.setHeader(HttpHeaders.AUTHORIZATION,
					"Basic " + encodeBase64String((u.getUsername() + ":" + u.getPassword()).getBytes())));
			HttpResponse response = clientConnector.execute(request);
			assertState(
					response.getStatusLine().getStatusCode() == 201 || response.getStatusLine().getStatusCode() == 200,
					response);
			String uri = apiName;
			if (response.getStatusLine().getStatusCode() == 201) {
				uri = response.getHeaders("Location")[0].getValue();
			}
			return Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public String put(final String apiName, final Optional<UserInfo> user, final Map<String, String> bodyData) {
		try {
			final HttpPut request = new HttpPut(baseUrl + apiName);
			StringEntity bodyJson = new StringEntity(jsonMapper.writeValueAsString(bodyData), "UTF-8");
			bodyJson.setContentType("application/json");
			bodyJson.setContentEncoding("UTF-8");
			request.setEntity(bodyJson);
			request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
			user.ifPresent(u -> request.setHeader(HttpHeaders.AUTHORIZATION,
					"Basic " + encodeBase64String((u.getUsername() + ":" + u.getPassword()).getBytes())));
			HttpResponse response = clientConnector.execute(request);
			assertState(response.getStatusLine().getStatusCode() == 200, response);
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

	}

	/**
	 * A put call to send a zipped data stream to the server
	 * 
	 * @param apiName  Name of the api
	 * @param user     User information if needed to fulfil the request
	 * @param sendData The data to be send to the server
	 */
	public void put(final String apiName, final Optional<UserInfo> user, final byte[] sendData) {
		try {
			final HttpPut request = new HttpPut(baseUrl + apiName);
			ByteArrayEntity bodyData = new ByteArrayEntity(sendData);
			bodyData.setContentType("application/zip");
			request.setEntity(bodyData);
			request.setHeader(HttpHeaders.CONTENT_TYPE, "application/zip");
			user.ifPresent(u -> request.setHeader(HttpHeaders.AUTHORIZATION,
					"Basic " + encodeBase64String((u.getUsername() + ":" + u.getPassword()).getBytes())));
			HttpResponse response = clientConnector.execute(request);
			assertState(response.getStatusLine().getStatusCode() == 200, response);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Returns an element or an array of elements depending on the returnClass
	 * 
	 * @param apiName     The api name of the get call
	 * @param user        User information if needed to fulfil the request
	 * @param returnClass The class object of a result type
	 * @return The found element or array of elements
	 */
	public <T> T get(final String apiName, final Optional<UserInfo> user, final Class<T> returnClass) {
		try {
			final HttpGet request = new HttpGet(baseUrl + apiName);
			request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
			user.ifPresent(u -> request.setHeader(HttpHeaders.AUTHORIZATION,
					"Basic " + encodeBase64String((u.getUsername() + ":" + u.getPassword()).getBytes())));
			HttpResponse response = clientConnector.execute(request);
			assertState(response.getStatusLine().getStatusCode() == 200, response);
			return jsonMapper.readValue(new InputStreamReader(response.getEntity().getContent()), returnClass);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

	}

	/**
	 * Return access to the input stream for a get call with a stream return value
	 * 
	 * @param apiName The api name of the get call
	 * @param user    User information if needed to fulfil the request
	 * @return The input stream delivered by the server
	 */
	public InputStream get(final String apiName, Optional<UserInfo> user) {
		try {
			final HttpGet request = new HttpGet(baseUrl + apiName);
			request.setHeader(HttpHeaders.CONTENT_TYPE, "application/zip");
			user.ifPresent(u -> request.setHeader(HttpHeaders.AUTHORIZATION,
					"Basic " + encodeBase64String((u.getUsername() + ":" + u.getPassword()).getBytes())));
			HttpResponse response = clientConnector.execute(request);
			assertState(response.getStatusLine().getStatusCode() == 200, response);
			return response.getEntity().getContent();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Delete an entity via a rest call
	 * 
	 * @param apiName The api name for the deletion
	 * @param user    User information if needed to fulfil the request
	 */
	public void delete(final String apiName, final Optional<UserInfo> user) {
		try {
			final String requestString = baseUrl + apiName;
			final HttpDelete request = new HttpDelete(requestString);
			request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
			user.ifPresent(u -> request.setHeader(HttpHeaders.AUTHORIZATION,
					"Basic " + encodeBase64String((u.getUsername() + ":" + u.getPassword()).getBytes())));
			HttpResponse response = clientConnector.execute(request);
			assertState(response.getStatusLine().getStatusCode() == 200, response);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Constructor, creates the HTTP Client object to execute http rest requests
	 */
	public RestUtils configure() {
		final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT)
				.setConnectionRequestTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();
		clientConnector = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		applicationProps = loadAppProps();
		String host = getProperty("ptm.host");
		String port = getProperty("ptm.port");
		baseUrl = "http://" + host + ":" + port;
		jsonMapper = new ObjectMapper();
		jsonMapper.registerModule(new JavaTimeModule());
		return this;
	}

	private Properties loadAppProps() {
		Properties applicationProps = new Properties();
		try (InputStream in = getClass().getResourceAsStream("rest.properties")) {
			applicationProps.load(in);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return applicationProps;
	}

	private String getProperty(final String key) {
		String prop = System.getenv(key);
		prop = (prop == null) ? System.getProperty(key) : prop;
		prop = (prop == null) ? applicationProps.getProperty(key) : prop;
		assertState(prop != null, key + " has no value given");
		return prop;
	}
}
