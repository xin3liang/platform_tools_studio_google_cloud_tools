/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.google.gct.idea.samples;

import com.android.tools.idea.gradle.project.GradleProjectImporter;
import com.android.tools.idea.wizard.*;
import com.appspot.gsamplesindex.samplesindex.model.Sample;
import com.appspot.gsamplesindex.samplesindex.model.SampleCollection;
import com.google.common.base.Strings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.android.tools.idea.wizard.ScopedStateStore.Scope.PATH;
import static com.android.tools.idea.wizard.ScopedStateStore.createKey;

/**
 * Wizard path for importing a Sample as a Project
 */
public class SampleImportWizardPath extends DynamicWizardPath {

  private Logger LOG = Logger.getInstance(SampleImportWizardPath.class);

  @NotNull private final Disposable myParentDisposable;
  @NotNull private final SampleCollection mySampleList;

  static final ScopedStateStore.Key<Sample> SAMPLE_KEY = createKey("SampleObject", PATH, Sample.class);
  static final ScopedStateStore.Key<String> SAMPLE_NAME = createKey("SampleName", PATH, String.class);
  static final ScopedStateStore.Key<String> SAMPLE_DIR = createKey("SampleDirectory", PATH, String.class);
  static final ScopedStateStore.Key<String> SAMPLE_URL = createKey("SampleUrl", PATH, String.class);

  public SampleImportWizardPath(@NotNull SampleCollection sampleList, @NotNull Disposable parentDisposable) {
    mySampleList = sampleList;
    myParentDisposable = parentDisposable;
  }

  @Override
  protected void init() {
    addStep(new SampleBrowserStep(mySampleList, myParentDisposable));
    addStep(new SampleSetupStep(myParentDisposable));
  }

  @NotNull
  @Override
  public String getPathName() {
    return "Sample Import";
  }

  @Override
  public boolean performFinishingActions() {

    Sample sample = myState.get(SAMPLE_KEY);
    String sampleName = myState.get(SAMPLE_NAME);
    File sampleDir = new File(myState.get(SAMPLE_DIR));

    assert !sampleDir.exists();

    if (!FileUtilRt.createDirectory(sampleDir)) {
      Messages.showErrorDialog("Failed to create project directory", "Sample Import Failed");
      return false;
    }
    Project project = ProjectManager.getInstance().createProject(sampleName, sampleDir.getAbsolutePath());

    String url = sample.getCloneUrl();

    NewFromGithubWizard.GithubRepoContents downloadResult = NewFromGithubWizard.downloadGithubRepo(project, url , null, null);

    if (downloadResult.errorMessage != null) {
      LOG.error(downloadResult.errorMessage);
      Messages.showErrorDialog(downloadResult.errorMessage, "Sample Download Failed");
      return false;
    }

    // we don't care about the template folders (downloadResult.templateFolders), so only check if we find sampleRoots
    if (downloadResult.sampleRoots.size() == 0) {
      Messages.showErrorDialog("Failed to find any projects in Git repository", "Sample Import Failed");
      return false;
    }

    try {
      String path = sample.getPath();
      if (!Strings.isNullOrEmpty(path)) {
        // we have a path to work with, find the project that matches it
        path = trimSlashes(path);
        sampleSearch: {
          for (File sampleRoot : downloadResult.sampleRoots) {
            if (sampleRoot.getCanonicalPath().equals(new File(downloadResult.rootFolder, path).getCanonicalPath())) {
              // we found our sample root
              FileUtil.copyDir(sampleRoot, new File(project.getBasePath()));
              break sampleSearch;
            }
          }
          // we have a project that doesn't contain the sample root we're looking for... notify the user
          Messages.showErrorDialog("Could not fine Sample Root '" + path + "' in Github Repo", "Sample Import Failed");
          return false;
        }
      }
      else {
        // no root was specified, just grab the first root
        FileUtil.copyDir(downloadResult.sampleRoots.get(0), new File(project.getBasePath()));
      }
    }
    catch (IOException e) {
      LOG.error(e);
      Messages.showErrorDialog("Unable to copy sample into project directory", "Sample Import Failed");
      return false;
    }

    GradleProjectImporter.getInstance().importProject(project.getBaseDir());
    // TODO : display the correct starting file for users
    return true;
  }

  /**
   * Trim trailing and leading forward slashes from a string
   */
  @NotNull
  static String trimSlashes(@NotNull String path) {
    while (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    while (path.startsWith("/")) {
      path = path.substring(1, path.length());
    }
    return path;
  }
}
