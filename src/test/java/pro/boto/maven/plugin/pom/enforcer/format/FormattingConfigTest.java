package pro.boto.maven.plugin.pom.enforcer.format;

import static org.assertj.core.api.Assertions.assertThat;

import pro.boto.maven.plugin.pom.enforcer.serde.PomOutputProcessor;

import org.jdom2.output.Format;
import org.junit.jupiter.api.Test;

class FormattingConfigTest {

    @Test
    void defaultConfigShouldHaveSensibleDefaults() {
        FormattingConfig config = FormattingConfig.defaultConfig();

        assertThat(config.encoding()).isEqualTo("UTF-8");
        assertThat(config.indentSize()).isEqualTo(4);
        assertThat(config.keepBlankLines()).isFalse();
        assertThat(config.lineSeparator()).isEqualTo("\n");
        assertThat(config.schemaLocation()).isEqualTo(SchemaLocationPolicy.INDENTED);
    }

    @Test
    void buildFormatShouldApplyAllSettings() {
        FormattingConfig config = new FormattingConfig("ISO-8859-1", 2, false, "\r\n", SchemaLocationPolicy.INLINE);

        Format format = config.buildFormat();

        assertThat(format.getEncoding()).isEqualTo("ISO-8859-1");
        assertThat(format.getIndent()).isEqualTo("  ");
        assertThat(format.getLineSeparator()).isEqualTo("\r\n");
    }

    @Test
    void buildOutputProcessorShouldReflectSchemaPolicy() {
        FormattingConfig indented = new FormattingConfig("UTF-8", 4, false, "\n", SchemaLocationPolicy.INDENTED);
        FormattingConfig inline = new FormattingConfig("UTF-8", 4, false, "\n", SchemaLocationPolicy.INLINE);

        PomOutputProcessor indentedProcessor = indented.buildOutputProcessor();
        PomOutputProcessor inlineProcessor = inline.buildOutputProcessor();

        // Both should be non-null and distinct instances
        assertThat(indentedProcessor).isNotNull();
        assertThat(inlineProcessor).isNotNull();
        assertThat(indentedProcessor).isNotSameAs(inlineProcessor);
    }

    @Test
    void setLineSeparatorShouldResolveEscapedNewline() {
        FormattingConfig config = new FormattingConfig();
        config.setLineSeparator("\\n");

        assertThat(config.lineSeparator()).isEqualTo("\n");
    }

    @Test
    void setLineSeparatorShouldResolveEscapedCrLf() {
        FormattingConfig config = new FormattingConfig();
        config.setLineSeparator("\\r\\n");

        assertThat(config.lineSeparator()).isEqualTo("\r\n");
    }

    @Test
    void keepPolicyShouldNotFilterAttributes() {
        FormattingConfig config = new FormattingConfig("UTF-8", 4, false, "\n", SchemaLocationPolicy.KEEP);

        Format format = config.buildFormat();

        assertThat(format.isSpecifiedAttributesOnly()).isFalse();
    }
}
