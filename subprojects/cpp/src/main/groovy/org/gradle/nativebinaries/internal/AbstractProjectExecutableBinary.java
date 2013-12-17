/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.nativebinaries.internal;

import org.gradle.language.base.internal.DefaultBinaryNamingScheme;
import org.gradle.nativebinaries.*;
import org.gradle.nativebinaries.internal.resolve.NativeDependencyResolver;
import org.gradle.nativebinaries.platform.Platform;
import org.gradle.nativebinaries.toolchain.internal.ToolChainInternal;

public class AbstractProjectExecutableBinary extends AbstractProjectNativeBinary implements ExecutableBinary {
    private final Executable executable;

    public AbstractProjectExecutableBinary(Executable executable, Flavor flavor, ToolChainInternal toolChain, Platform platform, BuildType buildType,
                                           DefaultBinaryNamingScheme namingScheme, NativeDependencyResolver resolver) {
        super(executable, flavor, toolChain, platform, buildType, namingScheme.withTypeString("Executable"), resolver);
        this.executable = executable;
    }

    public Executable getComponent() {
        return executable;
    }

    public String getOutputFileName() {
        return getToolChain().getExecutableName(getComponent().getBaseName());
    }
}