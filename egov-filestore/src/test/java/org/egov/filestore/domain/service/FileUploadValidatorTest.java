package org.egov.filestore.domain.service;

import static org.junit.Assert.fail;

import org.egov.filestore.domain.exception.InvalidFileUploadException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

public class FileUploadValidatorTest {

    private static final byte[] PDF_MAGIC_BYTES = "%PDF-1.4\n%test pdf content".getBytes();
    private static final byte[] HTML_MARKUP_BYTES = "<html><head><title>t</title></head><body>hi</body></html>".getBytes();

    private FileUploadValidator validator;

    @Before
    public void setUp() {
        validator = new FileUploadValidator();
        ReflectionTestUtils.setField(validator, "allowedExtensionsConfig",
                "pdf,png,jpg,jpeg,txt,doc,docx,xls,xlsx,csv");
    }

    private void assertRejected(MockMultipartFile file) {
        try {
            validator.validate(file);
            fail("Expected InvalidFileUploadException for file: " + file.getOriginalFilename());
        } catch (InvalidFileUploadException expected) {
            // expected
        }
    }

    @Test
    public void rejectsDoubleExtensionWithDisallowedFinalExtension() {
        assertRejected(new MockMultipartFile("file", "file.pdf.php", "application/octet-stream", HTML_MARKUP_BYTES));
    }

    @Test
    public void rejectsDoubleExtensionWithBlacklistedMiddleSegment() {
        assertRejected(new MockMultipartFile("file", "file.php.pdf", "application/pdf", PDF_MAGIC_BYTES));
    }

    @Test
    public void acceptsValidPdfWithMatchingMagicBytes() {
        validator.validate(new MockMultipartFile("file", "resume.pdf", "application/pdf", PDF_MAGIC_BYTES));
    }

    @Test
    public void rejectsMarkupContentDisguisedWithAllowedExtension() {
        assertRejected(new MockMultipartFile("file", "notes.txt", "text/plain", HTML_MARKUP_BYTES));
    }

    @Test
    public void rejectsContentThatDoesNotMatchDeclaredExtension() {
        assertRejected(new MockMultipartFile("file", "image.png", "image/png", PDF_MAGIC_BYTES));
    }
}
