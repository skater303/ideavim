/*
 * Copyright 2022 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

@file:Suppress("RemoveCurlyBracesFromTemplate")

package org.jetbrains.plugins.ideavim.action.motion.gn

import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.VimStateMachine
import com.maddyhome.idea.vim.common.Direction
import org.jetbrains.plugins.ideavim.SkipNeovimReason
import org.jetbrains.plugins.ideavim.TestWithoutNeovim
import org.jetbrains.plugins.ideavim.VimTestCase
import javax.swing.KeyStroke

class GnPreviousTextObjectTest : VimTestCase() {
  @TestWithoutNeovim(SkipNeovimReason.DIFFERENT)
  fun `test delete word`() {
    doTestWithSearch(
      injector.parser.parseKeys("dgN"),
      """
      Hello, ${c}this is a test here
      """.trimIndent(),
      """
        Hello, this is a ${c} here
      """.trimIndent()
    )
  }

  @TestWithoutNeovim(SkipNeovimReason.DIFFERENT)
  fun `test delete second word`() {
    doTestWithSearch(
      injector.parser.parseKeys("2dgN"),
      """
      Hello, this is a test here
      Hello, this is a test ${c}here
      """.trimIndent(),
      """
        Hello, this is a ${c} here
        Hello, this is a test here
      """.trimIndent()
    )
  }

  fun `test gn uses last used pattern not just search pattern`() {
    doTest(
      listOf("/is<CR>", ":s/test/tester/<CR>", "$", "dgN"),
      "Hello, ${c}this is a test here",
      "Hello, this is a ${c}er here",
      VimStateMachine.Mode.COMMAND, VimStateMachine.SubMode.NONE
    )
  }

  private fun doTestWithSearch(keys: List<KeyStroke>, before: String, after: String) {
    configureByText(before)
    VimPlugin.getSearch().setLastSearchState(myFixture.editor, "test", "", Direction.FORWARDS)
    typeText(keys)
    assertState(after)
    assertState(VimStateMachine.Mode.COMMAND, VimStateMachine.SubMode.NONE)
  }
}
