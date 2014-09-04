/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.genivi.ionas.test

import java.util.LinkedList
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jface.bindings.keys.KeyStroke
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Display
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView
import org.eclipse.swtbot.swt.finder.SWTBot
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner
import org.eclipse.swtbot.swt.finder.utils.SWTUtils
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*

enum StringConditionType {
	CONTAINS,
	STARTS_WITH,
	EQUALS
}

@RunWith(SWTBotJunit4ClassRunner)
class BasicTransformationTest {
	private static SWTWorkbenchBot bot

	public static final int PROBLEMS_DESCRIPTION_COLUMN_INDEX = 0;
	public static final int PROBLEMS_RESOURCE_COLUMN_INDEX = 1;
	public static final int PROBLEMS_PATH_COLUMN_INDEX = 2;
	public static final int PROBLEMS_TYPE_COLUMN_INDEX = 4;

	@BeforeClass
	def static void beforeClass() throws Exception {
		waitForDisplayToAppear(10000)
		bot = new SWTWorkbenchBot

		Thread.sleep(2000)
		//println("shell visible " + bot.activeShell.visible + " and active " + bot.activeShell.active)
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
		var SWTBotTreeItem item = bot.tree.allItems.get(1)
		
		println("item is " + item)
		bot.checkBox("Copy projects into workspace").select

		Thread.sleep(500)
		bot.button("Finish").setFocus
		bot.button("Deselect All").click
		item.toggleCheck
		Thread.sleep(500)
		bot.button("Finish").click
		println("done with beforeclass")
		// this takes a while
		Thread.sleep(4000)
	}

	@Test
	def void test01() {
		println("starting test01")
		bot.tree.getTreeItem("org.genivi.ionas.testProject").expand
		Thread.sleep(5000)
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("connector1.fcon").contextMenu("Generate stubs").
			click
		println("generating stubs")
		bot.shell("Progress Information")
		Thread.sleep(8000)
		bot.shell("IoNAS").bot.button("OK").click
		println("hopefully done")
		bot.tree.getTreeItem("org.genivi.ionas.testProject").contextMenu("Refresh").click
		Thread.sleep(2000)
		bot.tree.getTreeItem("org.genivi.ionas.testProject").expand
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("output").expand
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("result").expand
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("output").getNode("autosarOutput1.arxml")
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("result").getNode("francaOutput1.fidl")
		
		// check if some of the elements which should have been generated are there
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("output").getNode("autosarOutput1.arxml").doubleClick
		val arOutBot = bot.editorByTitle("autosarOutput1.arxml").bot
		arOutBot.tree.getTreeItem("AUTOSAR").expand
		arOutBot.tree.getTreeItem("AUTOSAR").getNode("ARRoot").expand
		arOutBot.tree.getTreeItem("AUTOSAR").getNode("ARRoot").getNode("aComposition").expand
		arOutBot.tree.getTreeItem("AUTOSAR").getNode("ARRoot").getNode("aComposition").getNode("instance1")
		arOutBot.tree.getTreeItem("AUTOSAR").getNode("ARRoot").getNode("aComposition").getNode("connection2")
		
		arOutBot.tree.getTreeItem("AUTOSAR").expand
		arOutBot.tree.getTreeItem("AUTOSAR").getNode("ApplicationDataTypes")
		
		bot.viewByTitle("AUTOSAR Explorer").setFocus
		bot.tree.getTreeItem("org.genivi.ionas.testProject").getNode("result").getNode("francaOutput1.fidl").doubleClick
		val fOutBot = bot.editorByTitle("francaOutput1.fidl").bot
		fOutBot.styledText.text.contains("CSr")
		
		// check if the console output is there
		var SWTBotView console = bot.views.findFirst[title == "Console"]

		assertNotNull(console)
		val consoleOutput = console.bot.styledText.text
		assertTrue(consoleOutput.contains("Franca model has been successfully created."))
		assertTrue(consoleOutput.contains("AUTOSAR model has been successfully created."))
		assertFalse(consoleOutput.contains("Exception") || consoleOutput.contains("Error"))

		// make sure the problems view shows no errors
		bot.menu("Window").menu("Show View").menu("Problems").click();
		val SWTBotView view = bot.viewByTitle("Problems");
		view.show();
		assertTrue(view.bot.label.text.startsWith("0 errors"))
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
