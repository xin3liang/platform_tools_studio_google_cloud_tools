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

import com.android.tools.idea.wizard.DynamicWizard;
import com.appspot.gsamplesindex.samplesindex.model.SampleCollection;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Presents a wizard to the user to import hosted samples
 */
public class SampleImportWizard extends DynamicWizard {

  private SampleCollection mySampleList;

  public SampleImportWizard(@Nullable Project project, @NotNull SampleCollection samples) {
    super(project, null, "Import Sample");
    mySampleList = samples;
    init();

  }
  @Override
  public void init() {
    if (!AndroidSdkUtils.isAndroidSdkAvailable()) {
      String title = "SDK problem";
      String msg =
        "<html>Your Android SDK is missing or out of date.<br>" +
        "You can configure your SDK via <b>Configure | Project Defaults | Project Structure | SDKs</b></html>";
      super.init();
      Messages.showErrorDialog(msg, title);
      throw new IllegalStateException(msg);
    }

    addPath(new SampleImportWizardPath(mySampleList, getDisposable()));
    super.init();
  }

  @Override
  public void performFinishingActions() {
    // our single path is performing the finishing actions
  }

  @Override
  protected String getWizardActionDescription() {
    return "Import Sample";
  }
}
