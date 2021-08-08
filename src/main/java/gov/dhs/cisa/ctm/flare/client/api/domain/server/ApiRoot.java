package gov.dhs.cisa.ctm.flare.client.api.domain.server;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.api.domain.audit.AbstractAuditingEntity;
import gov.dhs.cisa.ctm.flare.client.api.domain.collection.TaxiiCollection;

@Document(collection = Constants.RepositoryLabels.API_ROOT)
public class ApiRoot extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Field("url")
	private URI url;

	/**
	 * The URL endpoint associated with this API Root, e.g. "/api1/"
	 */
	@Field("endpoint")
	private String endpoint;

	@NotNull
	@Field("object")
	private gov.dhs.cisa.ctm.taxii2.resources.ApiRoot object;

	@DBRef
	@Field("collections")
	private HashSet<TaxiiCollection> collections = new HashSet<>();

	/**
	 * The server's ID, not label, that this API Root belongs to
	 */
	@Field("server_ref")
	private String serverRef;

	private ApiRoot() {
	}

	public ApiRoot(String serverRef) {
		this.serverRef = serverRef;
	}

	public static String getEndpointFromURL(String URL) throws URISyntaxException {
		return new URI(URL).getRawPath();
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public gov.dhs.cisa.ctm.taxii2.resources.ApiRoot getObject() {
		return object;
	}

	public void setObject(gov.dhs.cisa.ctm.taxii2.resources.ApiRoot object) {
		this.object = object;
	}

	public HashSet<TaxiiCollection> getCollections() {
		return collections;
	}

	public void setCollections(HashSet<TaxiiCollection> collections) {
		this.collections = collections;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public URI getUrl() {
		return url;
	}

	public void setUrl(URI url) {
		this.url = url;
	}

	public String getServerRef() {
		return serverRef;
	}

	public void setServerRef(String serverRef) {
		this.serverRef = serverRef;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ApiRoot apiRoot = (ApiRoot) o;
		return Objects.equals(id, apiRoot.id) && Objects.equals(serverRef, apiRoot.serverRef);
	}

	@Override
	public int hashCode() {

		return Objects.hash(id, serverRef);
	}
}
