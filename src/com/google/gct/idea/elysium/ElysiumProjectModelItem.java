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
package com.google.gct.idea.elysium;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This model item represents a single elysium project.
 */
class ElysiumProjectModelItem extends DefaultMutableTreeNode {
  private String myDescription;
  private String myProjectId;

  public ElysiumProjectModelItem(@Nullable String description, @NotNull String id) {
    setDescription(description);
    setProjectId(id);
  }

  public String getDescription() {
    return myDescription;
  }

  public void setDescription(String description) {
    myDescription = description;
  }

  public String getProjectId() {
    return myProjectId;
  }

  public void setProjectId(String projectId) {
    myProjectId = projectId;
  }
}
