package org.genivi.ionas.test;

import static junit.framework.Assert.fail;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotMultiPageEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList.ListElement;
import org.junit.Assert;

@SuppressWarnings("restriction")
public class TestUtil {
	
	private final static String TEST_PROJECT_NAME = "org.genivi.ionas.testProject";
	private static final String FEATURE_MODEL_FILE = null;

	public static void deleteProject(SWTWorkbenchBot bot) {
		if (isProjectCreated(TEST_PROJECT_NAME, bot)) {		
			try {
				SWTBotView packageExplorer = getProjectExplorer(bot);
				SWTBotTree tree = packageExplorer.bot().tree();
				SWTBotTreeItem treeItem = tree.getTreeItem(TEST_PROJECT_NAME);
				treeItem.contextMenu("Delete").click();
				// the project deletion confirmation dialog
				SWTBotShell shell = bot.shell("Delete Resources");
				shell.activate();
				bot.checkBox("Delete project contents on disk (cannot be undone)").select();
				bot.button("OK").click();
				
//				SWTBotShell shell2 = bot.shell("Delete Resources");
//				if (shell2!=null) {
//					shell2.activate();
//					bot.button("Continue").click();
//				}
				bot.waitUntil(shellCloses(shell));	
			} catch(WidgetNotFoundException e) {
				fail("Project " + TEST_PROJECT_NAME + " has not been created!");
			}
		}
	}

	public static void createProject(SWTWorkbenchBot bot, String fileName) {
		if (!isProjectCreated(TEST_PROJECT_NAME, bot)) {		
			bot.menu("File").menu("New Feature Model").click();
			SWTBotShell shell = bot.shell("New Feature Model");
			shell.activate();	
			bot.textWithLabel("File name:").setText(fileName);
			bot.comboBoxWithLabel("Select root directory:").setText(
					getWorkspaceRoot() + "/" + TestUtil.TEST_PROJECT_NAME);
			bot.button("Finish").click();
			SWTBotShell warning = bot.shell("Project not found");
			warning.activate();
			bot.button("OK").click();
			bot.waitUntil(Conditions.shellCloses(shell));
		}
	}
	
	public static String getWorkspaceRoot() {
		URL url = Platform.getInstanceLocation().getURL();
		File f = new File(url.getFile());
		return f.getAbsolutePath();
	}

	public static void createProject(SWTWorkbenchBot bot) {
		createProject(bot, TestUtil.FEATURE_MODEL_FILE);
	}
	
	public static boolean isProjectCreated(String name, SWTWorkbenchBot bot) {
		try {
			SWTBotView packageExplorer = getProjectExplorer(bot);
			SWTBotTree tree = packageExplorer.bot().tree();
			tree.getTreeItem(name);
			return true;
		} catch (WidgetNotFoundException e) {
			return false;
		}
	}

	public static SWTBotView getProjectExplorer(SWTWorkbenchBot bot) {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		return view;
	}
	
	public static SWTBotTreeItem openProject(SWTWorkbenchBot bot, String projectName) {
		SWTBotView view = getProjectExplorer(bot);
		SWTBotTree tree = view.bot().tree();
		return tree.getTreeItem(projectName);
	}
	
	public static boolean openModel(
			SWTWorkbenchBot bot, 
			String projectName,
			String modelName
	) {
		SWTBotTreeItem ti = openProject(bot, projectName);
		if (ti==null)
			return false;
		
		String[] segments = modelName.split("/");
		int iLast = segments.length-1;
		for(int i=0; i<iLast; i++) {
			ti.expand();
			ti = ti.getNode(segments[i]);
			if (ti==null)
				return false;
		}
		
		ti.expand();
		ti = ti.getNode(segments[iLast]);
		if (ti==null)
			return false;
		ti.select();
		ti.doubleClick();
		return true;
	}
	
