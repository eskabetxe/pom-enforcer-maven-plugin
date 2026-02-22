package pro.boto.maven.plugin.pom.enforcer.format;

import static org.assertj.core.api.Assertions.assertThat;

import org.jdom2.Document;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

class PomSerdeTest {

    @Test
    void shouldFormatProjectAttributesBeIndented() throws Exception {
        String messyXml =
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n<modelVersion>4.0.0</modelVersion>\n</project>";

        PomSerde pomSerde = new PomSerde(new FormattingConfig("UTF-8", true, true, 4, false, "\n"));
        Document doc = pomSerde.deserialize(new StringReader(messyXml));

        byte[] cleanXml = pomSerde.serialize(doc);
        assertThat(new String(cleanXml))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
                        + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                        + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                        + "    <modelVersion>4.0.0</modelVersion>\n"
                        + "</project>\n");
    }

    @Test
    void shouldFormatProjectAttributesBeInlined() throws Exception {
        String messyXml =
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n<modelVersion>4.0.0</modelVersion>\n</project>";

        PomSerde pomSerde = new PomSerde(new FormattingConfig("UTF-8", true, false, 4, false, "\n"));
        Document doc = pomSerde.deserialize(new StringReader(messyXml));

        byte[] cleanXml = pomSerde.serialize(doc);
        assertThat(new String(cleanXml))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                        + "    <modelVersion>4.0.0</modelVersion>\n"
                        + "</project>\n");
    }
}
