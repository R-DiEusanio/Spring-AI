package com.artificial.SpringAIDemo.loader;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataIngestion {

    private static final Logger log = LoggerFactory.getLogger(DataIngestion.class);

    private final VectorStore vectorStore;

    public DataIngestion(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Value("classpath:documents/VocabSecrets.pdf")
    private Resource resource;

    @PostConstruct
    void run() {
        log.info("Loading PDF as document");
        storeDocument(resource);
    }

    private void storeDocument(Resource resource) {
        try (InputStream is = resource.getInputStream(); PDDocument pdfDoc = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdfDoc);

            log.info("PDF text loaded, splitting into chunks...");
            List<Document> documents = splitTextToDocuments(text, 1000); // Chunk di 1000 caratteri

            for (Document doc : documents) {
                addResourcePathToDocMetaData(doc, resource);
            }

            log.info("Creating and storing Embeddings from Documents");
            vectorStore.add(documents);
        } catch (Exception e) {
            log.error("Error loading or processing the PDF document", e);
        }
    }

    private List<Document> splitTextToDocuments(String text, int chunkSize) {
        List<Document> docs = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            docs.add(new Document(text.substring(i, end)));
        }
        return docs;
    }

    private void addResourcePathToDocMetaData(Document doc, Resource resource) {
        try {
            doc.getMetadata().put("file_path", resource.getFile().toURI());
        } catch (Exception e) {
            log.error("Error adding file path to document metadata", e);
        }
    }
}