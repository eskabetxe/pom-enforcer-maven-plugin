package pro.boto.maven.plugin.pom.enforcer.xml;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.Walker;
import org.jdom2.util.NamespaceStack;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

public class PomOutputProcessor extends AbstractXMLOutputProcessor {

    @Override
    protected void printElement(Writer out, FormatStack fstack, NamespaceStack nstack, Element element)
            throws IOException {
        nstack.push(element);
        try {
            List<Content> content = element.getContent();
            this.write(out, "<");
            this.write(out, element.getQualifiedName());

            // Handle Namespaces and Attributes for root tag alignment
            handleRootTagFormatting(out, fstack, nstack, element);

            if (content.isEmpty()) {
                // Compact empty tag: <dependencies/>
                this.write(out, "/>");
            } else {
                this.write(out, ">");

                fstack.push();
                try {
                    Walker walker = this.buildWalker(fstack, content, true);
                    if (walker.hasNext()) {
                        // Insert the leading newline and indentation for child elements
                        if (!walker.isAllText()) {
                            this.write(out, fstack.getPadBetween());
                        }

                        this.printContent(out, fstack, nstack, walker);

                        // Insert the trailing newline and indentation before the closing tag
                        if (!walker.isAllText()) {
                            this.write(out, fstack.getPadLast());
                        }
                    }
                } finally {
                    fstack.pop();
                }

                this.write(out, "</");
                this.write(out, element.getQualifiedName());
                this.write(out, ">");
            }
        } finally {
            nstack.pop();
        }
    }

    private void handleRootTagFormatting(Writer out, FormatStack fstack, NamespaceStack nstack, Element element)
            throws IOException {
        if (!"project".equals(element.getName())) {
            // Standard attribute printing for non-root elements
            if (element.hasAttributes()) {
                for (Attribute attribute : element.getAttributes()) {
                    this.printAttribute(out, fstack, attribute);
                }
            }
            return;
        }

        // Custom alignment logic for <project> attributes and namespaces
        Iterator<Namespace> nsIterator = nstack.addedForward().iterator();
        boolean first = true;
        while (nsIterator.hasNext()) {
            Namespace ns = nsIterator.next();
            if (first) {
                super.printNamespace(out, fstack, ns);
                first = false;
            } else {
                this.write(out, fstack.getLineSeparator());
                this.write(out, " ".repeat(8));
                super.printNamespace(out, fstack, ns);
            }
        }

        if (element.hasAttributes()) {
            for (Attribute attribute : element.getAttributes()) {
                this.write(out, fstack.getLineSeparator());
                this.write(out, " ".repeat(8));
                super.printAttribute(out, fstack, attribute);
            }
        }
    }

    @Override
    protected void printAttribute(Writer out, FormatStack fstack, Attribute attribute) throws IOException {
        // Standard attribute logic
        this.write(out, " ");
        this.write(out, attribute.getQualifiedName());
        this.write(out, "=");
        this.write(out, "\"");
        this.attributeEscapedEntitiesFilter(out, fstack, attribute.getValue());
        this.write(out, "\"");
    }
}
