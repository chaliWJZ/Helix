package com.helix.app.highlight.languages

import com.helix.app.highlight.core.Language
import com.helix.app.highlight.languages.bash.bash
import com.helix.app.highlight.languages.c.c
import com.helix.app.highlight.languages.cmake.cmake
import com.helix.app.highlight.languages.cpp.cpp
import com.helix.app.highlight.languages.csharp.csharp
import com.helix.app.highlight.languages.css.css
import com.helix.app.highlight.languages.dart.dart
import com.helix.app.highlight.languages.diff.diff
import com.helix.app.highlight.languages.dockerfile.dockerfile
import com.helix.app.highlight.languages.go.go
import com.helix.app.highlight.languages.glsl.glsl
import com.helix.app.highlight.languages.ini.ini
import com.helix.app.highlight.languages.java.java
import com.helix.app.highlight.languages.javascript.javascript
import com.helix.app.highlight.languages.json.json
import com.helix.app.highlight.languages.kotlin.kotlin
import com.helix.app.highlight.languages.latex.latex
import com.helix.app.highlight.languages.lua.lua
import com.helix.app.highlight.languages.markdown.markdown
import com.helix.app.highlight.languages.php.php
import com.helix.app.highlight.languages.powershell.powershell
import com.helix.app.highlight.languages.properties.properties
import com.helix.app.highlight.languages.python.python
import com.helix.app.highlight.languages.rust.rust
import com.helix.app.highlight.languages.ruby.ruby
import com.helix.app.highlight.languages.sql.sql
import com.helix.app.highlight.languages.swift.swift
import com.helix.app.highlight.languages.typescript.typescript
import com.helix.app.highlight.languages.xml.xml
import com.helix.app.highlight.languages.yaml.yaml

/**
 * Every grammar bundled with the highlighter.
 *
 * Each entry builds a fresh mode tree: compilation mutates modes in place, mirroring `highlight.js`.
 */
internal fun builtinLanguages(): List<Language> = listOf(
    json(),
    ini(),
    cmake(),
    go(),
    glsl(),
    yaml(),
    bash(),
    dockerfile(),
    javascript(),
    typescript(),
    xml(),
    css(),
    dart(),
    java(),
    kotlin(),
    latex(),
    lua(),
    powershell(),
    properties(),
    python(),
    c(),
    cpp(),
    csharp(),
    sql(),
    diff(),
    markdown(),
    rust(),
    ruby(),
    php(),
    swift(),
)
