/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.integtests.fixtures.executer;

import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.test.fixtures.file.TestDirectoryProvider;
import org.gradle.test.fixtures.file.TestFile;
import org.gradle.util.GradleVersion;
import org.gradle.util.VersionNumber;

public class DefaultGradleDistribution implements GradleDistribution {

    private final GradleVersion version;
    private final TestFile gradleHomeDir;
    private final TestFile binDistribution;

    public DefaultGradleDistribution(GradleVersion gradleVersion, TestFile gradleHomeDir, TestFile binDistribution) {
        this.version = gradleVersion;
        this.gradleHomeDir = gradleHomeDir;
        this.binDistribution = binDistribution;
    }

    @Override
    public String toString() {
        return version.toString();
    }

    public TestFile getGradleHomeDir() {
        return gradleHomeDir;
    }

    public TestFile getBinDistribution() {
        return binDistribution;
    }

    public GradleVersion getVersion() {
        return version;
    }

    public GradleExecuter executer(TestDirectoryProvider testDirectoryProvider) {
        return new ForkingGradleExecuter(this, testDirectoryProvider);
    }

    public boolean worksWith(Jvm jvm) {
        // Milestone 4 was broken on the IBM jvm
        if (jvm.isIbmJvm() && isVersion("1.0-milestone-4")) {
            return false;
        }
        // 0.9-rc-1 was broken for Java 5
        if (isVersion("0.9-rc-1")) {
            return jvm.getJavaVersion().isJava6Compatible();
        }
        if (isSameOrOlder("1.12")) {
            return jvm.getJavaVersion().isJava5Compatible();
        }
        return jvm.getJavaVersion().isJava6Compatible();
    }

    public boolean worksWith(OperatingSystem os) {
        // 1.0-milestone-5 was broken where jna was not available
        //noinspection SimplifiableIfStatement
        if (isVersion("1.0-milestone-5")) {
            return os.isWindows() || os.isMacOsX() || os.isLinux();
        } else {
            return true;
        }
    }

    public boolean isDaemonSupported() {
        // Milestone 7 was broken on the IBM jvm
        if (Jvm.current().isIbmJvm() && isVersion("1.0-milestone-7")) {
            return false;
        }

        if (OperatingSystem.current().isWindows()) {
            // On windows, daemon is ok for anything > 1.0-milestone-3
            return isSameOrNewer("1.0-milestone-4");
        } else {
            // Daemon is ok for anything >= 0.9
            return isSameOrNewer("0.9");
        }
    }

    public boolean isDaemonIdleTimeoutConfigurable() {
        return isSameOrNewer("1.0-milestone-7");
    }

    public boolean isOpenApiSupported() {
        return isSameOrNewer("0.9-rc-1") && !isSameOrNewer("2.0");
    }

    public boolean isToolingApiSupported() {
        return isSameOrNewer("1.0-milestone-3");
    }

    public boolean isToolingApiNonAsciiOutputSupported() {
        if (OperatingSystem.current().isWindows()) {
            return !isVersion("1.0-milestone-7") && !isVersion("1.0-milestone-8") && !isVersion("1.0-milestone-8a");
        }
        return true;
    }

    public VersionNumber getArtifactCacheLayoutVersion() {
        if (isSameOrNewer("2.0-rc-1")) {
            return VersionNumber.parse("2.12");
        } else if (isSameOrNewer("1.12-rc-1")) {
            return VersionNumber.parse("2.6");
        } else if (isSameOrNewer("1.11-rc-1")) {
            return VersionNumber.parse("2.2");
        } else if (isSameOrNewer("1.9-rc-2")) {
            return VersionNumber.parse("2.1");
        } else if (isSameOrNewer("1.9-rc-1")) {
            return VersionNumber.parse("1.31");
        } else if (isSameOrNewer("1.7-rc-1")) {
            return VersionNumber.parse("0.26");
        } else if (isSameOrNewer("1.6-rc-1")) {
            return VersionNumber.parse("0.24");
        } else if (isSameOrNewer("1.4-rc-1")) {
            return VersionNumber.parse("0.23");
        } else if (isSameOrNewer("1.3")) {
            return VersionNumber.parse("0.15");
        } else {
            return VersionNumber.parse("0.1");
        }
    }

    public boolean wrapperCanExecute(GradleVersion version) {
        if (version.equals(GradleVersion.version("0.8")) || isVersion("0.8")) {
            // There was a breaking change after 0.8
            return false;
        }
        if (isVersion("0.9.1")) {
            // 0.9.1 couldn't handle anything with a timestamp whose timezone was behind GMT
            return version.getVersion().matches(".*+\\d{4}");
        }
        if (isSameOrNewer("0.9.2") && isSameOrOlder("1.0-milestone-2")) {
            // These versions couldn't handle milestone patches
            if (version.getVersion().matches("1.0-milestone-\\d+[a-z]-.+")) {
                return false;
            }
        }
        return true;
    }

    public boolean isSupportsSpacesInGradleAndJavaOpts() {
        return isSameOrNewer("1.0-milestone-5");
    }

    public boolean isFullySupportsIvyRepository() {
        return isSameOrNewer("1.0-milestone-7");
    }

    protected boolean isSameOrNewer(String otherVersion) {
        return isVersion(otherVersion) || version.compareTo(GradleVersion.version(otherVersion)) > 0;
    }

    protected boolean isSameOrOlder(String otherVersion) {
        return isVersion(otherVersion) || version.compareTo(GradleVersion.version(otherVersion)) <= 0;
    }

    protected boolean isVersion(String otherVersionString) {
        GradleVersion otherVersion = GradleVersion.version(otherVersionString);
        return version.compareTo(otherVersion) == 0 || (version.isSnapshot() && version.getBaseVersion().equals(otherVersion.getBaseVersion()));
    }

}
