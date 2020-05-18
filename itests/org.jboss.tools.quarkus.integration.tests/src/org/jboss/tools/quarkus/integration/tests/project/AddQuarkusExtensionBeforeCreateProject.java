/******************************************************************************* 
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.quarkus.integration.tests.project;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.core.handler.WidgetHandler;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.jface.text.contentassist.ContentAssistant;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.workbench.impl.editor.TextEditor;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.reddeer.common.util.Display;
import org.eclipse.reddeer.common.util.ResultRunnable;
import org.eclipse.reddeer.swt.impl.list.DefaultList;
import org.eclipse.reddeer.swt.impl.styledtext.DefaultStyledText;
import org.jboss.tools.quarkus.reddeer.common.QuarkusLabels;
import org.jboss.tools.quarkus.reddeer.common.QuarkusLabels.TextLabels;
import org.jboss.tools.quarkus.reddeer.perspective.QuarkusPerspective;
import org.jboss.tools.quarkus.reddeer.wizard.CodeProjectTypeWizardPage;
import org.jboss.tools.quarkus.reddeer.wizard.QuarkusWizard;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.tools.quarkus.reddeer.wizard.CodeProjectExtensionsWizardPage;
import org.jboss.tools.quarkus.integration.tests.content.assistant.AbstractContentAssistantTest;
import org.jboss.tools.quarkus.integration.tests.universal.AbstractQuarkusTestMethods;

/**
 * 
 * @author olkornii@redhat.com
 *
 */
@OpenPerspective(QuarkusPerspective.class)
@RunWith(RedDeerSuite.class)
public class AddQuarkusExtensionBeforeCreateProject extends AbstractQuarkusTestMethods {

	private static String PROJECT_NAME = "testAddExtension";
	private static List<String> componentsList = new ArrayList<String>(
			Arrays.asList("kubernetes", "s2i", "openshift", "docker"));
	private static String proposalForChoose = ".auto-deploy-enabled";

	@BeforeClass
	public static void testAddExtension() {
		new WorkbenchShell().setFocus();

		QuarkusWizard qw = new QuarkusWizard();
		qw.open();
		assertTrue(qw.isOpen());

		CodeProjectTypeWizardPage wp = new CodeProjectTypeWizardPage(qw);
		wp.setProjectName(PROJECT_NAME);
		wp.setMavenProjectType();

		qw.next();
		qw.next();

		new CodeProjectExtensionsWizardPage(qw).selectCategory("Cloud");
		new CodeProjectExtensionsWizardPage(qw).selectExtension("Kubernetes"); // w8 for fix
		String[] my_extensions = new CodeProjectExtensionsWizardPage(qw).getExtensions();
		List<String> list_my_extensions = new ArrayList<>(Arrays.asList(my_extensions));
		int index_of_kubernetes = list_my_extensions.indexOf("Kubernetes");
		
		
		click(index_of_kubernetes);
		
		qw.finish(TimePeriod.VERY_LONG);

		assertTrue(new ProjectExplorer().containsProject(PROJECT_NAME));

		checkJdkVersion(PROJECT_NAME, TextLabels.MAVEN_TYPE);

		checkProblemsView();
	}

	@Test
	public void checkExtensions() {
		TextEditor ed = AbstractContentAssistantTest.openFileWithGenericTextEditor(PROJECT_NAME);
		for (String component : componentsList) {
			AbstractContentAssistantTest.insertAndCheckProposal(ed, component);
			ContentAssistant ca = AbstractContentAssistantTest.openContentAssist(ed);
			AbstractContentAssistantTest.checkProposal(ca, component + proposalForChoose);
		}

	}
	
	public static void click(final int offset) {
		Event e = createEvent();
		Widget swtWidget = new DefaultList(QuarkusLabels.TextLabels.EXTENSIONS_NAMES).getSWTWidget(); // mam swtWidget meho listu, kde mam najit "Kubernetes" a kliknout.
		Point p = new DefaultList(QuarkusLabels.TextLabels.EXTENSIONS_NAMES).getSWTWidget().getLocation();
		e.x = p.x;
		e.y = p.y;
		WidgetHandler.getInstance().notifyWidget(SWT.MouseDoubleClick, e, new DefaultList(QuarkusLabels.TextLabels.EXTENSIONS_NAMES).getSWTWidget());
	}

//	 returns point (x,y) of offset in this StyledText widget.
//	private Point getLocationAtOffset(final int offset) {
//		return Display.syncExec(new ResultRunnable<Point>() {
////
//			@Override
//			public Point run() {
//				return new DefaultList(QuarkusLabels.TextLabels.EXTENSIONS_NAMES).getSWTWidget().getLocationAtOffset(offset);
////				swtWidget.
//			}
//		});
//	}

	private static Event createEvent() {
		Event event = new Event();
		event.time = (int) System.currentTimeMillis();
		event.widget = new DefaultList(QuarkusLabels.TextLabels.EXTENSIONS_NAMES).getSWTWidget();
		event.display = Display.getDisplay();
		event.type = SWT.MouseDoubleClick;
		event.button = 1;
		return event;
	}

}
