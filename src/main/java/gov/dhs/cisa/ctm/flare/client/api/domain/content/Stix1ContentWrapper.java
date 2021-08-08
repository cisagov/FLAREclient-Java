package gov.dhs.cisa.ctm.flare.client.api.domain.content;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Sets;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.taxii.TaxiiAssociation;
import gov.dhs.cisa.ctm.flare.client.taxii.Validation;
import gov.dhs.cisa.ctm.utils.util.DocumentUtils;

/**
 * STIX 1.X implementation of a Content Wrapper
 */
@Document(collection = Constants.RepositoryLabels.CONTENT)
@JsonTypeName("STIX1")
public class Stix1ContentWrapper extends AbstractContentWrapper {

	private static final Logger log = LoggerFactory.getLogger(Stix1ContentWrapper.class);

	@Field("content_object")
	private String contentObject;

	/**
	 * Determines the version of STIX content (1.0, 1.1, 1.0.1, 1.1.1, 1.2)
	 */
	@Field("content_binding")
	private String contentBinding;

	public Stix1ContentWrapper() {
		this.setId(UUID.randomUUID().toString());
	}

	public Stix1ContentWrapper(Object contentObject, TaxiiAssociation taxiiAssociation) {
		this.setId(UUID.randomUUID().toString());
		this.contentObject = contentObjectToString(contentObject);
		this.setAssociation(taxiiAssociation);
		if (this.contentObject == null) {
			this.setValidationResult(
					new ValidationResult(ValidationResult.Status.ERROR, Sets.newHashSet("Content unable to parsed")));
		} else {
			this.setValidationResult(new ValidationResult(ValidationResult.Status.PENDING, null));
		}
	}

	@SuppressWarnings("unused")
	private String hash(String object) {
		try {
			return DatatypeConverter
					.printHexBinary(MessageDigest.getInstance("MD5").digest(this.contentObject.getBytes()))
					.toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public String getContentObject() {
		return contentObject;
	}

	public void setContentObject(Object contentObject) {
		this.contentObject = contentObjectToString(contentObject);
		this.setId(hash(this.contentObject));
	}

	private String contentObjectToString(Object contentObject) {
		String stixContent = null;
		if (contentObject instanceof ElementNSImpl) {
			// NOTE: .getAttribute() returns an empty string, not null. id, version,
			// timestamp may be empty strings.
			this.setContentId(((ElementNSImpl) contentObject).getAttribute("id"));
			this.setContentBinding(((ElementNSImpl) contentObject).getAttribute("version"));
			this.setContentTimestamp(DateTimeFormatter.ISO_DATE_TIME
					.parse(((ElementNSImpl) contentObject).getAttribute("timestamp"), ZonedDateTime::from));
			stixContent = DocumentUtils.nodeToString((ElementNSImpl) contentObject, false).trim();
		} else if (contentObject instanceof String) {
			try {
				org.w3c.dom.Document document = DocumentUtils.stringToDocument((String) contentObject);
				this.setContentId(document.getDocumentElement().getAttributes().getNamedItem("id").getNodeValue());
				this.setContentBinding(
						document.getDocumentElement().getAttributes().getNamedItem("version").getNodeValue());
				this.setContentTimestamp(DateTimeFormatter.ISO_DATE_TIME.parse(
						document.getDocumentElement().getAttributes().getNamedItem("timestamp").getNodeValue(),
						ZonedDateTime::from));
				stixContent = (String) contentObject;
			} catch (IOException | SAXException e) {
				log.error(e.getMessage());
			}
		} else {
			throw new IllegalArgumentException("Content object is not an interpretable XML Document");
		}
		return stixContent;
	}

	public void setContentObject(String contentObject) {
		this.contentObject = contentObject;
	}

	public String getContentBinding() {
		return contentBinding;
	}

	private void setContentBinding(String contentBinding) {
		this.contentBinding = contentBinding;
	}

	@Override
	public ValidationResult validate() {
		HashSet<String> errors = Validation.getInstance().validateAndReturnErrors(this.contentObject,
				this.contentBinding);
		this.getValidationResult()
				.setStatus(errors.isEmpty() ? ValidationResult.Status.VALID : ValidationResult.Status.INVALID);
		this.getValidationResult().setErrors(errors.isEmpty() ? null : errors);
		return this.getValidationResult();
	}
}
