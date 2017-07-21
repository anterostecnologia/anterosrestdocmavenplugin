package br.com.anteros.restdoc.maven.plugin.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

/**
 * ArtifactFilter implementation which returns true for the given pomArtifact
 * and offers the opportunity to delegate to another filter, if the given
 * artifact is not the pomArtifact.
 */
public class ProjectArtifactFilter implements ArtifactFilter
{

    private Artifact pomArtifact;
    private ArtifactFilter childFilter;

    public ProjectArtifactFilter(Artifact pomArtifact)
    {
        this(pomArtifact, null);
    }

    public ProjectArtifactFilter(Artifact pomArtifact, ArtifactFilter childFilter)
    {
        this.pomArtifact = pomArtifact;
        this.childFilter = childFilter;
    }

    public boolean include(Artifact artifact)
    {
        // always include the pom artifact
        if ( pomArtifact.equals(artifact) )
        {
            return true;
        }

        // delegate to given filter, if available
        if ( childFilter != null )
        {
            return childFilter.include(artifact);
        }

        // given artifact does not match any rule
        return false;
    }
}
