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
package org.jboss.tools.quarkus.integration.tests.application.yaml;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasText;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.handler.WorkbenchShellHandler;
import org.eclipse.reddeer.workbench.impl.editor.TextEditor;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.quarkus.integration.tests.project.universal.methods.AbstractQuarkusTest;
import org.jboss.tools.quarkus.reddeer.common.QuarkusLabels.TextLabels;
import org.jboss.tools.quarkus.reddeer.perspective.QuarkusPerspective;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author olkornii@redhat.com
 */
@OpenPerspective(QuarkusPerspective.class)
@RunWith(RedDeerSuite.class)
public class ApplicationYaml extends AbstractQuarkusTest {

	private static String MAVEN_PROJECT_NAME = "testApplicationYaml";
	private static String APPLICATION_YAML_NAME = "application.yaml";
	private static String APPLICATION_YAML_PATH = "src/main/resources";
	private static String POM_NAME = "pom.xml";
	private static String POM_PATH = "";
	private static List<String> extension = Arrays.asList("	</dependency>",
			"		<artifactId>quarkus-config-yaml</artifactId>", "		<groupId>io.quarkus</groupId>",
			"	<dependency>");
	private static List<String> application_yaml = Arrays.asList("    name: new_test_quarkus_project_name", "  application:", "quarkus:");

	@BeforeClass
	public static void createNewQuarkusProject() {
		testCreateNewProject(MAVEN_PROJECT_NAME, TextLabels.MAVEN_TYPE);
		checkJdkVersion(MAVEN_PROJECT_NAME, TextLabels.MAVEN_TYPE);
		checkProblemsView();
	}

	@Test
	public void runWithApplicationYaml() {
		TextEditor editor = openFileWithTextEditor(MAVEN_PROJECT_NAME, TextLabels.TEXT_EDITOR, POM_NAME, POM_PATH);

		addExtensionInPomFile(editor);

		createNewFile(MAVEN_PROJECT_NAME, APPLICATION_YAML_NAME, APPLICATION_YAML_PATH);

		addLinesInApplicationYaml(editor);

		WorkbenchShellHandler.getInstance().closeAllNonWorbenchShells();
		createNewQuarkusConfiguration(MAVEN_PROJECT_NAME);
		new PushButton(TextLabels.RUN).click();
		
		ConsoleView consoleView = new ConsoleView();
		new WaitUntil(new ConsoleHasText(consoleView, "new_test_quarkus_project_name"), TimePeriod.getCustom(600));
		
		checkUrlContent("hello");
		new DefaultToolItem(TextLabels.TERMINATE).click();
//		new DefaultToolItem(new WorkbenchShell(), "Stop "+MAVEN_PROJECT_NAME+TextLabels.CONFIGURATION).click();
	}

	private void addExtensionInPomFile(TextEditor editor) {
		int end_line_number = editor.getLineOfText("</dependencyManagement>");
		String start_dependencies_line = editor.getTextAtLine(end_line_number + 1);
		assertEquals("Expected <dependencies> but was " + start_dependencies_line, "  <dependencies>",
				start_dependencies_line);
		int line_for_insert = end_line_number + 2;

		for (String str : extension) {
			editor.insertLine(line_for_insert, str);
		}

		editor.close(true);
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}

	private void addLinesInApplicationYaml(TextEditor editor) {
		editor = new TextEditor(APPLICATION_YAML_NAME);

		int line_for_insert = 0;
		for (String str : application_yaml) {
			editor.insertLine(line_for_insert, str);
		}
		editor.close(true);
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}
}
