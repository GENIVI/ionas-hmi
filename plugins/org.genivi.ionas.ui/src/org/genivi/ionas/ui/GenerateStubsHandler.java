/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.genivi.ionas.ui;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.validation.internal.util.Log;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.franca.core.dsl.ui.internal.FrancaIDLAdaptiveActivator;
import org.franca.deploymodel.dsl.ui.internal.FDeployAdaptiveActivator;

import autosar40.util.Autosar40ReleaseDescriptor;
import de.fraunhofer.fokus.autosar_to_franca.AutosarToFrancaTranslation;
import de.fraunhofer.fokus.franca_to_autosar.FrancaToAutosarTranslation;

@SuppressWarnings("restriction")
public class GenerateStubsHandler implements IHandler {

	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}
	
	public MessageConsole getConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();

		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new MessageConsole[]{myConsole});

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view;
		try {
			view = (IConsoleView) page.showView(id);
			view.display(myConsole);
		} catch (PartInitException e) {
			myConsole = null;
		}
		
		return myConsole;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.getCurrentSelection(event) instanceof TreeSelection) {
			final TreeSelection selection = (TreeSelection) HandlerUtil
					.getCurrentSelection(event);
			if (selection.getFirstElement() instanceof IFile) {

				ProgressMonitorDialog pd = new ProgressMonitorDialog(
						HandlerUtil.getActiveShell(event));
				MessageConsole console = getConsole("IoNAS");
				console.activate();
				console.clearConsole();
				final PrintStream out = new PrintStream(console.newMessageStream());
				try {
					pd.run(true, false, new WorkspaceModifyOperation() {

						protected void execute(final IProgressMonitor monitor)
								throws CoreException,
								InvocationTargetException, InterruptedException {
							monitor.beginTask("Generating stubs...", 110);

							// activate plugins (that seems to be important)
							FrancaIDLAdaptiveActivator
									.getInstance()
									.getInjector(
											FrancaIDLAdaptiveActivator.ORG_FRANCA_CORE_DSL_FRANCAIDL);
							FDeployAdaptiveActivator
									.getInstance()
									.getInjector(
											FDeployAdaptiveActivator.ORG_FRANCA_DEPLOYMODEL_DSL_FDEPLOY);

							final IFile file = (IFile) selection.getFirstElement();
							
							TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil
									.getEditingDomain(file.getProject(), Autosar40ReleaseDescriptor.INSTANCE);

							final ResourceSet rs = new XtextResourceSet();
							final URI uri = URI.createPlatformResourceURI(
									file.getProject().getName(), true)
									.appendSegments(
											file.getProjectRelativePath()
													.segments());
							
							editingDomain.getCommandStack().execute(new Command(){

								public boolean canExecute() {
									return true;
								}

								public void execute() {
									AutosarToFrancaTranslation.convertAtoF(uri, rs,
											file.getProject(), monitor, out);
									FrancaToAutosarTranslation.convertFtoA(uri, rs,
											file.getProject(), monitor, out);
								}

								public boolean canUndo() {
									return false;
								}

								public void undo() {
									
								}

								public void redo() {
									
								}

								public Collection<?> getResult() {
									return null;
								}

								public Collection<?> getAffectedObjects() {
									return Collections.EMPTY_LIST;
								}

								public String getLabel() {
									return "IoNAS";
								}

								public String getDescription() {
									return "Generating stubs";
								}

								public void dispose() {									
								}

								public Command chain(Command command) {
									return new CompoundCommand(Arrays.asList(command, this));
								}
								
							});
							monitor.done();
						}
					});
					MessageDialog.openInformation(
							HandlerUtil.getActiveShell(event), "IoNAS",
							"Stubs have been generated. See the console for further information.");
				} catch (Exception e) {
					e.printStackTrace();
					Exception ex = e;
					String message = e.getMessage();
					while ((message == null || message.equals(""))
							&& (ex instanceof RuntimeException || ex instanceof InvocationTargetException)) {
						System.out.println("in while");
						if (ex instanceof RuntimeException && ((RuntimeException)ex).getCause() instanceof Exception) {
							RuntimeException re = (RuntimeException) ex;
							message = re.getCause().getMessage();
							ex = (Exception) re.getCause();
						} else if (ex instanceof InvocationTargetException && ((InvocationTargetException)ex).getTargetException() instanceof Exception) {
							InvocationTargetException ite = (InvocationTargetException) ex;
							message = ite.getTargetException().getMessage();
							ex = (Exception) ite.getTargetException();
						}else{
							break;
						}
					}
					MessageDialog.openError(HandlerUtil.getActiveShell(event),
							"IoNAS",
							"An error occured while generating stubs: "
									+ message);
					Log.log(new Status(Status.ERROR, Activator.PLUGIN_ID,
							Status.OK, message, e));
				}

			}
		}
		return null;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
