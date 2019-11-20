package com.xpay.starter.comp.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for email support.
 */
@ConfigurationProperties(prefix = "email")
public class MailProperties {
	public List<Mailer> mailers;

	public List<Mailer> getMailers() {
		return mailers;
	}

	public void setMailers(List<Mailer> mailers) {
		this.mailers = mailers;
	}

	public static class Mailer{
		private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

		/**
		 * SMTP server host. For instance, `smtp.example.com`.
		 */
		private String host;

		/**
		 * SMTP server port.
		 */
		private Integer port;

		/**
		 * Login user of the SMTP server.
		 */
		private String username;

		/**
		 * Login password of the SMTP server.
		 */
		private String password;

		/**
		 * Protocol used by the SMTP server.
		 */
		private String protocol = "smtp";

		/**
		 * Default MimeMessage encoding.
		 */
		private Charset defaultEncoding = DEFAULT_CHARSET;

		/**
		 * Additional JavaMail Session properties.
		 */
		private Map<String, String> properties = new HashMap<>();


		public String getHost() {
			return this.host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public Integer getPort() {
			return this.port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		public String getUsername() {
			return this.username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return this.password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getProtocol() {
			return this.protocol;
		}

		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}

		public Charset getDefaultEncoding() {
			return this.defaultEncoding;
		}

		public void setDefaultEncoding(Charset defaultEncoding) {
			this.defaultEncoding = defaultEncoding;
		}

		public void setProperties(Map<String, String> properties) {
			this.properties = properties;
		}

		public Map<String, String> getProperties() {
			return this.properties;
		}
	}
}
