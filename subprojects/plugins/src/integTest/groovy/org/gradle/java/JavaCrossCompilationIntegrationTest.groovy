/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.java

import org.gradle.api.JavaVersion
import org.gradle.integtests.fixtures.AvailableJavaHomes
import org.gradle.integtests.fixtures.MultiVersionIntegrationSpec
import org.gradle.integtests.fixtures.TargetVersions
import org.gradle.internal.jvm.JavaInfo
import org.gradle.util.TextUtil
import org.junit.Assume

@TargetVersions(["1.5", "1.6", "1.7", "1.8"])
class JavaCrossCompilationIntegrationTest extends MultiVersionIntegrationSpec {
    def JavaInfo getTarget() {
        return AvailableJavaHomes.getJdk(JavaVersion.toVersion(version))
    }

    def setup() {
        Assume.assumeTrue(target != null)
        def java = TextUtil.escapeString(target.getJavaExecutable())
        def javac = TextUtil.escapeString(target.getExecutable("javac"))

        buildFile << """
apply plugin: 'java'
sourceCompatibility = ${version}
targetCompatibility = ${version}
repositories { mavenCentral() }
tasks.withType(JavaCompile) {
    options.with {
        fork = true
        forkOptions.executable = "$javac"
    }
}
tasks.withType(Test) {
    executable = "$java"
}
"""

        file("src/main/java/Thing.java") << """
class Thing { }
"""
    }

    def "can compile source and run JUnit tests using target Java version"() {
        given:
        buildFile << """
dependencies { testCompile 'junit:junit:4.11' }
"""

        file("src/test/java/ThingTest.java") << """
import org.junit.Test;
import static org.junit.Assert.*;

public class ThingTest {
    @Test
    public void verify() {
        assertTrue(System.getProperty("java.version").startsWith("${version}."));
    }
}
"""

        expect:
        succeeds 'test'
    }

    def "can compile source and run TestNG tests using target Java version"() {
        given:
        buildFile << """
dependencies { testCompile 'org.testng:testng:6.8.8' }
"""

        file("src/test/java/ThingTest.java") << """
import org.testng.annotations.Test;

public class ThingTest {
    @Test
    public void verify() {
        assert System.getProperty("java.version").startsWith("${version}.");
    }
}
"""

        expect:
        succeeds 'test'
    }
}
