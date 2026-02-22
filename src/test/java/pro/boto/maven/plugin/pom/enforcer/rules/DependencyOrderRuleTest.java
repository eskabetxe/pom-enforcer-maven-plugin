package pro.boto.maven.plugin.pom.enforcer.rules;

import static org.assertj.core.api.Assertions.assertThat;

import pro.boto.maven.plugin.pom.enforcer.format.PomSerde;
import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

class DependencyOrderRuleTest {

    private final PomSerde pomSerde = PomSerde.defaultConfig();
    private final Namespace ns = Namespace.getNamespace("http://maven.apache.org/POM/4.0.0");

    @Test
    void shouldSortDependenciesByGroupIdAndArtifactId() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" + "  <dependencies>"
                + "    <dependency><groupId>org.hibernate</groupId><artifactId>hibernate-core</artifactId></dependency>"
                + "    <dependency><groupId>com.google.guava</groupId><artifactId>guava</artifactId></dependency>"
                + "    <dependency><groupId>org.hibernate</groupId><artifactId>hibernate-validator</artifactId></dependency>"
                + "  </dependencies>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        DependencyOrderRule rule = new DependencyOrderRule();

        List<RuleViolation> violations = rule.apply(doc);
        // Assert
        assertThat(violations).isNotEmpty();

        List<Element> deps = doc.getRootElement().getChild("dependencies", ns).getChildren();
        assertThat(deps)
                .extracting(e -> e.getChildText("artifactId", ns))
                .containsExactly("guava", "hibernate-core", "hibernate-validator");
    }

    @Test
    void shouldPlaceBomsAtTopAndSortThemAlphabetically() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" + "  <dependencies>"
                + "    <dependency><groupId>Z.regular</groupId><artifactId>A</artifactId></dependency>"
                + "    <dependency><groupId>M.bom</groupId><artifactId>B</artifactId><type>pom</type><scope>import</scope></dependency>"
                + "    <dependency><groupId>A.bom</groupId><artifactId>C</artifactId><type>pom</type><scope>import</scope></dependency>"
                + "  </dependencies>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        // Arrange: BOMs first, and sort the BOMs themselves by groupId
        DependencyOrderRule rule = new DependencyOrderRule().withBomKeepOrder(false);

        List<RuleViolation> violations = rule.apply(doc);
        // Assert
        assertThat(violations).isNotEmpty();

        // Assert
        List<Element> deps = doc.getRootElement().getChild("dependencies", ns).getChildren();
        assertThat(deps).extracting(e -> e.getChildText("groupId", ns)).containsExactly("A.bom", "M.bom", "Z.regular");
    }

    @Test
    void shouldPlaceBomsAtTopButKeepTheirOriginalOrder() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" + "  <dependencies>"
                + "    <dependency><groupId>Z.regular</groupId><artifactId>A</artifactId></dependency>"
                + "    <dependency><groupId>M.bom</groupId><artifactId>B</artifactId><type>pom</type><scope>import</scope></dependency>"
                + "    <dependency><groupId>A.bom</groupId><artifactId>C</artifactId><type>pom</type><scope>import</scope></dependency>"
                + "  </dependencies>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        // Arrange: BOMs first, but keep their relative order (M then A)
        DependencyOrderRule rule = new DependencyOrderRule();

        List<RuleViolation> violations = rule.apply(doc);
        // Assert
        assertThat(violations).isNotEmpty();
        List<Element> deps = doc.getRootElement().getChild("dependencies", ns).getChildren();
        assertThat(deps).extracting(e -> e.getChildText("groupId", ns)).containsExactly("M.bom", "A.bom", "Z.regular");
    }

    @Test
    void shouldSortByScopeThenGroupId() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" + "  <dependencies>"
                + "    <dependency><groupId>B</groupId><artifactId>B</artifactId><scope>test</scope></dependency>"
                + "    <dependency><groupId>A</groupId><artifactId>A</artifactId><scope>compile</scope></dependency>"
                + "    <dependency><groupId>C</groupId><artifactId>C</artifactId><scope>test</scope></dependency>"
                + "  </dependencies>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));

        // Arrange: No BOM priority, sort by scope (alphabetical) then groupId
        DependencyOrderRule rule = new DependencyOrderRule();

        List<RuleViolation> violations = rule.apply(doc);
        // Assert
        assertThat(violations).isNotEmpty();
        List<Element> deps = doc.getRootElement().getChild("dependencies", ns).getChildren();
        assertThat(deps).extracting(e -> e.getChildText("groupId", ns)).containsExactly("A", "B", "C");
    }

    @Test
    void shouldHandleDependencyManagementRecursively() throws Exception {
        String xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" + "  <dependencyManagement>"
                + "    <dependencies>"
                + "      <dependency><groupId>Z</groupId><artifactId>Z</artifactId></dependency>"
                + "      <dependency><groupId>A</groupId><artifactId>A</artifactId></dependency>"
                + "    </dependencies>"
                + "  </dependencyManagement>"
                + "</project>";
        Document doc = pomSerde.deserialize(new StringReader(xml));
        DependencyOrderRule rule = new DependencyOrderRule();

        List<RuleViolation> violations = rule.apply(doc);
        // Assert
        assertThat(violations).isNotEmpty();
        List<Element> deps = doc.getRootElement()
                .getChild("dependencyManagement", ns)
                .getChild("dependencies", ns)
                .getChildren();

        assertThat(deps).extracting(e -> e.getChildText("groupId", ns)).containsExactly("A", "Z");
    }
}
