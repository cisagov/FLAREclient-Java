package gov.dhs.cisa.ctm.flare.client.api.domain.collection;

import java.net.URI;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.taxii2.resources.Collection;

/**
 * TAXII 2.1 implementation of a TaxiiCollection
 */
@Document(collection = Constants.RepositoryLabels.TAXII_COLLECTION)
public class Taxii21Collection extends TaxiiCollection {

	private static final Logger log = LoggerFactory.getLogger(Taxii21Collection.class);

	@Field("collection_object")
	private Collection collectionObject;

	/**
	 * The API endpoint associated with this collection, e.g. '/api1/'
	 */
	@Field("api_root_ref")
	private String apiRootRef;

	@Field("api_url")
	private URI apiUrl;

	public Taxii21Collection(Collection collectionObject, String serverRef, String apiRootRef) {
		this.collectionObject = collectionObject;
		this.setServerRef(serverRef);
		this.apiRootRef = apiRootRef;
		this.setDisplayName(collectionObject.getTitle());
		this.setTaxiiVersion(Constants.TaxiiVersion.TAXII21);
	}

	public Collection getCollectionObject() {
		return collectionObject;
	}

	public void setCollectionObject(Collection collectionObject) {
		this.collectionObject = collectionObject;
	}

	public String getApiRootRef() {
		return apiRootRef;
	}

	public void setApiRootRef(String apiRootRef) {
		this.apiRootRef = apiRootRef;
	}

	public URI getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(URI apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		Taxii21Collection that = (Taxii21Collection) o;
		return Objects.equals(collectionObject, that.collectionObject);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), collectionObject);
	}

	@Override
	public String toString() {
		return "Taxii21Collection{" + "collectionObject=" + collectionObject + ", apiRootRef='" + apiRootRef + '\''
				+ ", apiUrl=" + apiUrl + "} " + super.toString();
	}
}
