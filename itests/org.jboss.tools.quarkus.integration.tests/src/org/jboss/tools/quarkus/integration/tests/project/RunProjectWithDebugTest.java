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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasText;
import org.eclipse.reddeer.eclipse.core.resources.ProjectItem;
import org.eclipse.reddeer.eclipse.debug.ui.views.variables.VariablesView;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.eclipse.ui.perspectives.AbstractPerspective;
import org.eclipse.reddeer.eclipse.ui.perspectives.DebugPerspective;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.menu.ShellMenuItem;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.workbench.impl.editor.TextEditor;
import org.jboss.tools.quarkus.core.QuarkusCorePlugin;
import org.jboss.tools.quarkus.integration.tests.project.universal.methods.AbstractQuarkusTestMethods;
import org.jboss.tools.quarkus.reddeer.common.QuarkusLabels.TextLabels;
import org.jboss.tools.quarkus.reddeer.perspective.QuarkusPerspective;
import org.jboss.tools.quarkus.reddeer.ui.launch.QuarkusLaunchConfigurationTabGroup;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author olkornii@redhat.com Oleksii Korniienko
 * 
 */
@OpenPerspective(QuarkusPerspective.class)
@RunWith(RedDeerSuite.class)
public class RunProjectWithDebugTest extends AbstractQuarkusTestMethods {

	private static String PROJECT_NAME = "testRunWithDebug";
	private static String RESOURCE_PATH = "src/main/java";
	private static String ORG_ACME = "org.acme";
	private static String EXAMPLE_RESOURCE = "ExampleResource.java";
	private static String VARIABLE = "i";
	private static String FIRST_LINE = "    	int " + VARIABLE + " = 5;";
	private static String SECOND_LINE = "    	" + VARIABLE + " = 7;";

	@BeforeClass
	public static void testNewNewQuarkusMavenProject() {
		testCreateNewProject(PROJECT_NAME, TextLabels.MAVEN_TYPE);
		checkJdkVersion(PROJECT_NAME, TextLabels.MAVEN_TYPE);

		checkProblemsView();
	}

	@Test
	public void testRunWithDebug() {

		ProjectItem exampleResource = new ProjectExplorer().getProject(PROJECT_NAME).getProjectItem(RESOURCE_PATH)
				.getProjectItem(ORG_ACME).getProjectItem(EXAMPLE_RESOURCE);

		insertLines(exampleResource, FIRST_LINE, SECOND_LINE);
		addBreakpointToLine(exampleResource, FIRST_LINE);
		addBreakpointToLine(exampleResource, SECOND_LINE);

		new QuarkusLaunchConfigurationTabGroup().selectProject(PROJECT_NAME);
		new QuarkusLaunchConfigurationTabGroup().openDebugConfiguration();

		new DefaultTreeItem(TextLabels.QUARKUS_APPLICATION_TREE_ITEM).select();
		new ContextMenuItem("New Configuration").select();

		new PushButton(TextLabels.DEBUG).click();

		ConsoleView consoleView = new ConsoleView();
		new WaitUntil(new ConsoleHasText(consoleView, "[io.quarkus]"), TimePeriod.getCustom(600));

		URL localhost = null;
		try {
			localhost = new URL("http://localhost:8080/hello");
		} catch (MalformedURLException e) {
			QuarkusCorePlugin.logException("Wrong URL! ", e);
		}

		openDebugPerspective();
		Thread thread = startProject(localhost);
		try {
			Thread.sleep(1000); // w8 for read localhost from child thread
		} catch (InterruptedException e) {
			QuarkusCorePlugin.logException("Thread interupted!", e);
			Thread.currentThread().interrupt();
		}

		new ShellMenuItem("Run", "Step Over").select();
		int firstValue = checkVariable(VARIABLE);
		new ShellMenuItem("Run", "Step Over").select();
		int secondValue = checkVariable(VARIABLE);
		
		assertNotEquals("Variable can`t be same!", firstValue, secondValue);

		ConsoleView cv = new ConsoleView();
		cv.open();
		thread.interrupt();
		new DefaultToolItem("Terminate").click();

		checkProblemsView();

	}

	private void insertLines(ProjectItem exampleResource, String firstValue, String secondValue) {
		exampleResource.open();
		TextEditor ed = new TextEditor(EXAMPLE_RESOURCE);
		int line = ed.getLineOfText("public String hello() {");
		ed.insertLine(line + 1, firstValue);
		ed.insertLine(line + 2, secondValue);
		ed.save();
	}

	private void addBreakpointToLine(ProjectItem exampleResource, String text) {
		exampleResource.open();
		TextEditor textEditor = new TextEditor(EXAMPLE_RESOURCE);
		textEditor.setCursorPosition(textEditor.getPositionOfText(text));
		openDebugPerspective();
		new ShellMenuItem("Run", "Toggle Breakpoint").select();
		openQuarkusPerspective();
	}

	private void openDebugPerspective() {
		AbstractPerspective dp = new DebugPerspective();
		dp.open();
		assertTrue(dp.isOpened());
	}

	private void openQuarkusPerspective() {
		AbstractPerspective qp = new QuarkusPerspective();
		qp.open();
		assertTrue(qp.isOpened());
	}

	private int checkVariable(String varName) {
		VariablesView variablesView = new VariablesView();
		variablesView.open();
		int value = Integer.valueOf(variablesView.getValue(varName));
		return value;
	}

	private Thread startProject(URL localhost) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(localhost.openStream()))) {
					reader.close();
				} catch (IOException e) {
					QuarkusCorePlugin.logException("Can`t read from url!", e);
				}
			}
		});
		thread.start();
		return thread;
	}

}
