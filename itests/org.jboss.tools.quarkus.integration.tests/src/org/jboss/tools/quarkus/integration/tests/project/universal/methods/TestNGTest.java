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
package org.jboss.tools.quarkus.integration.tests.project.universal.methods;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasText;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.jface.text.contentassist.ContentAssistant;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.impl.editor.TextEditor;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.quarkus.reddeer.common.QuarkusLabels.TextLabels;
import org.jboss.tools.quarkus.reddeer.perspective.QuarkusPerspective;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author Oleksii Korniienko olkornii@redhat.com
 *
 */
@OpenPerspective(QuarkusPerspective.class)
@RunWith(RedDeerSuite.class)
public class TestNGTest extends AbstractQuarkusTest {

	private static String testNG_file_name = "NewTest.java";

	@BeforeClass
	public static void createNewProject() {
		testCreateNewProject("testTestNG", TextLabels.MAVEN_TYPE);
		checkJdkVersion("testTestNG", TextLabels.MAVEN_TYPE);
		checkProblemsView();
	}

	@Test
	public void runAndCheckTestNG() {

		new WorkbenchShell().setFocus();
		new ProjectExplorer().selectProjects("testTestNG");

		new ContextMenuItem("TestNG", "Create TestNG class").select();
		new FinishButton().click();
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);

		TextEditor ed = new TextEditor(testNG_file_name);
		ed.selectText("org.testng");

		ContentAssistant ca = ed.openQuickFixContentAssistant();
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		ca.chooseProposal("Add TestNG library");
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);

		ed.save();
		new ContextMenuItem(TextLabels.RUN_AS_CONTEXT_MENU_ITEM, TextLabels.TESTNG_TEST_CONTEXT_MENU_ITEM).select();

		ConsoleView consoleView = new ConsoleView();
		new WaitUntil(new ConsoleHasText(consoleView, "Total tests run: 1, Passes: 1, Failures: 0, Skips: 0"),
				TimePeriod.LONG);
	}
}
