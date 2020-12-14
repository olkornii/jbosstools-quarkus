package org.jboss.tools.quarkus.integration.tests.common;

import static org.junit.Assert.assertFalse;

import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.exception.RedDeerException;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.swt.impl.browser.InternalBrowser;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.impl.editor.DefaultEditor;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.central.reddeer.api.JavaScriptHelper;
import org.jboss.tools.central.reddeer.wait.CentralIsLoaded;
import org.jboss.tools.quarkus.integration.tests.project.universal.methods.AbstractQuarkusTest;
import org.jboss.tools.quarkus.reddeer.perspective.QuarkusPerspective;
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
public class CentralProjectWizardTest extends AbstractQuarkusTest {

	private static final String CENTRAL_LABEL = "Red Hat Central";
	private static InternalBrowser centralBrowser;
	private static JavaScriptHelper jsHelper = JavaScriptHelper.getInstance();

	@BeforeClass
	public static void openCentral() {

		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);

		DefaultEditor central = new DefaultEditor(CENTRAL_LABEL);
		central.close();

		new DefaultToolItem(new WorkbenchShell(), CENTRAL_LABEL).click();
		new WaitUntil(new CentralIsLoaded());

		new DefaultEditor(CENTRAL_LABEL);

		centralBrowser = new InternalBrowser();
		jsHelper.setBrowser(centralBrowser);
	}

	@Test
	public void openQuarkusProject() {

		jsHelper.clickWizard("Quarkus Project");

		new WaitWhile(new AbstractWaitCondition() {

			@Override
			public boolean test() {
				try {
					return new DefaultShell("New Quarkus project").isDisposed();
				} catch (RedDeerException e) {
					return false;
				}
			}

			@Override
			public String description() {
				return "Opening quarkus project...";
			}
		});

		DefaultShell quarkusProjectShell = new DefaultShell("New Quarkus project");
		quarkusProjectShell.setFocus();
		assertFalse(quarkusProjectShell.isDisposed());
		quarkusProjectShell.close();
	}
}
