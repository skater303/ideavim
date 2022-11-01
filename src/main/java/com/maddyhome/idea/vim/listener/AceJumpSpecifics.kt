/*
 * Copyright 2022 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

package com.maddyhome.idea.vim.listener

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.Editor
import org.acejump.session.SessionManager

/**
 * Key handling for IdeaVim should be updated to editorHandler usage. In this case this class can be safely removed.
 */

@Suppress("DEPRECATION")
interface AceJumpService {
  fun isActive(editor: Editor): Boolean

  companion object {
    fun getInstance(): AceJumpService? = ServiceManager.getService(AceJumpService::class.java)
  }
}

class AceJumpServiceImpl : AceJumpService {
  override fun isActive(editor: Editor): Boolean {
    return try {
      SessionManager[editor] != null
    } catch (e: Throwable) {
      // In case of any exception
      false
    }
  }
}
