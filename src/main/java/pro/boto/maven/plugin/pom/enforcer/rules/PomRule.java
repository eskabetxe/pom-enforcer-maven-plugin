package pro.boto.maven.plugin.pom.enforcer.rules;

import pro.boto.maven.plugin.pom.enforcer.model.RuleViolation;

import org.jdom2.Document;

import java.io.Serializable;
import java.util.List;

public interface PomRule extends Serializable {

    List<RuleViolation> apply(Document document);

    String getName();
}
