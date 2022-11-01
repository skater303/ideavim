/*
 * Copyright 2022 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

@file:JvmName("ModeHelper")

package com.maddyhome.idea.vim.helper

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.command.SelectionType
import com.maddyhome.idea.vim.command.VimStateMachine
import com.maddyhome.idea.vim.common.TextRange
import com.maddyhome.idea.vim.listener.SelectionVimListenerSuppressor
import com.maddyhome.idea.vim.newapi.IjExecutionContext
import com.maddyhome.idea.vim.newapi.IjVimCaret
import com.maddyhome.idea.vim.newapi.IjVimEditor
import com.maddyhome.idea.vim.newapi.vim

/**
 * Pop all modes, but leave editor state. E.g. editor selection is not removed.
 */
fun Editor.popAllModes() {
  val commandState = this.vim.vimStateMachine
  while (commandState.mode != VimStateMachine.Mode.COMMAND) {
    commandState.popModes()
  }
}

@RWLockLabel.NoLockRequired
fun Editor.exitVisualMode() {
  val selectionType = SelectionType.fromSubMode(this.subMode)
  SelectionVimListenerSuppressor.lock().use {
    if (inBlockSubMode) {
      this.caretModel.removeSecondaryCarets()
    }
    if (!this.vimKeepingVisualOperatorAction) {
      this.caretModel.allCarets.forEach(Caret::removeSelection)
    }
  }
  if (this.inVisualMode) {
    this.vimLastSelectionType = selectionType
    val primaryCaret = this.caretModel.primaryCaret
    val vimSelectionStart = primaryCaret.vimSelectionStart
    VimPlugin.getMark().setVisualSelectionMarks(this.vim, TextRange(vimSelectionStart, primaryCaret.offset))
    this.caretModel.allCarets.forEach { it.vimSelectionStartClear() }

    this.vim.vimStateMachine.popModes()
  }
}

/** [adjustCaretPosition] - if true, caret will be moved one char left if it's on the line end */
fun Editor.exitSelectMode(adjustCaretPosition: Boolean) {
  if (!this.inSelectMode) return

  this.vim.vimStateMachine.popModes()
  SelectionVimListenerSuppressor.lock().use {
    this.caretModel.allCarets.forEach {
      it.removeSelection()
      it.vimSelectionStartClear()
      if (adjustCaretPosition) {
        val lineEnd = EditorHelper.getLineEndForOffset(this, it.offset)
        val lineStart = EditorHelper.getLineStartForOffset(this, it.offset)
        if (it.offset == lineEnd && it.offset != lineStart) {
          it.moveToInlayAwareOffset(it.offset - 1)
        }
      }
    }
  }
}

/** [adjustCaretPosition] - if true, caret will be moved one char left if it's on the line end */
fun VimEditor.exitSelectMode(adjustCaretPosition: Boolean) {
  if (!this.inSelectMode) return

  this.vimStateMachine.popModes()
  SelectionVimListenerSuppressor.lock().use {
    this.carets().forEach { vimCaret ->
      val caret = (vimCaret as IjVimCaret).caret
      caret.removeSelection()
      caret.vimSelectionStartClear()
      if (adjustCaretPosition) {
        val lineEnd = EditorHelper.getLineEndForOffset((this as IjVimEditor).editor, caret.offset)
        val lineStart = EditorHelper.getLineStartForOffset(this.editor, caret.offset)
        if (caret.offset == lineEnd && caret.offset != lineStart) {
          caret.moveToInlayAwareOffset(caret.offset - 1)
        }
      }
    }
  }
}

fun Editor.exitInsertMode(context: DataContext, operatorArguments: OperatorArguments) {
  VimPlugin.getChange().processEscape(IjVimEditor(this), IjExecutionContext(context), operatorArguments)
}
