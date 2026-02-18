# POM Enforcer Maven Plugin
A Maven plugin designed to ensure architectural consistency and pedantic organization within pom.xml files

---

## Goals

| Goal | Description |
| :--- | :--- |
| `pom-enforcer:check` | Validates the POM files. Fails the build if violations are found. |
| `pom-enforcer:apply` | Automatically reformats the POM files to fix formatting, ordering, and sorting violations. |

---

## Configuration

The plugin can be configured in your `pom.xml`. Below are the available parameters based on the project source:

### Formatting Options
| Parameter              | Default                  | Description                                                                                                          |
|:-----------------------|:-------------------------|:---------------------------------------------------------------------------------------------------------------------|
| `encoding`             | `UTF-8`                  | The character encoding used for the XML output.                                                                      |
| `formatSchemaLocation` | `true`                   | If true, ensures only specified attributes are formatted in the output.                                              |
| `indentSchemaLocation` | `true`                   | When true, enables pedantic multi-line alignment for namespaces and the `xsi:schemaLocation` in the `<project>` tag. |
| `indentSpacesNumber`   | `4`                      | Number of spaces to use for each indentation level.                                                                  |
| `keepBlankLines`       | `false`                  | If true, preserves existing blank lines between elements from the source file.                                       |
| `lineSeparator`        | `System.lineSeparator()` | The character sequence used for new lines (e.g., `\n` or `\r\n`).                                                    |

### Parsing & Ordering Options
| Parameter        | Default                               | Description                                                                                                                   |
|:-----------------|:--------------------------------------|:------------------------------------------------------------------------------------------------------------------------------|
| `bomAtBeginning` | `true`                                | When true, Bill of Materials (BOM) imports (type `pom` and scope `import`) are prioritized at the top of the dependency list. |
| `bomKeepOrder`   | `true`                                | If true, preserves the original relative order between BOMs while keeping them at the top.                                    |
| `templateOrder`  | `/default_formatter.xml`              | Path to a custom XML template file used to determine the recursive ordering of POM elements.                                  |
| `dependencySort` | `groupId,artifactId,scope,classifier` | A comma-separated list of fields used to sort `dependencies` and `dependencyManagement` entries.                              |

---

## Default Element Order
The plugin uses an internal `default_formatter.xml` to determine the order of elements. Elements found in the template are moved to their relative positions; unknown elements are sorted alphabetically at the end. The general structure follows:

1.  `modelVersion`
2.  `parent`
3.  `groupId`, `artifactId`, `version`, `packaging`
4.  `name`, `description`, `url`, `inceptionYear`
5.  `organization`, `licenses`, `developers`, `contributors`
6.  `mailingLists`, `prerequisites`, `modules`, `scm`
7.  `issueManagement`, `ciManagement`, `distributionManagement`
8.  `properties`
9.  `dependencyManagement`
10. `dependencies`
11. `repositories`, `pluginRepositories`
12. `build`
13. `reporting`, `reports`
14. `profiles`

---

## Usage Example

Add the following to your root `pom.xml` to enable the plugin during the `validate` phase:

```xml
<plugin>
    <groupId>pro.boto</groupId>
    <artifactId>pom-enforcer-maven-plugin</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <configuration>
        <indentSpacesNumber>4</indentSpacesNumber>
        <dependencySort>groupId,artifactId,scope</dependencySort>
        <indentSchemaLocation>true</indentSchemaLocation>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
            <phase>validate</phase>
        </execution>
    </executions>
</plugin>
