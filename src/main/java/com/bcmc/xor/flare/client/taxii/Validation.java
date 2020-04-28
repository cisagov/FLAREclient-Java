package com.bcmc.xor.flare.client.taxii;

import com.bcmc.xor.flare.client.api.config.ApplicationProperties;
import com.bcmc.xor.flare.client.api.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import xor.flare.utils.util.DocumentUtils;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static xor.flare.utils.util.DocumentUtils.getContentBinding;


public class Validation {

    private static final Logger log = LoggerFactory.getLogger(Validation.class);
    private static Validation instance = null;
    private static final Map<String, Schema> SCHEMAS = new HashMap<>(5, 1f);

    private final ApplicationProperties applicationProperties;

    private Validation(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        log.debug("Schema path: {}", applicationProperties.getSchemasDirectory());
        File schemaDir = new File(applicationProperties.getSchemasDirectory());
        log.debug("Loading schemas from {}", schemaDir.getAbsolutePath());
        for (String contentBinding : applicationProperties.getSupportedContentBindings()) {
            if (!SCHEMAS.containsKey(contentBinding)) {
                addSchemaSet(contentBinding);
            }
        }
    }

    public static void init(ApplicationProperties applicationProperties) {
        instance = new Validation(applicationProperties);
    }

    public static synchronized Validation getInstance() {
        if (instance == null) {
            log.error("Validation instance not properly initialized?");
        }
        return instance;
    }

    private void addSchemaSet(String schemaLabel) {
        String schemaDirectory;
        schemaDirectory = applicationProperties.getSchemasDirectory() + File.separator + schemaLabel;
        log.trace("Add schemas from {}...", schemaDirectory);
        Schema schema = null;
        try {
            schema = loadSchemas(schemaDirectory);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            log.error("Exception when trying to load schemas from directory " + schemaDirectory + " Exception: " + e.getMessage());
            log.error("Stack Trace", e);
        }

        SCHEMAS.put(schemaLabel, schema);
    }

    private Schema loadSchemas(String schemaDirectory)
        throws SAXException, IOException, ParserConfigurationException {

        Map<String, String> prefixSchemaBindings = new HashMap<>(5, 1f);
        Path pathToSchemas = Paths.get(schemaDirectory);
        List<File> schemasAsSources =
            Files.walk(pathToSchemas)
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".xsd"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        String prefix, targetNamespace;
        Document schemaDocument;
        NamedNodeMap attributes;
        Node attribute;

        for (File sourceFile : schemasAsSources) {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false);
            schemaDocument = factory
                .newDocumentBuilder().parse(new FileInputStream(sourceFile.getAbsoluteFile()));

            schemaDocument.getDocumentElement().normalize();

            attributes = schemaDocument.getDocumentElement()
                .getAttributes();

            for (int i = 0; i < attributes.getLength(); i++) {
                attribute = attributes.item(i);

                targetNamespace = schemaDocument.getDocumentElement()
                    .getAttribute("targetNamespace");

                if (attribute.getNodeName().startsWith("xmlns:")
                    && attribute.getNodeValue().equals(targetNamespace)) {

                    prefix = attributes.item(i).getNodeName().split(":")[1];

                    if ((prefixSchemaBindings.containsKey(prefix))
                        && (prefixSchemaBindings.get(prefix).split(schemaDirectory)[1]
                        .startsWith("external"))) {
                        continue;

                    }
                    prefixSchemaBindings.put(prefix, sourceFile.getAbsolutePath());
                }
            }
        }

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Source[] schemas = new Source[prefixSchemaBindings.values().size()];

        int i = 0;
        for (String schemaLocation : prefixSchemaBindings.values()) {
            schemas[i++] = new StreamSource(schemaLocation);
        }

