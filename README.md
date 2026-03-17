# POM Enforcer Maven Plugin

A Maven plugin designed to ensure architectural consistency and pedantic organization within `pom.xml` files.

An alternative to [sortpom](https://github.com/Ekryd/sortpom) and [pedantic-pom-enforcers](https://github.com/ferstl/pedantic-pom-enforcers) — combining the best of both: **check** violations like pedantic, **fix** them automatically like sortpom.

---

## Goals

| Goal                   | Description                                                                 |
|:-----------------------|:----------------------------------------------------------------------------|
| `pom-enforcer:check`   | Validates POM files. Fails the build if violations are found.               |
| `pom-enforcer:apply`   | Automatically fixes formatting, ordering, and sorting violations in place.  |

The `check` goal performs **read-only analysis** — it never modifies your files, not even in memory.
The `apply` goal detects and fixes violations in a single pass, writing corrected files back to disk.

---

## Quick Start

Add the following to your root `pom.xml`:

```xml
<plugin>
    <groupId>pro.boto</groupId>
    <artifactId>pom-enforcer-maven-plugin</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
            <phase>validate</phase>
        </execution>
    </executions>
</plugin>
```

Fix violations automatically:

```bash
mvn pom-enforcer:apply
```

---

## Configuration

Configuration is split into two blocks: **formatting** (how the file looks on disk) and **rules** (what logical rules to enforce).

```xml
<plugin>
    <groupId>pro.boto</groupId>
    <artifactId>pom-enforcer-maven-plugin</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <configuration>
        <formatting>
            <encoding>UTF-8</encoding>
            <indentSize>4</indentSize>
            <lineSeparator>\n</lineSeparator>
            <keepBlankLines>false</keepBlankLines>
            <schemaLocation>INDENTED</schemaLocation>
        </formatting>
        <rules>
            <templateOrder>
                <template>/default_formatter.xml</template>
            </templateOrder>
            <dependencyOrder>
                <sortBy>groupId,artifactId,scope,classifier</sortBy>
                <bomFirst>true</bomFirst>
                <bomPreserveOrder>true</bomPreserveOrder>
            </dependencyOrder>
        </rules>
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
```

All parameters are optional — the defaults shown above are applied when omitted.

---

### Formatting

Controls serialization style — encoding, indentation, line endings, and `<project>` tag rendering.

| Parameter        | Default    | Description                                                                     |
|:-----------------|:-----------|:--------------------------------------------------------------------------------|
| `encoding`       | `UTF-8`    | Character encoding for the XML output.                                          |
| `indentSize`     | `4`        | Number of spaces per indentation level.                                         |
| `lineSeparator`  | `\n`       | Line ending character sequence (`\n` or `\r\n`).                                |
| `keepBlankLines` | `false`    | When `true`, preserves existing blank lines between elements.                   |
| `schemaLocation` | `INDENTED` | How the `<project>` tag attributes are rendered. See below.                     |

#### Schema Location Policy

Controls the rendering of namespace declarations and `xsi:schemaLocation` on the `<project>` tag:

| Value      | Result                                                                                     |
|:-----------|:-------------------------------------------------------------------------------------------|
| `INDENTED` | Each attribute on its own line, vertically aligned.                                        |
| `INLINE`   | All attributes on a single line.                                                           |
| `KEEP`     | No reformatting — attributes are preserved as-is.                                          |

**`INDENTED`** (default):
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                       http://maven.apache.org/xsd/maven-4.0.0.xsd">
```

**`INLINE`**:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
```

---

### Rules

Each rule is configured in its own block under `<rules>`. Setting a rule block to empty uses its defaults. Omitting a rule block entirely still enables it with defaults. To **disable** a rule, it must be explicitly excluded (see [Disabling Rules](#disabling-rules)).

#### Template Order

Sorts POM elements recursively according to a reference XML template.

| Parameter  | Default                  | Description                                                      |
|:-----------|:-------------------------|:-----------------------------------------------------------------|
| `template` | `/default_formatter.xml` | Classpath path to the XML template defining element order.       |

Elements found in the template are sorted to their defined positions. Unknown elements are sorted alphabetically at the end.

#### Dependency Order

Sorts `<dependency>` elements across all sections of the POM.

| Parameter          | Default                               | Description                                                                                           |
|:-------------------|:--------------------------------------|:------------------------------------------------------------------------------------------------------|
| `sortBy`           | `groupId,artifactId,scope,classifier` | Comma-separated list of fields used for sorting.                                                      |
| `bomFirst`         | `true`                                | When `true`, BOM imports (`type=pom`, `scope=import`) are placed at the top of the dependency list.   |
| `bomPreserveOrder` | `true`                                | When `true`, BOMs keep their original relative order. When `false`, BOMs are sorted by `sortBy`.      |

**Note:** `bomFirst` and `bomPreserveOrder` only apply to `<dependencyManagement>` sections, where BOM imports are defined. In regular `<dependencies>` blocks, all entries are sorted uniformly by `sortBy`.

---

### Disabling Rules

To disable a specific rule, exclude its block from the configuration. When `<rules>` is present, only the rules explicitly configured (or left at defaults) are active:

```xml
<configuration>
    <rules>
        <!-- Only template ordering is active, dependency ordering is disabled -->
        <templateOrder/>
    </rules>
</configuration>
```

---

## Default Element Order

The built-in `default_formatter.xml` template defines the canonical order of POM elements:

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

This order is applied recursively — inner elements like `<dependency>` children (`groupId`, `artifactId`, `version`, ...) are also sorted according to the template.

To use a custom order, provide your own template file:

```xml
<rules>
    <templateOrder>
        <template>/my-custom-order.xml</template>
    </templateOrder>
</rules>
```

---

## Violation Reporting

The `check` goal produces structured violation reports with precise location information:

```
[ERROR] Violation in pom.xml: [template-order] Elements are not in the expected order.
    - at <project> expected=[modelVersion, parent, dependencies] actual=[dependencies, parent, modelVersion]
[ERROR] Violation in pom.xml: [dependency-order] Dependencies in <dependencyManagement/dependencies> are not sorted.
    - at <dependencyManagement/dependencies> expected=[com.google:guava, org.hibernate:core] actual=[org.hibernate:core, com.google:guava]
```

---

## Roadmap

### In Progress
- [ ] Documentation site with detailed examples and migration guides from sortpom / pedantic-pom-enforcers

### Planned Rules
- [ ] **Plugin Order** — sort `<plugin>` elements in `<build>` and `<pluginManagement>`
- [ ] **Property Order** — sort `<properties>` alphabetically
- [ ] **Module Order** — sort `<modules>` alphabetically
- [ ] **Profile Order** — sort `<profile>` elements by id
- [ ] **Duplicate Dependency Detection** — check-only rule, flags duplicate GAV entries

### Planned Features
- [ ] **Per-section dependency config** — override `sortBy`, `bomFirst` per dependency location (`dependencyManagement`, `pluginDependencies`, etc.)
- [ ] **Idempotency verification in CI** — `mvn pom-enforcer:check` returns non-zero exit code with structured output for CI integration
- [ ] **Custom rule SPI** — allow external rules via ServiceLoader

---

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)
