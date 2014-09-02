package org.genivi.ionas.test

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Text
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException
import org.eclipse.swtbot.swt.finder.finders.MenuFinder
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType
import org.eclipse.swtbot.swt.finder.utils.SWTUtils
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matcher
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher
import org.hamcrest.Description
import org.eclipse.swt.widgets.MenuItem
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.swt.widgets.Widget
import org.eclipse.swt.custom.CCombo
import org.eclipse.swt.widgets.Combo
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes
import org.eclipse.swt.SWT
import org.eclipse.jface.bindings.keys.KeyStroke
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView

import static org.junit.Assert.*

@RunWith(SWTBotJunit4ClassRunner)
class BasicTransformationTest {
	private static SWTWorkbenchBot bot

	@BeforeClass
	def static void beforeClass() throws Exception {
		waitForDisplayToAppear(10000)
		bot = new SWTWorkbenchBot
		
		

		Thread.sleep(2000)
		println("shell visible " + bot.activeShell.visible + " and active " + bot.activeShell.active)
		try {
			bot.viewByTitle("Welcome").close
			println("closed welcome view")
		} catch (WidgetNotFoundException e) {
			println("there was no welcome view ")
		}
		
		bot.perspectiveByLabel("Artop").activate
		
		if (bot.perspectiveByLabel("Artop").active)
			println("Artop perspective is active")
			
		bot.activeShell
		Thread.sleep(1000)
		bot.menu("File").menu("Import...").click();
		bot.tree.getTreeItem("General").expand();
		bot.tree.getTreeItem("General").getNode("Existing Projects into Workspace").select;
		bot.button("Next >").click;
		bot.comboBox.text = ResourcesPlugin.getWorkspace().root.rawLocation.makeAbsolute.toString + "/../../../../"
		bot.comboBox.pressShortcut(KeyStroke.getInstance(SWT.LF));
		Thread.sleep(1000)
		var SWTBotTreeItem item = null
		for (i : bot.tree.allItems){
			if (i.text.startsWith("")){
				item = i
			}
		}
		println("item is " + item)
		ResourcesPlugin.getWorkspace().root.rawLocation.makeAbsolute.toString
		bot.checkBox("Copy projects into workspace").select
		
		Thread.sleep(500)
		bot.button("Finish").setFocus
		bot.button("Deselect All").click
		item.toggleCheck
		Thread.sleep(500)
		bot.button("Finish").click
		// this takes a while
		Thread.sleep(4000)
	}

	@Test
	def void test01() {
		bot.tree.getTreeItem("org.genivi.ionas.testProject").expand
		Thread.sleep(5000)
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("connector1.fcon").contextMenu("Generate stubs").click
		bot.shell("Progress Information")
		Thread.sleep(8000)
		bot.shell("IoNAS").bot.button("OK").click
		bot.tree.getTreeItem("org.genivi.ionas.testProject").contextMenu("Refresh").click
		Thread.sleep(2000)
		bot.tree.getTreeItem("org.genivi.ionas.testProject").expand
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("output").expand
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("result").expand
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("output").getNode("autosarOutput1.arxml")
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("result").getNode("francaOutput1.fidl")
		
		var SWTBotView console = bot.views.findFirst[title == "Console"]

		assertNotNull(console)
		val consoleOutput = console.bot.styledText.text
		assertTrue(consoleOutput.contains("Franca model has been successfully created."))
		assertTrue(consoleOutput.contains("AUTOSAR model has been successfully created."))
		assertFalse(consoleOutput.contains("Exception") || consoleOutput.contains("Error"))
		
		bot.menu("Window").menu("Show View").menu("Problems").click();
		val problemsBot = bot.views.findFirst[title == "Problems"].bot
		//assertNotNull(problemsBot.tree.allItems.findFirst[it.nodes.head.startsWith("Warnings")])
		//assertNull(problemsBot.tree.allItems.findFirst[text.startsWith("Errors")])
		
		Thread.sleep(5000)
	}

	def private static void waitForDisplayToAppear(long time) {
		var long endTime = -1;
		var Display display = null;
		try {
			endTime = System.currentTimeMillis() + time;
			while (System.currentTimeMillis() < endTime) { // wait until timeout
				try {
					display = SWTUtils.display();
					println("got a display")
					if (display != null) {
						return;
					}
				} catch (Exception e) {
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				} // sleep for a while and try again
			}
		} finally {
			display = null;
		}
	}
}