        try {
            return factory.newSchema(schemas); //Returns if all schemas in directory are valid
        } catch (SAXException e) {
            //Point out the exact sources that are invalid
            for (int j = 0; j < i; ++j) {
                try {
                    factory.newSchema(schemas[j]);
                } catch (SAXException specificSourceException) {
                    log.error("Error with schema " + schemas[j].getSystemId() + "\n" + e.getMessage());
                    e = specificSourceException;
                }
            }
            throw e;
        }
    }

    private List<String> getSupportedContentBindings() {
        return this.applicationProperties.getSupportedContentBindings();
    }

    //
    public Map.Entry<String, List<String>> validate(Element stixElement, String contentBinding) {

        if (contentBinding == null) {
            return validate(stixElement);
        }
        List<String> errors = new ArrayList<>(4);
        String id = stixElement.getAttribute("id");
        if (SCHEMAS.get(contentBinding) == null) {
            errors.add("No schemas to validate against for content binding: \"" + contentBinding + "\".");
            return new HashMap.SimpleEntry<>(id, errors);
        }
        Validator validator = SCHEMAS.get(contentBinding).newValidator();
        try {
            validator.validate(new DOMSource(stixElement));
        } catch (SAXException | IOException e) {
            errors.add(e.getMessage());
        }
        return new HashMap.SimpleEntry<>(id, errors);
    }

    public Map.Entry<String, List<String>> validate(Element stixElement) {
        List<String> errors = new ArrayList<>();
        Node versionNode = stixElement.getAttributes().getNamedItem("version");
        String id = stixElement.getAttributes().getNamedItem("id").getNodeValue();
        if (versionNode == null) {
            errors.add("No version specified, cannot validate.");
            return new HashMap.SimpleEntry<>(id, errors);
        }
        String version = versionNode.getNodeValue();
        if (!Constants.ContentBindings.contentBindingMap.containsKey(version)) {
            errors.add(String.format("Unsupported version: %s", version));
            return new HashMap.SimpleEntry<>(id, errors);
        }
        Validator validator = SCHEMAS.get(Constants.ContentBindings.contentBindingMap.get(version)).newValidator();
        try {
            validator.validate(new DOMSource(stixElement));
        } catch (SAXException | IOException e) {
            errors.add(e.getMessage());
        }
        if (!errors.isEmpty()) {
            log.error("Validation errors: {}", String.join(", ", errors));
        }
        return new HashMap.SimpleEntry<>(id, errors);
    }

    public Map.Entry<String, List<String>> validateAndReturnErrors(String stixString) {
        Element element;
        try {
            element = DocumentUtils.stringToDocument(stixString).getDocumentElement();
        } catch (IOException | SAXException e) {
            return new HashMap.SimpleEntry<>("unknown", Collections.singletonList(e.getMessage()));
        }
        String contentBinding = getContentBinding(element);
        return validate(element, contentBinding);
    }

    public HashSet<String> validateAndReturnErrors(String stixString, String version) {
        HashSet<String> errors = new HashSet<>();
        if (stixString == null || stixString.isEmpty() || version == null || version.isEmpty()) {
            errors.add("Null content or version");
            return errors;
        }
        Element element = null;

        if (!getSupportedContentBindings().contains(Constants.ContentBindings.contentBindingMap.get(version))) {
            errors.add("Unsupported content binding: " + version);
            errors.add("Supported content bindings are " + Arrays.toString(getSupportedContentBindings().toArray()));
        }

        try {
            element = DocumentUtils.stringToDocument(stixString).getDocumentElement();
        } catch (IOException | SAXException e) {
            errors.add(e.getMessage());
        }

        if (SCHEMAS.get(Constants.ContentBindings.contentBindingMap.get(version)) == null) {
            errors.add("No schemas to validate against for content binding: " + version);
            log.error("No schemas to validate against for content binding: {}", version);
            errors.add("Supported content bindings are " + Arrays.toString(getSupportedContentBindings().toArray()));
        }

        Validator validator = SCHEMAS.get(Constants.ContentBindings.contentBindingMap.get(version)).newValidator();
        try {
            validator.validate(new DOMSource(element));
        } catch (SAXException | IOException e) {
            errors.add(e.getMessage());
        }

        return errors;
    }

    public static void setInstance(Validation instance) {
        Validation.instance = instance;
    }
}


