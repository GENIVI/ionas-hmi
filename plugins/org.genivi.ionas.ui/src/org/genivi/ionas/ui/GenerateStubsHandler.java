package org.genivi.ionas.ui;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.validation.internal.util.Log;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.franca.core.dsl.ui.internal.FrancaIDLAdaptiveActivator;
import org.franca.deploymodel.dsl.ui.internal.FDeployAdaptiveActivator;

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
					pd.run(true, false, new IRunnableWithProgress() {

						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
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

							IFile file = (IFile) selection.getFirstElement();

							ResourceSet rs = new XtextResourceSet();
							URI uri = URI.createPlatformResourceURI(
									file.getProject().getName(), true)
									.appendSegments(
											file.getProjectRelativePath()
													.segments());
							System.out.println("pre atof");
							AutosarToFrancaTranslation.convertAtoF(uri, rs,
									file.getProject(), monitor, out);
							System.out.println("post atof");
							FrancaToAutosarTranslation.convertFtoA(uri, rs,
									file.getProject(), monitor, out);
							System.out.println("post ftoa");
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
						if (ex instanceof RuntimeException) {
							RuntimeException re = (RuntimeException) ex;
							message = re.getCause().getMessage();
							ex = (Exception) re.getCause();
						} else if (ex instanceof InvocationTargetException) {
							InvocationTargetException ite = (InvocationTargetException) ex;
							message = ite.getTargetException().getMessage();
							ex = (Exception) ite.getTargetException();
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