	/**
	 * Import example models into test project.
	 */
	public static void importExampleModels(SWTWorkbenchBot bot) {
		// ensure that test project is available
		createProject(bot);
		
		// determine path to example models
		String modelsPath = new File("models").getAbsolutePath();
		System.out.println("Path of example models: " + modelsPath);

		
//		SWTBotView explorer = TestUtil.getProjectExplorer(bot);
//		SWTBotTree tree = explorer.bot().tree();

		// open import dialog
		bot.menu("File").menu("Import...").click();
		SWTBotShell shellImport = bot.shell("Import");
		shellImport.activate();
		bot.tree().select("General").expandNode("General").select("File System");
		bot.button("Next >").click();

		// switch to "import file system" dialog
		SWTBotShell shell = bot.shell("Import");
		shell.activate();
		bot.comboBox().setFocus();
		
		// provide original folder of example models and select everything
		bot.comboBox().setText(modelsPath);
		bot.comboBox().typeText("\n");
		bot.tree(0).pressShortcut(Keystrokes.SPACE);
		
		// set target project, some options, and finish
		bot.textWithLabel("Into folder:").setText(TestUtil.TEST_PROJECT_NAME);
		bot.checkBoxInGroup("Create top-level folder", "Options").click();
		bot.button("Finish").click();

		System.out.println("Successfully imported example models.");
		bot.sleep(2000);
	}
	
	public static SWTBotTree getModelTree(final SWTWorkbenchBot bot, String fileName) {
		SWTBotMultiPageEditor editor = bot.multipageEditorByTitle(fileName);
		if (editor==null)
			return null;
		
		SWTBotCTabItem treeEditorPage = editor.activatePage("Tree Viewer");
		if (treeEditorPage==null)
			return null;
		
		treeEditorPage.activate();
		return editor.bot().tree();
	}

	public static void pressEnterKey(final Widget widget, final SWTWorkbenchBot bot) {
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				widget.notifyListeners(SWT.Traverse, keyEvent(SWT.NONE, SWT.CR, SWT.Selection, widget, bot));
			}
		});
	}
	
	private static Event keyEvent(int modificationKey, char c, int keyCode,	Widget widget, SWTWorkbenchBot bot) {
		Event keyEvent = createEvent(widget, bot);
		keyEvent.stateMask = modificationKey;
		keyEvent.character = c;
		keyEvent.keyCode = keyCode;
		return keyEvent;
	}

	private static Event createEvent(Widget widget, SWTWorkbenchBot bot) {
		Event event = new Event();
		event.time = (int) System.currentTimeMillis();
		event.widget = widget;
		event.display = bot.getDisplay();
		return event;
	}
	public static void selectTabbedPropertyView(final SWTBot viewerBot, final String tabeText) {
		UIThreadRunnable.syncExec(new VoidResult() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swtbot.swt.finder.results.VoidResult#run()
			 */
			public void run() {
				try {
					List<? extends Widget> widgets = viewerBot.getFinder().findControls(
							WidgetMatcherFactory.widgetOfType(TabbedPropertyList.class));
					Assert.assertTrue(widgets.size() > 0);
					TabbedPropertyList tabbedPropertyList = (TabbedPropertyList) widgets.get(0);
					int i = 0;
					boolean found = false;
					ListElement currentTab;
					Method selectMethod = TabbedPropertyList.class.getDeclaredMethod("select", new Class[] { int.class });
					selectMethod.setAccessible(true);
					do {
						currentTab = ((org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList.ListElement)tabbedPropertyList.getElementAt(i));
						if (currentTab != null) {
							String label = currentTab.getTabItem().getText();
							if (label.equals(tabeText)) {
								found = true;
								selectMethod.invoke(tabbedPropertyList, i);
							}
						}
						i++;
					} while (currentTab != null && !found);
					if (!found) {
						throw new WidgetNotFoundException("Can't find a tab item with " + tabeText + " label 1");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new WidgetNotFoundException("Can't find a tab item with " + tabeText + " label 2");
				}
			}
		});
		}
	
	/**
	 * Select the tab with the name label in the property views
	 */
	public static ListElement getTabItem(final String label, final TabbedPropertyList tabbedProperty) {
		for (final Object listElement : tabbedProperty.getTabList()) {
			if (listElement instanceof ListElement
					&& ((ListElement) listElement).getTabItem().getText()
							.equals(label)) {
				return (ListElement) listElement;
			}
		}
		return null;
	}

}
